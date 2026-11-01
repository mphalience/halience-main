package ai.halience.aql.primitive;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ai.halience.aql.config.BrainProperties;
import ai.halience.aql.domain.ActionRecord;
import ai.halience.aql.domain.ApprovalRecord;
import ai.halience.aql.domain.PrimitiveRecord;
import ai.halience.aql.domain.PrimitiveRevision;
import ai.halience.aql.domain.Repositories.ActionRepository;
import ai.halience.aql.domain.Repositories.PrimitiveRepository;
import ai.halience.aql.domain.Repositories.PrimitiveRevisionRepository;
import ai.halience.aql.governance.Actor;
import ai.halience.aql.governance.ApprovalService;
import ai.halience.aql.governance.AuditLog;
import ai.halience.aql.governance.GovernanceException;
import ai.halience.aql.governance.GovernanceException.Reason;
import ai.halience.aql.spec.Authority;
import ai.halience.aql.spec.Vocabulary.ApprovalMode;
import ai.halience.aql.spec.Vocabulary.Consequence;
import ai.halience.aql.spec.Vocabulary.Initiator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The one door: the only way a primitive changes. Halie (or a person) proposes an exact new version; a Halience admin
 * approves that exact version; an admin applies it. Each step leaves an Action record, every applied version is kept as
 * a revision, and any primitive can be rolled back to an earlier revision. This follows the configuration progression
 * View → Propose → Review/Approve → Apply: a proposal creates no authority and changes nothing live.
 */
@Service
public class PrimitiveEditService {

    public static final String EDIT = "primitive_edit";
    public static final String ROLLBACK = "primitive_rollback";

    private final PrimitiveRepository records;
    private final PrimitiveRevisionRepository revisions;
    private final ActionRepository actions;
    private final ApprovalService approvals;
    private final PrimitiveLibrary library;
    private final AuditLog audit;
    private final String adminRole;
    private final Clock clock;

    public PrimitiveEditService(PrimitiveRepository records, PrimitiveRevisionRepository revisions,
            ActionRepository actions, ApprovalService approvals, PrimitiveLibrary library, AuditLog audit,
            BrainProperties properties, Clock clock) {
        this.records = records;
        this.revisions = revisions;
        this.actions = actions;
        this.approvals = approvals;
        this.library = library;
        this.audit = audit;
        this.adminRole = properties.brain().adminRole();
        this.clock = clock;
    }

    /** A proposal: the action awaiting approval and the approval an admin must decide. */
    public record Proposal(ActionRecord action, ApprovalRecord approval, List<ValidationIssue> warnings) {
    }

    /**
     * Proposes {@code markdown} as the new text of the primitive at {@code key} (new or existing). The whole set is
     * validated with the change in place; a proposal that would break the brain is refused outright.
     */
    @Transactional
    public Proposal propose(String key, String markdown, String reason, Actor proposer) {
        Primitive proposed;
        try {
            proposed = Primitive.parse(key + ".md", markdown);
        } catch (IllegalArgumentException e) {
            throw new GovernanceException(Reason.INVALID, "The proposed primitive doesn't parse: " + e.getMessage());
        }
        List<ValidationIssue> issues = PrimitiveValidator.validate(library.current().with(proposed));
        List<ValidationIssue> errors = issues.stream().filter(ValidationIssue::isError).toList();
        if (!errors.isEmpty()) {
            throw new GovernanceException(Reason.INVALID, "The proposal would break the brain: " + errors);
        }
        PrimitiveRecord existing = records.findByKey(key).orElse(null);

        ActionRecord action = new ActionRecord();
        action.setTenantId(proposer.tenantId());
        action.setActionType(EDIT);
        action.setTarget(key);
        action.setTool("primitive_propose");
        action.setActorId(proposer.id());
        action.setActorType(proposer.type().name());
        action.setInitiator(proposer.type() == Actor.Type.AGENT ? Initiator.AQL : Initiator.USER);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("markdown", markdown);
        payload.put("baseRevision", existing == null ? 0 : existing.getRevision());
        action.setPayload(payload);
        action.setBeforeState(existing == null ? Map.of() : Map.of("markdown", existing.getMarkdown(),
                "revision", existing.getRevision()));
        action.setAuthorityRequired(Authority.EXECUTE_WITH_APPROVAL);
        action.setApprovalMode(ApprovalMode.REQUIRED);
        action.setConsequence(consequenceOf(proposed));
        action.setReversible(true);
        action.setBasis(reason);
        action = actions.save(action);
        ApprovalRecord approval = approvals.request(action, proposer, adminRole,
                "Primitive changes need a Halience admin's approval");
        return new Proposal(action, approval, issues);
    }

