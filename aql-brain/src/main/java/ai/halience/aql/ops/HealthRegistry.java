package ai.halience.aql.ops;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ai.halience.aql.domain.SignalEvent;
import ai.halience.aql.spec.Vocabulary.FailureState;
import org.springframework.stereotype.Component;

/**
 * Current health of capabilities, sources, integrations and models (PA-032, SG-002, SG-028), fed by operational signals
 * and by the runtime itself (failed model calls, tool failures). It lets degradation be explicit and lets routing pick
 * a fallback rather than silently degrading (GV-07). In memory for the first cut; a real deployment would back it with
 * telemetry.
 */
@Component
public class HealthRegistry {

    public record Health(String capability, FailureState state, String detail, Instant since, Instant lastSeen) {
    }

    private final Map<String, Health> health = new ConcurrentHashMap<>();
    private final Clock clock;

    public HealthRegistry(Clock clock) {
        this.clock = clock;
    }

    public void report(String capability, FailureState state, String detail) {
        Instant now = Instant.now(clock);
        health.compute(capability, (k, previous) -> new Health(capability, state, detail,
                previous != null && previous.state() == state ? previous.since() : now, now));
    }

    /** Records an operational signal against the entities it names. */
    public void record(SignalEvent event) {
        FailureState state = switch (String.valueOf(event.getSignalClass())) {
            case "SG-028" -> FailureState.FAILED;
            case "SG-002" -> FailureState.DEGRADED;
            default -> FailureState.NONE;
        };
        for (String entity : event.getEntityRefs()) {
            report(entity, state, event.getEventType());
        }
    }

    public FailureState state(String capability) {
        Health h = health.get(capability);
        return h == null ? FailureState.UNKNOWN : h.state();
    }

    public Collection<Health> all() {
        return List.copyOf(health.values());
    }
}
