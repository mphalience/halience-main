package ai.halience.aql.config;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import ai.halience.aql.llm.Thinking;
import ai.halience.aql.spec.Vocabulary.Consequence;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings under {@code halience.*}.
 *
 * @param brain the brain: which phase runs, where primitives come from, who administers them
 * @param llm   which LLMs each thinking level uses
 */
@ConfigurationProperties(prefix = "halience")
public record BrainProperties(Brain brain, Llm llm) {

    public BrainProperties {
        brain = brain == null ? Brain.defaults() : brain;
        llm = llm == null ? Llm.defaults() : llm;
    }

    public static BrainProperties defaults() {
        return new BrainProperties(Brain.defaults(), Llm.defaults());
    }

    /**
     * @param maxPhase            primitives in a later phase load and validate but don't run (1 for Phase 1)
     * @param primitives          where the starter primitives are, as a Spring resource pattern
     * @param seedOnStartup       whether to seed/refresh stored primitives from the starters at startup
     * @param adminRole           the role Halience staff admins hold; only they approve primitive edits
     * @param approvalThreshold   for tools with {@code approval: policy}, the consequence at or above which approval
     *                            is required
     * @param maxStepsPerRun      the default cap on model calls for a behaviour run
     * @param maxCostCentsPerRun  the default approximate cost cap for a behaviour run
     */
    public record Brain(int maxPhase, String primitives, boolean seedOnStartup, String adminRole,
                        Consequence approvalThreshold, int maxStepsPerRun, int maxCostCentsPerRun) {

        public Brain {
            maxPhase = maxPhase <= 0 ? 1 : maxPhase;
            primitives = primitives == null || primitives.isBlank() ? "classpath*:brain/**/*.md" : primitives;
            adminRole = adminRole == null || adminRole.isBlank() ? "admin" : adminRole;
            approvalThreshold = approvalThreshold == null ? Consequence.MEDIUM : approvalThreshold;
            maxStepsPerRun = maxStepsPerRun <= 0 ? 12 : maxStepsPerRun;
            maxCostCentsPerRun = maxCostCentsPerRun <= 0 ? 200 : maxCostCentsPerRun;
        }

        public static Brain defaults() {
            return new Brain(1, null, true, null, null, 0, 0);
        }
    }

    /**
     * @param thinking for each thinking level, the models to try in order, as {@code provider/model} or
     *                 {@code provider} (let the router pick that provider's model)
     */
    public record Llm(Map<Thinking, List<String>> thinking) {

        public Llm {
            Map<Thinking, List<String>> merged = new EnumMap<>(DEFAULT_THINKING);
            if (thinking != null) {
                thinking.forEach((level, models) -> {
                    if (models != null && !models.isEmpty()) {
                        merged.put(level, List.copyOf(models));
                    }
                });
            }
            thinking = Map.copyOf(merged);
        }

        public static Llm defaults() {
            return new Llm(null);
        }
    }

    /** Sensible defaults from the router's capability table; override in application.yml. */
    public static final Map<Thinking, List<String>> DEFAULT_THINKING = Map.of(
            Thinking.LOW, List.of("anthropic/claude-haiku-5-5", "openai/gpt-6-luna", "openrouter/google/gemini-3.8-flash"),
            Thinking.MEDIUM, List.of("anthropic/claude-sonnet-5-5", "openai/gpt-6.1-sol", "openrouter/google/gemini-4-argon"),
            Thinking.HIGH, List.of("anthropic/claude-opus-5-5", "openai/gpt-6-astra", "anthropic/claude-fable-5-1"));
}
