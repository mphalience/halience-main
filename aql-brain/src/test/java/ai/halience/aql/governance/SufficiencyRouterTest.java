package ai.halience.aql.governance;

import static org.assertj.core.api.Assertions.assertThat;

import ai.halience.aql.spec.Vocabulary.Consequence;
import ai.halience.aql.spec.Vocabulary.FailureState;
import ai.halience.aql.spec.Vocabulary.Route;
import ai.halience.aql.spec.Vocabulary.Sufficiency;
import org.junit.jupiter.api.Test;

class SufficiencyRouterTest {

    @Test
    void sufficientConfidentResultsNeedNoRouting() {
        assertThat(SufficiencyRouter.route(Sufficiency.SUFFICIENT, 0.9, Consequence.HIGH, FailureState.NONE))
                .isEqualTo(Route.NONE);
    }

    @Test
    void insufficientEvidenceEscalatesWhenConsequential() {
        assertThat(SufficiencyRouter.route(Sufficiency.INSUFFICIENT, 0.95, Consequence.MEDIUM, FailureState.NONE))
                .as("confidence never compensates for missing evidence").isEqualTo(Route.ESCALATE);
        assertThat(SufficiencyRouter.route(Sufficiency.INSUFFICIENT, null, Consequence.LOW, FailureState.NONE))
                .isEqualTo(Route.ANSWER_WITH_LIMITS);
    }

    @Test
    void partialOrWeakResultsGoToAPersonWhenHighConsequence() {
        assertThat(SufficiencyRouter.route(Sufficiency.PARTIAL, 0.9, Consequence.HIGH, FailureState.NONE))
                .isEqualTo(Route.HUMAN_REVIEW);
        assertThat(SufficiencyRouter.route(Sufficiency.SUFFICIENT, 0.3, Consequence.CRITICAL, FailureState.NONE))
                .isEqualTo(Route.HUMAN_REVIEW);
        assertThat(SufficiencyRouter.route(Sufficiency.SUFFICIENT, 0.3, Consequence.LOW, FailureState.NONE))
                .isEqualTo(Route.ANSWER_WITH_LIMITS);
    }

    @Test
    void failuresAreNeverSilentlyDegraded() {
        assertThat(SufficiencyRouter.route(Sufficiency.SUFFICIENT, 1.0, Consequence.MEDIUM, FailureState.FAILED))
                .isEqualTo(Route.ESCALATE);
        assertThat(SufficiencyRouter.route(null, null, Consequence.LOW, FailureState.FAILED)).isEqualTo(Route.FALLBACK);
        assertThat(SufficiencyRouter.route(null, null, Consequence.LOW, FailureState.BLOCKED)).isEqualTo(Route.BLOCK);
        assertThat(SufficiencyRouter.route(Sufficiency.SUFFICIENT, 1.0, Consequence.HIGH, FailureState.DEGRADED))
                .isEqualTo(Route.HUMAN_REVIEW);
    }
}
