package ai.halience.aql.signal;

import java.util.ArrayList;
import java.util.List;

import ai.halience.aql.domain.SignalEvent;
import ai.halience.aql.spec.ObservedInferred;
import ai.halience.aql.spec.SignalClass;
import ai.halience.aql.spec.SpecCode;
import ai.halience.aql.spec.Vocabulary.Initiator;

/**
 * Enforces the Signal Contract's "Always" and "Conditional" field rules on a captured event. Returns the list of
 * violations; an event with any violation is not accepted, so provenance, time, epistemic status and lineage survive
 * every downstream use.
 */
public final class SignalContractValidator {

    private SignalContractValidator() {
    }

    public static List<String> violations(SignalEvent e) {
        List<String> v = new ArrayList<>();
        // Always
        require(v, e.getEventFamily(), "event_family");
        require(v, e.getEventType(), "event_type");
        if (e.getEntityRefs() == null || e.getEntityRefs().isEmpty()) {
            v.add("entity_refs is required (unresolved identity must be explicit, e.g. unresolved:<hint>)");
        }
        if (e.getInitiator() == null) {
            v.add("initiator is required");
        }
        if (e.getObservedInferred() == null) {
            v.add("observed_inferred is required");
        }
        if (e.getEventTime() == null) {
            v.add("event_time is required and must be distinct from captured time");
        }
        require(v, e.getRetentionClass(), "retention_class");
        if (e.getCorrectionState() == null) {
            v.add("correction_revocation_state is required");
        }
        SignalClass signal = e.getSignalClass() == null ? null
                : SpecCode.parse(SignalClass.class, e.getSignalClass()).orElse(null);
        if (e.getSignalClass() != null && signal == null) {
            v.add("signal_class '" + e.getSignalClass() + "' is not an SG code");
        }

        // Conditional
        boolean outcome = signal != null && signal.layer() == SignalClass.Layer.OUTCOME
                || "outcome".equalsIgnoreCase(e.getEventFamily());
        if (outcome && e.getOutcomeSource() == null) {
            v.add("outcome_source is required for an outcome (no feedback is never a label)");
        }
        if (e.isUsedForLearning() && blank(e.getEvidenceSnapshotRef())) {
            v.add("evidence_snapshot_ref is required for events used for evaluation or learning");
        }
        if (e.getInitiator() == Initiator.AQL && blank(e.getModelRuleVersion())) {
            v.add("model_rule_version is required for AQL-derived events");
        }
        if (e.getObservedInferred() == ObservedInferred.INFERRED && e.getConfidence() == null) {
            v.add("confidence is required for inferred values");
        }
        boolean change = signal != null && (signal.layer() == SignalClass.Layer.EVIDENCE
                || signal.layer() == SignalClass.Layer.INTELLIGENCE) || !blank(e.getOldValueRef());
        if (change) {
            if (e.getMateriality() == null) {
                v.add("materiality is required for a state or intelligence change");
            }
            if (blank(e.getOldValueRef()) || blank(e.getNewValueRef())) {
                v.add("old_value_ref and new_value_ref are required for a change");
            }
            if (blank(e.getCauseRef()) && (e.getLineageRefs() == null || e.getLineageRefs().isEmpty())) {
                v.add("cause_ref or lineage_refs are required for a derived change");
            }
        }
        if (e.isPersonalData() && (e.getPermissionConsentBasis() == null || e.getPermissionConsentBasis().isEmpty())) {
            v.add("permission_consent_basis is required when personal or permission-bound data is involved");
        }
        if (signal != null && signal.isUserEdit() && e.getUserRelevance() != null
                && e.getUserRelevance() == ai.halience.aql.spec.Vocabulary.UserRelevance.RELEVANT
                && "watch".equalsIgnoreCase(e.getEventFamily())) {
            v.add("a user edit can't be a relevant Watch change event");
        }
        return v;
    }

    private static void require(List<String> v, String value, String field) {
        if (blank(value)) {
            v.add(field + " is required");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
