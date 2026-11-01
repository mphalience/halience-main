package ai.halience.aql.primitive;

import java.util.List;
import java.util.Locale;

import ai.halience.agent.loop.PlanMode;
import ai.halience.aql.llm.Thinking;
import ai.halience.aql.spec.Authority;

/**
 * The typed front matter of an agent behaviour. Spec codes are kept as strings here and checked by
 * {@link PrimitiveValidator}, so a single bad code produces a clear validation issue rather than a parse failure.
 */
public record BehaviourSpec(String name, String folder, int phase, Thinking thinking, Authority authority,
                            List<String> promptClasses, List<String> activities, List<String> loops,
                            List<String> outputs, List<String> context, List<String> signals, List<String> triggers,
                            List<String> governance, List<String> consumers, List<String> skills,
                            List<String> knowledge, List<String> tools, List<String> delegates, boolean background,
                            PlanMode planMode, Integer maxSteps) {

    public static final String BACKGROUND = "background";

    public static BehaviourSpec of(Primitive primitive) {
        if (!primitive.isAgent()) {
            throw new IllegalArgumentException(primitive.key() + " is not an agent behaviour");
        }
        String runs = primitive.frontMatter().getOrDefault("runs", "inline").toLowerCase(Locale.ROOT);
        if (!runs.equals("inline") && !runs.equals(BACKGROUND)) {
            throw new IllegalArgumentException("runs must be inline or background; was '" + runs + "'");
        }
        return new BehaviourSpec(
                primitive.name(),
                primitive.folder(),
                primitive.phase(),
                Thinking.fromWire(primitive.field("thinking")),
                Authority.fromWire(primitive.frontMatter().getOrDefault("authority", "read")),
                primitive.list("prompt_classes"),
                primitive.list("activities"),
                primitive.list("loops"),
                primitive.list("outputs"),
                primitive.list("context"),
                primitive.list("signals"),
                primitive.list("triggers"),
                primitive.list("governance"),
                primitive.list("consumers"),
                primitive.list("skills"),
                primitive.list("knowledge"),
                primitive.list("tools"),
                primitive.list("delegates"),
                runs.equals(BACKGROUND),
                planMode(primitive.field("plan_mode")),
                maxSteps(primitive.field("max_steps")));
    }

    private static PlanMode planMode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return PlanMode.valueOf(value.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "plan_mode must be auto, always_plan, never_plan or recursive_on_each_step; was '" + value + "'");
        }
    }

    private static Integer maxSteps(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            int steps = Integer.parseInt(value.strip());
            if (steps < 1) {
                throw new IllegalArgumentException("max_steps must be at least 1; was " + value);
            }
            return steps;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("max_steps must be a whole number; was '" + value + "'");
        }
    }
}
