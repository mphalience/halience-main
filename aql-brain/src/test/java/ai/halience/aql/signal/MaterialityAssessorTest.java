package ai.halience.aql.signal;

import static org.assertj.core.api.Assertions.assertThat;

import ai.halience.aql.signal.MaterialityAssessor.Change;
import ai.halience.aql.signal.MaterialityAssessor.Verdict;
import ai.halience.aql.spec.SignalClass;
import ai.halience.aql.spec.Vocabulary.Materiality;
import ai.halience.aql.spec.Vocabulary.UserRelevance;
import org.junit.jupiter.api.Test;

class MaterialityAssessorTest {

    @Test
    void aRawSourceEventNeverNotifiesDirectly() {
        Verdict v = MaterialityAssessor.assess(new Change(SignalClass.SG_001, 1.0, false, true));
        assertThat(v.notifyUser()).isFalse();
        assertThat(v.recompute()).isTrue();
        assertThat(v.materiality()).isEqualTo(Materiality.NOT_MATERIAL);
    }

    @Test
    void aMaterialChangeToAWatchedEntityNotifies() {
        Verdict v = MaterialityAssessor.assess(new Change(SignalClass.SG_006, 0.2, false, true));
        assertThat(v.materiality()).isEqualTo(Materiality.MATERIAL);
        assertThat(v.userRelevance()).isEqualTo(UserRelevance.RELEVANT);
        assertThat(v.notifyUser()).isTrue();
        assertThat(MaterialityAssessor.assess(new Change(SignalClass.SG_007, 0.5, false, true)).materiality())
                .isEqualTo(Materiality.CRITICAL);
    }

    @Test
    void smallChangesAreHeldAndUnwatchedEntitiesDontNotify() {
        assertThat(MaterialityAssessor.assess(new Change(SignalClass.SG_006, 0.05, false, true)).notifyUser()).isFalse();
        Verdict unwatched = MaterialityAssessor.assess(new Change(SignalClass.SG_006, 0.5, false, false));
        assertThat(unwatched.materiality()).isEqualTo(Materiality.CRITICAL);
        assertThat(unwatched.notifyUser()).isFalse();
    }

    @Test
    void userEditsRecomputeButAreNeverWatchChanges() {
        for (SignalClass edit : new SignalClass[] {SignalClass.SG_011, SignalClass.SG_030, SignalClass.SG_033}) {
            Verdict v = MaterialityAssessor.assess(new Change(edit, 1.0, false, true));
            assertThat(v.notifyUser()).as(edit.code()).isFalse();
            assertThat(v.recompute()).as(edit.code()).isTrue();
            assertThat(v.userRelevance()).isEqualTo(UserRelevance.NOT_RELEVANT);
        }
        Verdict scoreFromEdit = MaterialityAssessor.assess(new Change(SignalClass.SG_012, 0.4, true, true));
        assertThat(scoreFromEdit.notifyUser()).as("Settle Score moved by the user's own edit").isFalse();
        Verdict scoreFromEvidence = MaterialityAssessor.assess(new Change(SignalClass.SG_012, 0.4, false, true));
        assertThat(scoreFromEvidence.notifyUser()).as("Settle Score moved by external evidence").isTrue();
    }

    @Test
    void operationalSignalsGoToOperationsNotUsers() {
        Verdict incident = MaterialityAssessor.assess(new Change(SignalClass.SG_028, 1.0, false, true));
        assertThat(incident.routeToOps()).isTrue();
        assertThat(incident.notifyUser()).isFalse();
        assertThat(MaterialityAssessor.assess(new Change(SignalClass.SG_026, 0, false, false)).recompute())
                .as("a model/rule version change triggers regression").isTrue();
    }

    @Test
    void ungatedSignalsAreRecordedOnly() {
        Verdict v = MaterialityAssessor.assess(new Change(SignalClass.SG_036, 1.0, false, true));
        assertThat(v.notifyUser()).isFalse();
        assertThat(v.recompute()).isFalse();
    }
}
