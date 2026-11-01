package ai.halience.aql.routine;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import ai.halience.aql.brain.BrainRequest;
import ai.halience.aql.brain.BrainResult;
import ai.halience.aql.brain.BrainService;
import ai.halience.aql.context.ContextEnvelope;
import ai.halience.aql.domain.Repositories.RoutineStateRepository;
import ai.halience.aql.domain.RoutineState;
import ai.halience.aql.governance.Actor;
import ai.halience.aql.primitive.Primitive;
import ai.halience.aql.primitive.PrimitiveLibrary;
import ai.halience.aql.primitive.PrimitiveType;
import ai.halience.aql.spec.SpecCode;
import ai.halience.aql.spec.Trigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runs due routines (TR-06). Each enabled routine whose cadence has elapsed runs its behaviour as the system, with the
 * routine's instructions as the task. A routine never has more authority than its behaviour, and a failure is recorded
 * against the routine rather than retried in a tight loop.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "halience.routines", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RoutineScheduler {

    private final PrimitiveLibrary library;
    private final RoutineStateRepository states;
    private final BrainService brain;
    private final Clock clock;

    public RoutineScheduler(PrimitiveLibrary library, RoutineStateRepository states, BrainService brain, Clock clock) {
        this.library = library;
        this.states = states;
        this.brain = brain;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${halience.routines.tick:PT1M}", initialDelayString = "${halience.routines.initial-delay:PT2M}")
    public void tick() {
        Instant now = Instant.now(clock);
        for (RoutineDefinition routine : due(now)) {
            runOne(routine, now);
        }
    }

    /** The enabled routines due at {@code now}. */
    public List<RoutineDefinition> due(Instant now) {
        List<RoutineDefinition> due = new ArrayList<>();
        for (Primitive p : library.current().ofType(PrimitiveType.ROUTINE)) {
            if (!library.isEnabled(p)) {
                continue;
            }
            RoutineDefinition routine = RoutineDefinition.of(p);
            boolean behaviourEnabled = library.current().agent(routine.behaviour()).map(library::isEnabled)
                    .orElse(false);
            Instant last = states.findByRoutine(routine.name()).map(RoutineState::getLastRunAt).orElse(null);
            if (behaviourEnabled && routine.cadence().isDue(last, now)) {
                due.add(routine);
            }
        }
        return due;
    }

    void runOne(RoutineDefinition routine, Instant now) {
        RoutineState state = states.findByRoutine(routine.name()).orElseGet(() -> {
            RoutineState s = new RoutineState();
            s.setRoutine(routine.name());
            return s;
        });
        state.setLastRunAt(now);
        Actor system = Actor.system("routine:" + routine.name());
        Trigger trigger = SpecCode.parse(Trigger.class, routine.trigger()).orElse(Trigger.TR_06);
        ContextEnvelope context = ContextEnvelope.of(system, trigger, now)
                .withExtra("Routine", routine.name() + " (" + routine.cadence().name().toLowerCase() + ", scope "
                        + routine.scope() + ")");
        try {
            BrainResult result = brain.run(BrainRequest.of(routine.behaviour(), "Routine " + routine.name() + ": "
                    + routine.description() + "\n\n" + routine.instructions(), context));
            state.setLastStatus(result.envelope().failureState().name().toLowerCase());
            state.setLastOutputRef(result.outputId());
            state.setLastError(null);
        } catch (RuntimeException e) {
            log.warn("Routine {} failed: {}", routine.name(), e.toString());
            state.setLastStatus("failed");
            state.setLastError(e.getMessage());
        }
        states.save(state);
    }
}
