package ai.halience.aql.journey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ai.halience.aql.primitive.FrontMatter;
import ai.halience.aql.primitive.Primitive;
import ai.halience.aql.primitive.PrimitiveType;
import ai.halience.aql.spec.Vocabulary.ApprovalMode;

/**
 * A journey: a chain of dependent milestones toward one goal. The body holds one {@code ## milestone_key} section per
 * milestone, each with {@code - field: value} bullets (title, after, behaviour, loop, done_when, approval, phase, why).
 */
public record JourneyDefinition(String name, String description, int phase, String goal, List<String> loops,
                                String chain, List<Milestone> milestones) {

    private static final Pattern HEADING = Pattern.compile("^##\\s+(\\S+)\\s*$");
    private static final Pattern BULLET = Pattern.compile("^[-*]\\s+([a-z_]+)\\s*:\\s*(.*)$");
    private static final Pattern KEY = Pattern.compile("^[a-z0-9_]{1,64}$");

    /** What marks a milestone complete. */
    public enum DoneWhen { OUTPUT, APPROVAL, OUTCOME, HUMAN }

    public record Milestone(String key, String title, List<String> after, String behaviour, String loop,
                            DoneWhen doneWhen, ApprovalMode approval, int phase, String why) {
    }

    public static JourneyDefinition of(Primitive primitive) {
        if (primitive.type() != PrimitiveType.JOURNEY) {
            throw new IllegalArgumentException(primitive.key() + " is not a journey");
        }
        List<Milestone> milestones = parseMilestones(primitive.body(), primitive.phase());
        if (milestones.isEmpty()) {
            throw new IllegalArgumentException("a journey needs at least one '## milestone' section");
        }
        return new JourneyDefinition(primitive.name(), primitive.description(), primitive.phase(),
                primitive.frontMatter().getOrDefault("goal", primitive.description()), primitive.list("loops"),
                primitive.field("chain"), milestones);
    }

    static List<Milestone> parseMilestones(String body, int journeyPhase) {
        List<Milestone> milestones = new ArrayList<>();
        String key = null;
        Map<String, String> fields = new LinkedHashMap<>();
        for (String raw : body.replace("\r\n", "\n").split("\n")) {
            String line = raw.strip();
            Matcher heading = HEADING.matcher(line);
            if (heading.matches()) {
                if (key != null) {
                    milestones.add(milestone(key, fields, journeyPhase));
                }
                key = heading.group(1);
                fields = new LinkedHashMap<>();
                continue;
            }
            Matcher bullet = BULLET.matcher(line);
            if (key != null && bullet.matches()) {
                if (fields.putIfAbsent(bullet.group(1), bullet.group(2).strip()) != null) {
                    throw new IllegalArgumentException("milestone " + key + " repeats field " + bullet.group(1));
                }
            }
        }
        if (key != null) {
            milestones.add(milestone(key, fields, journeyPhase));
        }
        return milestones;
    }

    private static Milestone milestone(String key, Map<String, String> fields, int journeyPhase) {
        if (!KEY.matcher(key).matches()) {
            throw new IllegalArgumentException("milestone key '" + key + "' must match " + KEY.pattern());
        }
        String doneWhen = fields.getOrDefault("done_when", "output").split("\\|")[0].strip();
        String phase = fields.get("phase");
        int milestonePhase = phase == null ? journeyPhase : Integer.parseInt(phase.strip());
        if (milestonePhase < journeyPhase) {
            throw new IllegalArgumentException("milestone " + key + " can't be in an earlier phase than its journey");
        }
        return new Milestone(key,
                fields.getOrDefault("title", key),
                FrontMatter.splitList(fields.get("after")),
                blankToNull(fields.get("behaviour")),
                blankToNull(fields.get("loop")),
                parse(DoneWhen.class, "done_when", doneWhen, key),
                parse(ApprovalMode.class, "approval", fields.getOrDefault("approval", "none"), key),
                milestonePhase,
                fields.getOrDefault("why", ""));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String field, String value, String milestone) {
        try {
            return Enum.valueOf(type, value.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("milestone " + milestone + ": " + field + " has unknown value '" + value
                    + "'");
        }
    }

    public Milestone milestone(String key) {
        return milestones.stream().filter(m -> m.key().equals(key)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("journey " + name + " has no milestone " + key));
    }
}
