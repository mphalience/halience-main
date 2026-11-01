package ai.halience.aql.score;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PC-07 Score &amp; Rank as a transparent, deterministic weighted score. AQL owns the mechanics; the domain owns the
 * methodology (dimensions, weights, minimum coverage). Rules from the spec:
 * <ul>
 *   <li>missing evidence lowers confidence (coverage); it never silently counts as a low value;</li>
 *   <li>below the methodology's minimum coverage there is no score at all, only the breakdown of what is known;</li>
 *   <li>each methodology declares which score it is; affordability may only be a dimension of the Settle Score, never
 *       of the Property or Area Score (GV-14).</li>
 * </ul>
 */
public final class ScoreEngine {

    /** The three top-level Settle scores, plus any other domain score. */
    public enum ScoreKind { PROPERTY, AREA, SETTLE, OTHER }

    public static final String AFFORDABILITY = "affordability";

    public record Dimension(String name, double weight) {
        public Dimension {
            if (weight <= 0) {
                throw new IllegalArgumentException("dimension " + name + " needs a positive weight");
            }
        }
    }

    /**
     * @param minimumCoverage the share of total weight that must have values before a score is produced (0–1)
     */
    public record Methodology(String name, String version, ScoreKind kind, List<Dimension> dimensions,
                              double minimumCoverage) {

        public Methodology {
            dimensions = List.copyOf(dimensions);
            if (dimensions.isEmpty()) {
                throw new IllegalArgumentException("a methodology needs at least one dimension");
            }
            if (kind != ScoreKind.SETTLE && dimensions.stream().anyMatch(d -> d.name().equals(AFFORDABILITY))) {
                throw new IllegalArgumentException("affordability is personal context: it can only be a Settle Score "
                        + "dimension, never part of a " + kind + " score");
            }
            if (minimumCoverage < 0 || minimumCoverage > 1) {
                throw new IllegalArgumentException("minimum coverage must be between 0 and 1");
            }
        }
    }

    /**
     * @param score     the weighted score on 0–100 over the dimensions that have values, or null when coverage is
     *                  below the methodology's minimum
     * @param coverage  the share of total weight with values (0–1); the confidence signal for the score
     */
    public record Result(String methodology, String version, ScoreKind kind, Double score, double coverage,
                         Map<String, Double> breakdown, List<String> missing, boolean sufficient) {

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("methodology", methodology + "@" + version);
            map.put("kind", kind.name());
            map.put("score", score);
            map.put("coverage", coverage);
            map.put("breakdown", breakdown);
            map.put("missing", missing);
            map.put("sufficient", sufficient);
            return map;
        }
    }

    private ScoreEngine() {
    }

    /** @param values dimension name to value on 0–100; absent or null means no evidence for that dimension */
    public static Result score(Methodology m, Map<String, Double> values) {
        double totalWeight = m.dimensions().stream().mapToDouble(Dimension::weight).sum();
        double coveredWeight = 0;
        double weighted = 0;
        Map<String, Double> breakdown = new LinkedHashMap<>();
        List<String> missing = new ArrayList<>();
        for (Dimension d : m.dimensions()) {
            Double value = values.get(d.name());
            if (value == null) {
                missing.add(d.name());
                continue;
            }
            if (value < 0 || value > 100) {
                throw new IllegalArgumentException("dimension " + d.name() + " must be between 0 and 100");
            }
            breakdown.put(d.name(), value);
            coveredWeight += d.weight();
            weighted += value * d.weight();
        }
        for (String name : values.keySet()) {
            if (m.dimensions().stream().noneMatch(d -> d.name().equals(name))) {
                throw new IllegalArgumentException("'" + name + "' is not a dimension of " + m.name());
            }
        }
        double coverage = round(coveredWeight / totalWeight);
        boolean sufficient = coveredWeight > 0 && coverage >= m.minimumCoverage();
        Double score = sufficient ? round(weighted / coveredWeight) : null;
        return new Result(m.name(), m.version(), m.kind(), score, coverage, breakdown, missing, sufficient);
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
