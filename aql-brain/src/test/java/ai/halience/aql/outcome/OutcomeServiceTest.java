package ai.halience.aql.outcome;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import ai.halience.aql.Fakes;
import ai.halience.aql.domain.OutcomeRecord;
import ai.halience.aql.domain.OutputRecord;
import ai.halience.aql.domain.Repositories.AuditRepository;
import ai.halience.aql.domain.Repositories.OutcomeRepository;
import ai.halience.aql.domain.Repositories.OutputRepository;
import ai.halience.aql.domain.Repositories.SnapshotRepository;
import ai.halience.aql.domain.Snapshot;
import ai.halience.aql.governance.Actor;
import ai.halience.aql.governance.AuditLog;
import ai.halience.aql.spec.ObservedInferred;
import ai.halience.aql.spec.OutcomeFamily;
import ai.halience.aql.spec.Vocabulary.CorrectionState;
import ai.halience.aql.spec.Vocabulary.OutcomeSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OutcomeServiceTest {

    SnapshotRepository snapshots = mock(SnapshotRepository.class);
    OutcomeRepository outcomeRepo = mock(OutcomeRepository.class);
    OutputRepository outputs = mock(OutputRepository.class);
    AuditRepository audits = mock(AuditRepository.class);
    OutcomeService service;
    Actor actor = Actor.system("outcome_recorder");
    OutputRecord verdict;

    @BeforeEach
    void setUp() {
        Fakes.backed(snapshots);
        Fakes.backed(outcomeRepo);
        Fakes.backed(outputs);
        Fakes.backed(audits);
        service = new OutcomeService(snapshots, outcomeRepo, outputs, new AuditLog(audits));
        verdict = new OutputRecord();
        verdict.setOutputType("OUT-04");
        verdict.setSummary("Asking price looks high against valuation evidence");
        verdict.setConfidence(0.7);
        outputs.save(verdict);
    }

    OutcomeRecord outcome() {
        OutcomeRecord o = new OutcomeRecord();
        o.setFamily(OutcomeFamily.VALUATION);
        o.setEventType("sale_price_recorded");
        o.setObservedInferred(ObservedInferred.OBSERVED);
        o.setOutcomeSource(OutcomeSource.PUBLIC_RECORD);
        o.setResult(Map.of("sale_price", 410000));
        o.setEventTime(Fakes.NOW.plusSeconds(86400));
        return o;
    }

    @Test
    void freezingCopiesThePriorOutputSoLaterChangesCantRewriteIt() {
        Snapshot s = service.freeze(List.of(verdict.getId()), "before the sale completes", actor);
        verdict.setSummary("rewritten in hindsight");
        @SuppressWarnings("unchecked")
        Map<String, Object> frozen = (Map<String, Object>) s.getContent().get(verdict.getId());
        assertThat(frozen.get("summary")).isEqualTo("Asking price looks high against valuation evidence");
        assertThat(s.getContentHash()).hasSize(64);
    }

    @Test
    void anOutcomeEvaluatingAPriorOutputNeedsItsSnapshot() {
        OutcomeRecord o = outcome();
        o.setPriorRef(verdict.getId());
        assertThatThrownBy(() -> service.record(o, actor)).hasMessageContaining("frozen snapshot");
        Snapshot s = service.freeze(List.of(verdict.getId()), "before", actor);
        o.setSnapshotRef(s.getId());
        assertThat(service.record(o, actor).getId()).startsWith("ocm_");
    }

    @Test
    void aSnapshotTakenAfterTheOutcomeIsNotHindsightSafe() {
        Snapshot s = service.freeze(List.of(verdict.getId()), "late", actor);
        OutcomeRecord o = outcome();
        o.setPriorRef(verdict.getId());
        o.setSnapshotRef(s.getId());
        o.setEventTime(Fakes.NOW.minusSeconds(3600));
        assertThatThrownBy(() -> service.record(o, actor)).hasMessageContaining("hindsight-safe");
    }

    @Test
    void silenceIsNeverAnOutcome() {
        OutcomeRecord o = outcome();
        o.setOutcomeSource(null);
        assertThatThrownBy(() -> service.record(o, actor)).hasMessageContaining("explicit source");
        OutcomeRecord noFamily = outcome();
        noFamily.setFamily(null);
        assertThatThrownBy(() -> service.record(noFamily, actor)).hasMessageContaining("19 outcome families");
    }

    @Test
    void correctionKeepsHistoryAndErasureRemovesPersonalContent() {
        OutcomeRecord saved = service.record(outcome(), actor);
        OutcomeRecord corrected = service.correct(saved.getId(), CorrectionState.CORRECTED, "ocm_new", actor);
        assertThat(corrected.isCurrent()).isFalse();
        assertThat(corrected.getResult()).containsKey("sale_price");
        OutcomeRecord erased = service.correct(saved.getId(), CorrectionState.ERASED_ANONYMISED, null, actor);
        assertThat(erased.getResult()).containsOnlyKeys("erased");
    }
}
