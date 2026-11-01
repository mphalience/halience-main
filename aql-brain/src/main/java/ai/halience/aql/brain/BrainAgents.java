package ai.halience.aql.brain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ai.halience.agent.loop.AgentLoop;
import ai.halience.agent.loop.agent.Agent;
import ai.halience.agent.loop.agent.AgentRuntime;
import ai.halience.agent.loop.skill.Skill;
import ai.halience.aql.governance.GovernanceException;
import ai.halience.aql.llm.ThinkingRoutes;
import ai.halience.aql.primitive.BehaviourSpec;
import ai.halience.aql.primitive.Primitive;
import ai.halience.aql.primitive.PrimitiveLibrary;
import ai.halience.aql.primitive.PrimitiveSet;
import ai.halience.aql.primitive.PrimitiveType;
import ai.halience.aql.tool.ToolCatalogue;
import org.springframework.stereotype.Component;

/**
 * Builds runnable agents from behaviour primitives and runs them on one shared {@link AgentRuntime}:
 * <ul>
 *   <li>the body becomes the agent's instructions;</li>
 *   <li>{@code thinking} picks the model route ({@link ThinkingRoutes});</li>
 *   <li>{@code skills} are included in full on every call; {@code knowledge} is offered on demand;</li>
 *   <li>{@code tools} come from the {@link ToolCatalogue}, gated;</li>
 *   <li>{@code delegates} folders become agents it can hand work to. Delegates are built one level deep (a delegate's
 *       own delegates are not offered), and the {@code admin} folder is only offered when the run is for a Halience
 *       admin.</li>
 * </ul>
 * Disabled (later-phase) skills, knowledge, tools and delegates are left out. Agents are rebuilt whenever the library
 * reloads.
 */
@Component
public class BrainAgents {

    public static final String ADMIN_FOLDER = "admin";

    private final PrimitiveLibrary library;
    private final ToolCatalogue tools;
    private final ThinkingRoutes routes;
    private final AgentRuntime runtime;
    private final Map<String, Agent> cache = new ConcurrentHashMap<>();
    private int cachedGeneration = -1;

    public BrainAgents(PrimitiveLibrary library, ToolCatalogue tools, ThinkingRoutes routes, AgentLoop baseLoop) {
        this.library = library;
        this.tools = tools;
        this.routes = routes;
        this.runtime = new AgentRuntime(baseLoop);
    }

    public AgentRuntime runtime() {
        return runtime;
    }

    /** The agent for behaviour {@code name}; {@code admin} offers the admin delegates. */
    public Agent agent(String name, boolean admin) {
        synchronized (this) {
            if (library.generation() != cachedGeneration) {
                cache.clear();
                runtime.clear();
                cachedGeneration = library.generation();
            }
        }
        String key = name + (admin ? "|admin" : "");
        Agent existing = cache.get(key);
        if (existing != null) {
            return existing;
        }
        Agent built = build(name, admin, true);
        Agent raced = cache.putIfAbsent(key, built);
        return raced != null ? raced : built;
    }

    public Primitive requireEnabled(String name) {
        Primitive p = library.current().agent(name)
                .orElseThrow(() -> new GovernanceException(GovernanceException.Reason.NOT_FOUND,
                        "No behaviour " + name));
        if (!library.isEnabled(p)) {
            throw new GovernanceException(GovernanceException.Reason.DISABLED, "Behaviour " + name + " is phase "
                    + p.phase() + " and not enabled in this deployment");
        }
        return p;
    }

    private Agent build(String name, boolean admin, boolean withDelegates) {
        Primitive primitive = requireEnabled(name);
        BehaviourSpec spec = BehaviourSpec.of(primitive);
        PrimitiveSet set = library.current();
        Agent.Builder builder = Agent.builder(primitive.body())
                .name(spec.name())
                .description(primitive.description())
                .routerConfig(routes.routerConfig(spec.thinking()));
        if (spec.planMode() != null) {
            builder.planMode(spec.planMode());
        }
        if (spec.maxSteps() != null) {
            builder.maxSteps(spec.maxSteps());
        }
        List<Skill> skills = new ArrayList<>();
        for (String skillName : spec.skills()) {
            set.skill(skillName).filter(library::isEnabled).ifPresent(s -> skills.add(skill(s)));
        }
        builder.skills(skills.toArray(Skill[]::new));
        List<Skill> knowledge = new ArrayList<>();
        for (String knowledgeName : spec.knowledge()) {
            set.named(PrimitiveType.KNOWLEDGE, knowledgeName).filter(library::isEnabled)
                    .ifPresent(k -> knowledge.add(skill(k)));
        }
        builder.onDemandSkills(knowledge);
        builder.tools(tools.toolsFor(spec));
        if (withDelegates) {
            for (String folder : spec.delegates()) {
                if (ADMIN_FOLDER.equals(folder) && !admin) {
                    continue;
                }
                for (Primitive delegate : set.agentsIn(folder)) {
                    if (delegate.name().equals(name) || !library.isEnabled(delegate)) {
                        continue;
                    }
                    builder.delegateTo(runtime.delegate(build(delegate.name(), admin, false)));
                }
            }
        }
        return builder.build();
    }

    private static Skill skill(Primitive p) {
        return new Skill(p.name(), p.description(), p.body(), List.of());
    }
}