    /** Applies an approved proposal. Only an admin may apply, and only the exact approved version. */
    @Transactional
    public PrimitiveRecord apply(String actionId, Actor admin) {
        requireAdmin(admin);
        ActionRecord action = actions.findById(actionId)
                .orElseThrow(() -> new GovernanceException(Reason.NOT_FOUND, "No action " + actionId));
        if (!EDIT.equals(action.getActionType())) {
            throw new GovernanceException(Reason.INVALID, actionId + " is not a primitive edit");
        }
        if (action.getStatus() != ActionRecord.Status.APPROVED) {
            throw new GovernanceException(Reason.APPROVAL, "Action " + actionId + " is " + action.getStatus()
                    + ", not approved");
        }
        approvals.requireAuthorised(action);
        String key = action.getTarget();
        String markdown = (String) action.getPayload().get("markdown");
        int baseRevision = ((Number) action.getPayload().get("baseRevision")).intValue();
        PrimitiveRecord record = records.findByKey(key).orElse(null);
        int currentRevision = record == null ? 0 : record.getRevision();
        if (currentRevision != baseRevision) {
            throw new GovernanceException(Reason.INVALID, key + " changed since this was proposed (revision "
                    + baseRevision + " → " + currentRevision + "); propose it again");
        }
        PrimitiveRecord saved = write(key, markdown, record, admin, action.getId(), action.getBasis());
        action.setStatus(ActionRecord.Status.EXECUTED);
        action.setExecutedAt(Instant.now(clock));
        action.setAfterState(Map.of("revision", saved.getRevision()));
        action.setResult("Applied as revision " + saved.getRevision());
        actions.save(action);
        audit.record(admin, "primitive_applied", key, "succeeded", Map.of("action", actionId,
                "revision", saved.getRevision()));
        library.reload();
        return saved;
    }

    /** Rolls a primitive back to an earlier revision, as a new revision. Admin only; leaves an Action record. */
    @Transactional
    public PrimitiveRecord rollback(String key, int toRevision, Actor admin, String reason) {
        requireAdmin(admin);
        PrimitiveRecord record = records.findByKey(key)
                .orElseThrow(() -> new GovernanceException(Reason.NOT_FOUND, "No primitive " + key));
        PrimitiveRevision target = revisions.findByPrimitiveKeyAndRevision(key, toRevision)
                .orElseThrow(() -> new GovernanceException(Reason.NOT_FOUND, key + " has no revision " + toRevision));
        Primitive restored = Primitive.parse(key + ".md", target.getMarkdown());
        List<ValidationIssue> errors = PrimitiveValidator.errors(library.current().with(restored));
        if (!errors.isEmpty()) {
            throw new GovernanceException(Reason.INVALID, "Rolling back would break the brain: " + errors);
        }
        ActionRecord action = new ActionRecord();
        action.setTenantId(admin.tenantId());
        action.setActionType(ROLLBACK);
        action.setTarget(key);
        action.setActorId(admin.id());
        action.setActorType(admin.type().name());
        action.setInitiator(Initiator.USER);
        action.setPayload(Map.of("toRevision", toRevision));
        action.setBeforeState(Map.of("revision", record.getRevision(), "markdown", record.getMarkdown()));
        action.setAuthorityRequired(Authority.EXECUTE);
        action.setConsequence(consequenceOf(restored));
        action.setBasis(reason);
        action.setStatus(ActionRecord.Status.EXECUTING);
        action = actions.save(action);
        PrimitiveRecord saved = write(key, target.getMarkdown(), record, admin, action.getId(),
                "Rolled back to revision " + toRevision + (reason == null ? "" : ": " + reason));
        action.setStatus(ActionRecord.Status.EXECUTED);
        action.setExecutedAt(Instant.now(clock));
        action.setAfterState(Map.of("revision", saved.getRevision()));
        actions.save(action);
        audit.record(admin, "primitive_rolled_back", key, "succeeded", Map.of("toRevision", toRevision));
        library.reload();
        return saved;
    }

    public List<PrimitiveRevision> history(String key) {
        return revisions.findByPrimitiveKeyOrderByRevisionDesc(key);
    }

    private PrimitiveRecord write(String key, String markdown, PrimitiveRecord existing, Actor actor, String actionId,
            String reason) {
        PrimitiveRecord record = existing;
        if (record == null) {
            record = new PrimitiveRecord();
            record.setKey(key);
            record.setType(Primitive.parse(key + ".md", markdown).type().name());
            record.setRevision(1);
        } else {
            record.setRevision(record.getRevision() + 1);
        }
        record.setMarkdown(markdown);
        record.setEdited(true);
        record.setUpdatedBy(actor.id());
        record = records.save(record);
        PrimitiveRevision revision = new PrimitiveRevision();
        revision.setPrimitiveKey(key);
        revision.setRevision(record.getRevision());
        revision.setMarkdown(markdown);
        revision.setActionRef(actionId);
        revision.setCreatedBy(actor.id());
        revision.setReason(reason);
        revisions.save(revision);
        return record;
    }

    private void requireAdmin(Actor actor) {
        if (!actor.isPerson() || !actor.hasRole(adminRole)) {
            throw new GovernanceException(Reason.AUTHORITY, "Only a Halience admin can change primitives");
        }
    }

    /** Behaviours and tools change what the brain does; knowledge, journeys and routines change what it uses. */
    static Consequence consequenceOf(Primitive primitive) {
        return switch (primitive.type()) {
            case BEHAVIOUR, TOOL -> Consequence.HIGH;
            case KNOWLEDGE, JOURNEY, ROUTINE -> Consequence.MEDIUM;
        };
    }
}
