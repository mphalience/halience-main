package ai.halience.aql.routine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class CadenceTest {

    static final Instant LAST = Instant.parse("2026-01-31T08:00:00Z");

    @Test
    void neverRunIsDueImmediately() {
        assertThat(Cadence.WEEKLY.isDue(null, LAST)).isTrue();
    }

    @Test
    void nextDueByCadence() {
        assertThat(Cadence.HOURLY.nextDue(LAST)).isEqualTo(Instant.parse("2026-01-31T09:00:00Z"));
        assertThat(Cadence.DAILY.nextDue(LAST)).isEqualTo(Instant.parse("2026-02-01T08:00:00Z"));
        assertThat(Cadence.WEEKLY.nextDue(LAST)).isEqualTo(Instant.parse("2026-02-07T08:00:00Z"));
        assertThat(Cadence.MONTHLY.nextDue(LAST)).isEqualTo(Instant.parse("2026-02-28T08:00:00Z"));
        assertThat(Cadence.QUARTERLY.nextDue(LAST)).isEqualTo(Instant.parse("2026-04-30T08:00:00Z"));
    }

    @Test
    void isDueOnceTheIntervalHasPassed() {
        assertThat(Cadence.DAILY.isDue(LAST, Instant.parse("2026-02-01T07:59:59Z"))).isFalse();
        assertThat(Cadence.DAILY.isDue(LAST, Instant.parse("2026-02-01T08:00:00Z"))).isTrue();
    }

    @Test
    void parsesFromFrontMatter() {
        assertThat(Cadence.fromWire("Weekly")).isEqualTo(Cadence.WEEKLY);
        assertThatThrownBy(() -> Cadence.fromWire("sometimes")).hasMessageContaining("cadence must be");
    }
}
