package ai.halience.aql.governance;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

import ai.halience.aql.domain.ActionRecord;
import ai.halience.aql.domain.ApprovalRecord;
import ai.halience.aql.domain.Repositories.ActionRepository;
import ai.halience.aql.domain.Repositories.ApprovalRepository;
import ai.halience.aql.governance.GovernanceException.Reason;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The approval &amp; reapproval loop (LP-15). An approval binds to the exact payload hash of one action:
 * <ul>
 *   <li>only a person can approve, and only one holding the required role;</li>
 *   <li>nobody approves their own request, and an agent's request can't be approved by the agent's principal on the
 *       agent's say-so: the principal must decide as a person;</li>
 *   <li>if the action's payload changes after approval, the hash no longer matches and the approval no longer
 *       authorises it (reapproval needed);</li>
 *   <li>approving never executes anything.</li>
 * </ul>
 */
@Service
public class ApprovalService {

    private final ApprovalRepository approvals;
    private final ActionRepository actions;
    private final AuditLog audit;
    private final Clock clock;

    public ApprovalService(ApprovalRepository approvals, ActionRepository actions, AuditLog audit, Clock clock) {
        this.approvals = approvals;
        this.actions = actions;
        this.audit = audit;
        this.clock = clock;
    }

    /** Opens an approval for {@code action} as it stands now. */
    @Transactional
    public ApprovalRecord request(ActionRecord action, Actor requester, String requiredRole, String policyBasis) {
        if (action.getStatus().isTerminal()) {
            throw new GovernanceException(Reason.INVALID, "Action " + action.getId() + " is already "
                    + action.getStatus());
        }
        String hash = PayloadHasher.hash(action.getActionType(), action.getTarget(), action.getPayload());
        action.setPayloadHash(hash);
        ApprovalRecord approval = new ApprovalRecord();
        approval.setTenantId(action.getTenantId());
        approval.setScopeRef(action.getScopeRef());
        approval.setSubjectRef(action.ensureId());
        approval.setPayloadHash(hash);
        approval.setRequestedBy(requester.id());
        approval.setRequiredRole(requiredRole);
        approval.setPolicyBasis(policyBasis);
        approval = approvals.save(approval);
        action.setApprovalRef(approval.getId());
        action.setStatus(ActionRecord.Status.AWAITING_APPROVAL);
        actions.save(action);
        audit.record(requester, "approval_requested", action.getId(), "pending",
                Map.of("approval", approval.getId(), "requiredRole", String.valueOf(requiredRole)));
        return approval;
    }

    @Transactional
    public ApprovalRecord decide(String approvalId, Actor approver, boolean approve, String reason) {
        ApprovalRecord approval = approvals.findById(approvalId)
                .orElseThrow(() -> new GovernanceException(Reason.NOT_FOUND, "No approval " + approvalId));
        ActionRecord action = actions.findById(approval.getSubjectRef())
                .orElseThrow(() -> new GovernanceException(Reason.NOT_FOUND, "No action " + approval.getSubjectRef()));
        if (approval.getState() != ApprovalRecord.State.PENDING) {
            throw new GovernanceException(Reason.INVALID, "Approval " + approvalId + " is " + approval.getState());
        }
        if (!approver.isPerson()) {
            deny(approver, approval, "only a person can approve");
        }
        if (approval.getRequiredRole() != null && !approver.hasRole(approval.getRequiredRole())) {
            deny(approver, approval, "approver needs the " + approval.getRequiredRole() + " role");
        }
        if (approver.id().equals(approval.getRequestedBy()) || approver.id().equals(action.getActorId())) {
            deny(approver, approval, "nobody can approve their own request");
        }
        String current = PayloadHasher.hash(action.getActionType(), action.getTarget(), action.getPayload());
        if (!current.equals(approval.getPayloadHash())) {
            approval.setState(ApprovalRecord.State.SUPERSEDED);
            approvals.save(approval);
            throw new GovernanceException(Reason.APPROVAL,
                    "The action changed after approval was requested; request approval again");
        }
        approval.setState(approve ? ApprovalRecord.State.APPROVED : ApprovalRecord.State.REJECTED);
        approval.setDecidedBy(approver.id());
        approval.setDecidedAt(Instant.now(clock));
        approval.setReason(reason);
        action.setStatus(approve ? ActionRecord.Status.APPROVED : ActionRecord.Status.REJECTED);
        actions.save(action);
        audit.record(approver, approve ? "approved" : "rejected", action.getId(), approval.getState().name(),
                Map.of("approval", approvalId));
        return approvals.save(approval);
    }

    private void deny(Actor approver, ApprovalRecord approval, String why) {
        audit.record(approver, "approval_denied", approval.getSubjectRef(), "denied", Map.of("reason", why));
        throw new GovernanceException(Reason.APPROVAL, "Can't decide approval " + approval.getId() + ": " + why);
    }

    /** Whether {@code action}, exactly as it stands now, is authorised by a current approval. */
    public boolean isAuthorised(ActionRecord action) {
        if (action.getApprovalRef() == null) {
            return false;
        }
        return approvals.findById(action.getApprovalRef())
                .filter(a -> a.getState() == ApprovalRecord.State.APPROVED)
                .filter(a -> a.getSubjectRef().equals(action.getId()))
                .filter(a -> a.getExpiresAt() == null || a.getExpiresAt().isAfter(Instant.now(clock)))
                .filter(a -> a.getPayloadHash().equals(
                        PayloadHasher.hash(action.getActionType(), action.getTarget(), action.getPayload())))
                .isPresent();
    }

    public void requireAuthorised(ActionRecord action) {
        if (!isAuthorised(action)) {
            throw new GovernanceException(Reason.APPROVAL, "Action " + action.getId()
                    + " has no current approval for exactly this payload");
        }
    }
}
