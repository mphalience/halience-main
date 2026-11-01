package ai.halience.aql.score;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import ai.halience.aql.score.ScoreEngine.Dimension;
import ai.halience.aql.score.ScoreEngine.Methodology;
import ai.halience.aql.score.ScoreEngine.Result;
import ai.halience.aql.score.ScoreEngine.ScoreKind;
import org.junit.jupiter.api.Test;

class ScoreEngineTest {

    static final Methodology PROPERTY = new Methodology("property_score", "v0", ScoreKind.PROPERTY,
            List.of(new Dimension("condition", 2), new Dimension("price_value", 2), new Dimension("risk", 1)), 0.6);

    @Test
    void weightsTheDimensionsThatHaveEvidence() {
        Result r = ScoreEngine.score(PROPERTY, Map.of("condition", 80.0, "price_value", 60.0, "risk", 100.0));
        assertThat(r.score()).isEqualTo(76.0);
        assertThat(r.coverage()).isEqualTo(1.0);
        assertThat(r.sufficient()).isTrue();
    }

    @Test
    void missingEvidenceLowersCoverageNotTheScore() {
        Result r = ScoreEngine.score(PROPERTY, Map.of("condition", 80.0, "price_value", 60.0));
        assertThat(r.score()).isEqualTo(70.0); // not dragged down as if risk were 0
        assertThat(r.coverage()).isEqualTo(0.8);
        assertThat(r.missing()).containsExactly("risk");
    }

    @Test
    void belowMinimumCoverageThereIsNoScore() {
        Result r = ScoreEngine.score(PROPERTY, Map.of("risk", 90.0));
        assertThat(r.score()).isNull();
        assertThat(r.sufficient()).isFalse();
        assertThat(r.breakdown()).containsEntry("risk", 90.0);
    }

    @Test
    void affordabilityCanOnlyBeASettleScoreDimension() {
        assertThatThrownBy(() -> new Methodology("property_score", "v0", ScoreKind.PROPERTY,
                List.of(new Dimension("affordability", 1)), 0.5)).hasMessageContaining("only be a Settle Score");
        assertThatThrownBy(() -> new Methodology("area_score", "v0", ScoreKind.AREA,
                List.of(new Dimension("affordability", 1)), 0.5)).isInstanceOf(IllegalArgumentException.class);
        Methodology settle = new Methodology("settle_score", "v0", ScoreKind.SETTLE,
                List.of(new Dimension("affordability", 3), new Dimension("must_haves", 2)), 1.0);
        assertThat(ScoreEngine.score(settle, Map.of("must_haves", 90.0)).score())
                .as("no Settle Score before affordability is sufficiently known").isNull();
    }

    @Test
    void rejectsUnknownDimensionsAndOutOfRangeValues() {
        assertThatThrownBy(() -> ScoreEngine.score(PROPERTY, Map.of("vibes", 50.0))).hasMessageContaining("not a dimension");
        assertThatThrownBy(() -> ScoreEngine.score(PROPERTY, Map.of("risk", 140.0))).hasMessageContaining("between 0 and 100");
    }
}
