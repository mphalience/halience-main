package ai.halience.aql.signal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import ai.halience.aql.Fakes;
import ai.halience.aql.domain.OutputRecord;
import ai.halience.aql.domain.Repositories.AuditRepository;
import ai.halience.aql.domain.Repositories.OutputRepository;
import ai.halience.aql.domain.Repositories.SignalEventRepository;
import ai.halience.aql.domain.SignalEvent;
import ai.halience.aql.governance.Actor;
import ai.halience.aql.governance.AuditLog;
import ai.halience.aql.governance.GovernanceException;
import ai.halience.aql.ops.HealthRegistry;
import ai.halience.aql.spec.ObservedInferred;
import ai.halience.aql.spec.Vocabulary.FailureState;
import ai.halience.aql.spec.Vocabulary.Initiator;
import ai.halience.aql.spec.Vocabulary.Materiality;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SignalServiceTest {

    SignalEventRepository events = mock(SignalEventRepository.class);
    OutputRepository outputs = mock(OutputRepository.class);
    AuditRepository audits = mock(AuditRepository.class);
    HealthRegistry health = new HealthRegistry(Fakes.CLOCK);
    Map<String, OutputRecord> outputStore;
    SignalService service;
    Actor actor = Actor.system("watch_change_detector");

    @BeforeEach
    void setUp() {
        Fakes.backed(events);
        outputStore = Fakes.backed(outputs);
        Fakes.backed(audits);
        service = new SignalService(events, outputs, health, new AuditLog(audits));
    }

    static SignalEvent riskChange() {
        SignalEvent e = new SignalEvent();
        e.setSignalClass("SG-006");
        e.setEventFamily("watch");
        e.setEventType("material_risk_change.v1");
        e.setEntityRefs(List.of("prop_1"));
        e.setInitiator(Initiator.AQL);
        e.setObservedInferred(ObservedInferred.INFERRED);
        e.setConfidence(0.8);
        e.setModelRuleVersion("risk-v1");
        e.setOldValueRef("risk_low");
        e.setNewValueRef("risk_high");
        e.setCauseRef("evt_flood_map");
        e.setEventTime(Fakes.NOW);
        return e;
    }

    @Test
    void theMaterialityGateSuppliesMaterialityBeforeTheContractIsChecked() {
        SignalService.Captured c = service.capture(riskChange(), 1.0, false, true, actor);
        assertThat(c.event().getMateriality()).isEqualTo(Materiality.CRITICAL);
        assertThat(c.materialChange()).isNotNull();
        assertThat(c.materialChange().getOutputType()).isEqualTo("OUT-12");
        assertThat(c.materialChange().getEvidenceRefs()).containsExactly(c.event().getId());
    }

    @Test
    void aChangeToAnUnwatchedEntityIsRecordedButNotSurfaced() {
        SignalService.Captured c = service.capture(riskChange(), 1.0, false, false, actor);
        assertThat(c.materialChange()).isNull();
        assertThat(outputStore).isEmpty();
    }

    @Test
    void contractViolationsAreRefused() {
        SignalEvent bad = riskChange();
        bad.setModelRuleVersion(null);
        bad.setCauseRef(null);
        assertThatThrownBy(() -> service.capture(bad, 1.0, false, true, actor))
                .isInstanceOf(GovernanceException.class)
                .hasMessageContaining("model_rule_version").hasMessageContaining("cause_ref");
    }

    @Test
    void anIncidentUpdatesHealthForOperations() {
        SignalEvent incident = new SignalEvent();
        incident.setSignalClass("SG-028");
        incident.setEventFamily("control");
        incident.setEventType("connector_down.v1");
        incident.setEntityRefs(List.of("connector:email"));
        incident.setInitiator(Initiator.SYSTEM);
        incident.setObservedInferred(ObservedInferred.OBSERVED);
        incident.setEventTime(Fakes.NOW);
        SignalService.Captured c = service.capture(incident, 1.0, false, false, actor);
        assertThat(c.materialChange()).isNull();
        assertThat(health.state("connector:email")).isEqualTo(FailureState.FAILED);
    }
}
