package ai.halience.aql.action;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import ai.halience.aql.domain.ActionRecord;
import ai.halience.aql.domain.Repositories.ActionRepository;
import ai.halience.aql.governance.Actor;
import ai.halience.aql.governance.ApprovalService;
import ai.halience.aql.governance.AuditLog;
import ai.halience.aql.governance.AuthorityPolicy;
import ai.halience.aql.governance.GovernanceException;
import ai.halience.aql.governance.GovernanceException.Reason;
import ai.halience.aql.governance.PayloadHasher;
import ai.halience.aql.ops.HealthRegistry;
import ai.halience.aql.spec.Authority;
import ai.halience.aql.spec.Vocabulary.ApprovalMode;
import ai.halience.aql.spec.Vocabulary.Consequence;
import ai.halience.aql.spec.Vocabulary.FailureState;
import ai.halience.aql.spec.Vocabulary.Initiator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Proposing and executing actions (PC-15, OUT-14, OUT-15). Recommendation, preparation, approval and execution stay
 * separate: {@link #propose} records what would be done; {@link #execute} does it only if the action, exactly as it
 * stands, is authorised. Execution is recorded with actor, tool, basis, before/after and result, and a failure is
 * recorded as a failure, never as success.
 */
@Service
public class ActionService {

    private final ActionRepository actions;
    private final ApprovalService approvals;
    private final AuthorityPolicy policy;
    private final AuditLog audit;
    private final HealthRegistry health;
    private final Map<String, Connector> connectors;
    private final Clock clock;

    public ActionService(ActionRepository actions, ApprovalService approvals, AuthorityPolicy policy, AuditLog audit,
            HealthRegistry health, List<Connector> connectors, Clock clock) {
        this.actions = actions;
        this.approvals = approvals;
        this.policy = policy;
        this.audit = audit;
        this.health = health;
        this.connectors = connectors.stream().collect(Collectors.toMap(Connector::channel, Function.identity()));
        this.clock = clock;
    }

    /** Records a proposed action. Proposing grants nothing. */
    @Transactional
    public ActionRecord propose(String actionType, String channel, String target, Map<String, Object> payload,
            Consequence consequence, ApprovalMode approvalMode, String basis, Actor proposer) {
        ActionRecord action = new ActionRecord();
        action.setTenantId(proposer.tenantId());
        action.setActionType(actionType);
        action.setTool(channel);
        action.setTarget(target);
        action.setPayload(payload == null ? new LinkedHashMap<>() : new LinkedHashMap<>(payload));
        action.setConsequence(consequence == null ? Consequence.MEDIUM : consequence);
        action.setApprovalMode(approvalMode == null ? ApprovalMode.REQUIRED : approvalMode);
        action.setAuthorityRequired(Authority.EXECUTE);
        action.setReversible(false);
        action.setBasis(basis);
        action.setActorId(proposer.id());
        action.setActorType(proposer.type().name());
        action.setInitiator(proposer.type() == Actor.Type.AGENT ? Initiator.AQL : Initiator.USER);
        action.setPayloadHash(PayloadHasher.hash(actionType, target, action.getPayload()));
        action.setIdempotencyKey(action.ensureId());
        ActionRecord saved = actions.save(action);
        audit.record(proposer, "action_proposed", saved.getId(), "proposed",
                Map.of("type", actionType, "channel", String.valueOf(channel)));
        return saved;
    }

    public boolean needsApproval(ActionRecord action) {
        return policy.requiresApproval(action.getApprovalMode(), action.getConsequence());
    }

    /**
     * Executes {@code actionId} through the connector for {@code channel}, if it is authorised. {@code expectedType}
     * guards against using one tool to run another kind of action (e.g. publishing via the email tool).
     */
    @Transactional
    public ActionRecord execute(String actionId, String channel, String expectedType, Actor actor) {
        ActionRecord action = actions.findById(actionId)
                .orElseThrow(() -> new GovernanceException(Reason.NOT_FOUND, "No action " + actionId));
        if (expectedType != null && !expectedType.equals(action.getActionType())) {
            throw new GovernanceException(Reason.INVALID, actionId + " is a " + action.getActionType()
                    + " action, not " + expectedType);
        }
        if (action.getStatus() == ActionRecord.Status.EXECUTED) {
            return action; // idempotent: never run twice
        }
        if (action.getStatus().isTerminal()) {
            throw new GovernanceException(Reason.INVALID, actionId + " is " + action.getStatus());
        }
        if (needsApproval(action)) {
            try {
                approvals.requireAuthorised(action);
            } catch (GovernanceException e) {
                audit.record(actor, "execute_denied", actionId, "denied", Map.of("reason", e.getMessage()));
                throw e;
            }
        }
        Connector connector = connectors.getOrDefault(channel, new OutboxConnector(channel));
        action.setStatus(ActionRecord.Status.EXECUTING);
        actions.save(action);
        try {
            String receipt = connector.execute(action);
            action.setStatus(ActionRecord.Status.EXECUTED);
            action.setResult(receipt);
            action.setExecutedAt(Instant.now(clock));
            health.report("connector:" + channel, FailureState.NONE, "ok");
            audit.record(actor, "action_executed", actionId, "succeeded", Map.of("channel", channel));
        } catch (Exception e) {
            action.setStatus(ActionRecord.Status.FAILED);
            action.setResult("failed: " + e.getMessage());
            health.report("connector:" + channel, FailureState.FAILED, e.getMessage());
            audit.record(actor, "action_failed", actionId, "failed", Map.of("error", String.valueOf(e.getMessage())));
        }
        return actions.save(action);
    }

    public ActionRecord get(String actionId) {
        return actions.findById(actionId)
                .orElseThrow(() -> new GovernanceException(Reason.NOT_FOUND, "No action " + actionId));
    }
}
