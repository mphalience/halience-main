package ai.halience.agent.loop.plan;

import java.util.List;

/** An ordered sequence of steps an {@link ai.halience.agent.loop.AgentLoop} run should follow. */
public record Plan(List<PlanStep> steps) {

    public Plan {
        steps = List.copyOf(steps);
    }
}
