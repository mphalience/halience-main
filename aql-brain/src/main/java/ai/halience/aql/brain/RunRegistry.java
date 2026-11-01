package ai.halience.aql.brain;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ai.halience.aql.context.ContextEnvelope;
import ai.halience.aql.domain.Ids;
import org.springframework.stereotype.Component;

/**
 * The state of brain runs in flight, keyed by the run id carried in the agent loop's run tags (which delegated runs
 * inherit). Tools find whose run they are serving here, and record the evidence refs the run has seen so the grounding
 * check can tell retrieved provenance from invented provenance.
 */
@Component
public class RunRegistry {

    public static final String RUN_TAG = "aql_run";

    public static final class RunState {
        private final String runId;
        private final ContextEnvelope context;
        private final Set<String> seenRefs = ConcurrentHashMap.newKeySet();

        RunState(String runId, ContextEnvelope context) {
            this.runId = runId;
            this.context = context;
        }

        public String runId() {
            return runId;
        }

        public ContextEnvelope context() {
            return context;
        }

        public Set<String> seenRefs() {
            return Set.copyOf(seenRefs);
        }

        public void saw(String ref) {
            if (ref != null && !ref.isBlank()) {
                seenRefs.add(ref);
            }
        }
    }

    private final Map<String, RunState> runs = new ConcurrentHashMap<>();

    public RunState open(ContextEnvelope context) {
        String id = Ids.next("run");
        RunState state = new RunState(id, context);
        runs.put(id, state);
        // Everything in the run's own context counts as seen.
        context.referents().forEach(r -> state.saw(String.valueOf(r.get("ref"))));
        context.activeContext().values().forEach(v -> state.saw(String.valueOf(v)));
        return state;
    }

    public Optional<RunState> find(Map<String, String> tags) {
        if (tags == null) {
            return Optional.empty();
        }
        String id = tags.get(RUN_TAG);
        return id == null ? Optional.empty() : Optional.ofNullable(runs.get(id));
    }

    public void close(String runId) {
        runs.remove(runId);
    }
}
