package ai.halience.aql.outcome;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ai.halience.aql.domain.Json;
import ai.halience.aql.domain.OutcomeRecord;
import ai.halience.aql.domain.OutputRecord;
import ai.halience.aql.domain.Repositories.OutcomeRepository;
import ai.halience.aql.domain.Repositories.OutputRepository;
import ai.halience.aql.domain.Repositories.SnapshotRepository;
import ai.halience.aql.domain.Snapshot;
import ai.halience.aql.governance.Actor;
import ai.halience.aql.governance.AuditLog;
import ai.halience.aql.governance.GovernanceException;
import ai.halience.aql.governance.GovernanceException.Reason;
import ai.halience.aql.governance.PayloadHasher;
import ai.halience.aql.spec.Vocabulary.CorrectionState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The outcome &amp; calibration loop's capture side (LP-06: freeze → observe → evaluate → calibrate). Prior outputs are
 * frozen into snapshots before outcomes arrive, so a later outcome can't rewrite what was known or predicted. An outcome
 * needs an explicit source: silence is never a label. Corrections and revocations change state; they don't delete
 * history.
 */
@Service
public class OutcomeService {

    private final SnapshotRepository snapshots;
    private final OutcomeRepository outcomes;
    private final OutputRepository outputs;
    private final AuditLog audit;

    public OutcomeService(SnapshotRepository snapshots, OutcomeRepository outcomes, OutputRepository outputs,
            AuditLog audit) {
        this.snapshots = snapshots;
        this.outcomes = outcomes;
        this.outputs = outputs;
        this.audit = audit;
    }

    /** Freezes the current state of {@code refs} (output records; other refs are recorded by reference only). */
    @Transactional
    public Snapshot freeze(List<String> refs, String reason, Actor actor) {
        Map<String, Object> content = new LinkedHashMap<>();
        for (String ref : refs) {
            OutputRecord output = outputs.findById(ref).orElse(null);
            if (output == null) {
                content.put(ref, Map.of("by_reference", true));
            } else {
                Map<String, Object> frozen = new LinkedHashMap<>();
                frozen.put("output_type", output.getOutputType());
                frozen.put("summary", output.getSummary());
                frozen.put("claims", output.getClaims());
                frozen.put("confidence", output.getConfidence());
                frozen.put("method_versions", output.getMethodVersions());
                frozen.put("behaviour", output.getBehaviour() + "@" + output.getBehaviourRevision());
                frozen.put("data", output.getData());
                frozen.put("captured", String.valueOf(output.getCapturedTime()));
                content.put(ref, frozen);
            }
        }
        Snapshot snapshot = new Snapshot();
        snapshot.setTenantId(actor.tenantId());
        snapshot.setSubjectRefs(List.copyOf(refs));
        snapshot.setContent(content);
        snapshot.setContentHash(PayloadHasher.sha256(Json.write(content)));
        snapshot.setReason(reason);
        snapshot = snapshots.save(snapshot);
        audit.record(actor, "snapshot_frozen", snapshot.getId(), "succeeded", Map.of("subjects", refs.size()));
        return snapshot;
    }

    /**
     * Records an outcome. When it evaluates a prior output/recommendation/action ({@code priorRef}), a frozen snapshot
     * taken before the outcome is required (hindsight-safe evaluation).
     */
    @Transactional
    public OutcomeRecord record(OutcomeRecord outcome, Actor actor) {
        if (outcome.getFamily() == null) {
            throw new GovernanceException(Reason.INVALID, "An outcome needs one of the 19 outcome families");
        }
        if (outcome.getOutcomeSource() == null) {
            throw new GovernanceException(Reason.INVALID,
                    "An outcome needs an explicit source; no feedback is never an outcome");
        }
        if (outcome.getObservedInferred() == null) {
            throw new GovernanceException(Reason.INVALID, "An outcome must say whether it is observed or inferred");
        }
        if (outcome.getPriorRef() != null) {
            if (outcome.getSnapshotRef() == null) {
                throw new GovernanceException(Reason.INVALID,
                        "An outcome that evaluates a prior output needs the frozen snapshot of it");
            }
            Snapshot snapshot = snapshots.findById(outcome.getSnapshotRef())
                    .orElseThrow(() -> new GovernanceException(Reason.NOT_FOUND, "No snapshot "
                            + outcome.getSnapshotRef()));
            if (!snapshot.getSubjectRefs().contains(outcome.getPriorRef())) {
                throw new GovernanceException(Reason.INVALID, "Snapshot " + snapshot.getId()
                        + " doesn't cover " + outcome.getPriorRef());
            }
            if (outcome.getEventTime() != null && snapshot.getCapturedTime() != null
                    && snapshot.getCapturedTime().isAfter(outcome.getEventTime())) {
                throw new GovernanceException(Reason.INVALID,
                        "The snapshot was taken after the outcome, so it isn't hindsight-safe");
            }
        }
        if (outcome.getTenantId() == null) {
            outcome.setTenantId(actor.tenantId());
        }
        OutcomeRecord saved = outcomes.save(outcome);
        audit.record(actor, "outcome_recorded", saved.getId(), "succeeded",
                Map.of("family", saved.getFamily().name()));
        return saved;
    }

    /** Marks an outcome corrected or revoked; consumers must honour current validity. History is kept. */
    @Transactional
    public OutcomeRecord correct(String outcomeId, CorrectionState state, String replacementId, Actor actor) {
        if (state == CorrectionState.CURRENT) {
            throw new GovernanceException(Reason.INVALID, "Use corrected, revoked or erased_anonymised");
        }
        OutcomeRecord outcome = outcomes.findById(outcomeId)
                .orElseThrow(() -> new GovernanceException(Reason.NOT_FOUND, "No outcome " + outcomeId));
        outcome.setCorrectionState(state);
        outcome.setSupersededBy(replacementId);
        if (state == CorrectionState.ERASED_ANONYMISED) {
            outcome.setResult(Map.of("erased", true));
            outcome.setEntityRefs(List.of());
        }
        audit.record(actor, "outcome_corrected", outcomeId, state.name(), Map.of());
        return outcomes.save(outcome);
    }
}
