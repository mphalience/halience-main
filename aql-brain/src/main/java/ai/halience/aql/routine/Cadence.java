package ai.halience.aql.routine;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;

/** How often a routine runs. Due times are computed in UTC from the last run (or immediately if it never ran). */
public enum Cadence {
    HOURLY, DAILY, WEEKLY, MONTHLY, QUARTERLY;

    /** When a routine last run at {@code lastRun} is next due. */
    public Instant nextDue(Instant lastRun) {
        if (lastRun == null) {
            return Instant.EPOCH;
        }
        ZonedDateTime last = lastRun.atZone(ZoneOffset.UTC);
        return switch (this) {
            case HOURLY -> lastRun.plus(Duration.ofHours(1));
            case DAILY -> last.plusDays(1).toInstant();
            case WEEKLY -> last.plusWeeks(1).toInstant();
            case MONTHLY -> last.plusMonths(1).toInstant();
            case QUARTERLY -> last.plusMonths(3).toInstant();
        };
    }

    public boolean isDue(Instant lastRun, Instant now) {
        return !now.isBefore(nextDue(lastRun));
    }

    public static Cadence fromWire(String value) {
        try {
            return valueOf(value.strip().toUpperCase(Locale.ROOT));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("cadence must be hourly, daily, weekly, monthly or quarterly; was '"
                    + value + "'");
        }
    }
}
