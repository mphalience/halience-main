package ai.halience.aql.signal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import ai.halience.aql.Fakes;
import ai.halience.aql.domain.SignalEvent;
import ai.halience.aql.spec.ObservedInferred;
import ai.halience.aql.spec.Vocabulary.Initiator;
import ai.halience.aql.spec.Vocabulary.Materiality;
import ai.halience.aql.spec.Vocabulary.OutcomeSource;
import ai.halience.aql.spec.Vocabulary.UserRelevance;
import org.junit.jupiter.api.Test;

class SignalContractValidatorTest {

    static SignalEvent valid() {
        SignalEvent e = new SignalEvent();
        e.setSignalClass("SG-013");
        e.setEventFamily("document");
        e.setEventType("document_uploaded.v1");
        e.setEntityRefs(List.of("doc_1", "user_1"));
        e.setInitiator(Initiator.USER);
        e.setObservedInferred(ObservedInferred.OBSERVED);
        e.setEventTime(Fakes.NOW);
        return e;
    }

    @Test
    void aCompleteEventPasses() {
        assertThat(SignalContractValidator.violations(valid())).isEmpty();
    }

    @Test
    void alwaysFieldsAreRequired() {
        SignalEvent e = new SignalEvent();
        assertThat(SignalContractValidator.violations(e)).anyMatch(v -> v.startsWith("event_family"))
                .anyMatch(v -> v.startsWith("event_type")).anyMatch(v -> v.startsWith("entity_refs"))
                .anyMatch(v -> v.startsWith("initiator")).anyMatch(v -> v.startsWith("observed_inferred"))
                .anyMatch(v -> v.startsWith("event_time"));
    }

    @Test
    void anOutcomeNeedsItsSourceBecauseSilenceIsNotALabel() {
        SignalEvent e = valid();
        e.setSignalClass("SG-019");
        assertThat(SignalContractValidator.violations(e)).anyMatch(v -> v.contains("outcome_source"));
        e.setOutcomeSource(OutcomeSource.PUBLIC_RECORD);
        assertThat(SignalContractValidator.violations(e)).isEmpty();
    }

    @Test
    void derivedChangesNeedLineageMaterialityVersionsAndConfidence() {
        SignalEvent e = valid();
        e.setSignalClass("SG-006");
        e.setInitiator(Initiator.AQL);
        e.setObservedInferred(ObservedInferred.INFERRED);
        assertThat(SignalContractValidator.violations(e)).anyMatch(v -> v.contains("model_rule_version"))
                .anyMatch(v -> v.contains("confidence")).anyMatch(v -> v.contains("materiality"))
                .anyMatch(v -> v.contains("old_value_ref")).anyMatch(v -> v.contains("cause_ref"));
        e.setModelRuleVersion("risk-v1");
        e.setConfidence(0.8);
        e.setMateriality(Materiality.MATERIAL);
        e.setOldValueRef("risk_old");
        e.setNewValueRef("risk_new");
        e.setCauseRef("evt_source");
        assertThat(SignalContractValidator.violations(e)).isEmpty();
    }

    @Test
    void learningUseNeedsAFrozenSnapshotAndPersonalDataNeedsABasis() {
        SignalEvent e = valid();
        e.setUsedForLearning(true);
        e.setPersonalData(true);
        assertThat(SignalContractValidator.violations(e)).anyMatch(v -> v.contains("evidence_snapshot_ref"))
                .anyMatch(v -> v.contains("permission_consent_basis"));
    }

    @Test
    void aUserEditCantBeARelevantWatchEvent() {
        SignalEvent e = valid();
        e.setSignalClass("SG-030");
        e.setEventFamily("watch");
        e.setUserRelevance(UserRelevance.RELEVANT);
        assertThat(SignalContractValidator.violations(e)).anyMatch(v -> v.contains("user edit"));
    }
}
