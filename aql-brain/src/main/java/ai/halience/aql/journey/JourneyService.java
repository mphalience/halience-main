package ai.halience.aql.journey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ai.halience.aql.domain.ApprovalRecord;
import ai.halience.aql.domain.JourneyRun;
import ai.halience.aql.domain.Repositories.ApprovalRepository;
import ai.halience.aql.domain.Repositories.JourneyRunRepository;
import ai.halience.aql.governance.Actor;
import ai.halience.aql.governance.AuditLog;
import ai.halience.aql.governance.GovernanceException;
import ai.halience.aql.governance.GovernanceException.Reason;
import ai.halience.aql.journey.JourneyDefinition.Milestone;
import ai.halience.aql.primitive.Primitive;
import ai.halience.aql.primitive.PrimitiveLibrary;
import ai.halience.aql.primitive.PrimitiveType;
import ai.halience.aql.spec.Vocabulary.ApprovalMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Follows journeys for subjects: which milestones are done, which can start, and completing one. A milestone whose
 * {@code approval} is {@code required} can only be completed with an approved approval record: stage gates, publishing,
 * investment and transaction steps stay human decisions.
 */
@Service
public class JourneyService {

    private final PrimitiveLibrary library;
    private final JourneyRunRepository runs;
    private final ApprovalRepository approvals;
    private final AuditLog audit;

    public JourneyService(PrimitiveLibrary library, JourneyRunRepository runs, ApprovalRepository approvals,
            AuditLog audit) {
        this.library = library;
        this.runs = runs;
        this.approvals = approvals;
        this.audit = audit;
    }

    public record Status(JourneyRun run, JourneyDefinition journey, List<Milestone> available, boolean complete) {
    }

    public JourneyDefinition definition(String name) {
        Primitive primitive = library.current().named(PrimitiveType.JOURNEY, name)
                .orElseThrow(() -> new GovernanceException(Reason.NOT_FOUND, "No journey " + name));
        if (!library.isEnabled(primitive)) {
            throw new GovernanceException(Reason.DISABLED, "Journey " + name + " is phase " + primitive.phase()
                    + " and not enabled");
        }
        return JourneyDefinition.of(primitive);
    }

    @Transactional
    public Status start(String journey, String subjectRef, Actor actor) {
        JourneyDefinition definition = definition(journey);
        JourneyRun run = new JourneyRun();
        run.setTenantId(actor.tenantId());
        run.setJourney(definition.name());
        run.setSubjectRef(subjectRef);
        run.setScopeRef(subjectRef);
        run.setStartedBy(actor.id());
        run = runs.save(run);
        audit.record(actor, "journey_started", run.getId(), "started", Map.of("journey", journey));
        return status(run);
    }

    public Status status(String runId) {
        return status(runs.findById(runId)
                .orElseThrow(() -> new GovernanceException(Reason.NOT_FOUND, "No journey run " + runId)));
    }

    Status status(JourneyRun run) {
        JourneyDefinition definition = definition(run.getJourney());
        Set<String> done = new HashSet<>(run.getCompleted());
        return new Status(run, definition, JourneyEngine.available(definition, done, library.maxPhase()),
                JourneyEngine.isComplete(definition, done, library.maxPhase()));
    }

    /**
     * Completes a milestone with the ref of what completed it (an output, an outcome, an approval). Required-approval
     * milestones need {@code approvalId} pointing at an approved approval.
     */
    @Transactional
    public Status complete(String runId, String milestoneKey, String evidenceRef, String approvalId, Actor actor) {
        Status status = status(runId);
        JourneyRun run = status.run();
        if (run.getStatus() != JourneyRun.Status.ACTIVE) {
            throw new GovernanceException(Reason.INVALID, "Journey run " + runId + " is " + run.getStatus());
        }
        Milestone milestone = status.journey().milestone(milestoneKey);
        if (status.available().stream().noneMatch(m -> m.key().equals(milestoneKey))) {
            throw new GovernanceException(Reason.INVALID, "Milestone " + milestoneKey
                    + " isn't available yet; it comes after " + milestone.after());
        }
        if (milestone.approval() == ApprovalMode.REQUIRED) {
            ApprovalRecord approval = approvalId == null ? null : approvals.findById(approvalId).orElse(null);
            if (approval == null || approval.getState() != ApprovalRecord.State.APPROVED) {
                throw new GovernanceException(Reason.APPROVAL, "Milestone " + milestoneKey
                        + " needs an approved approval to complete");
            }
        }
        List<String> completed = new ArrayList<>(run.getCompleted());
        completed.add(milestoneKey);
        run.setCompleted(completed);
        Map<String, Object> evidence = new LinkedHashMap<>(run.getEvidence());
        evidence.put(milestoneKey, approvalId != null ? approvalId : String.valueOf(evidenceRef));
        run.setEvidence(evidence);
        if (JourneyEngine.isComplete(status.journey(), new HashSet<>(completed), library.maxPhase())) {
            run.setStatus(JourneyRun.Status.COMPLETED);
        }
        runs.save(run);
        audit.record(actor, "milestone_completed", run.getId(), "completed", Map.of("milestone", milestoneKey));
        return status(run);
    }
}
