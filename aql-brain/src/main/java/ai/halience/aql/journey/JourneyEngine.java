package ai.halience.aql.journey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ai.halience.aql.journey.JourneyDefinition.Milestone;

/**
 * Pure journey logic: checks a journey's milestones form a DAG, and works out which milestones are available next
 * given those completed and the phase the deployment runs. Milestones in a later phase than {@code maxPhase} are
 * skipped (treated as not applicable), so a Phase 1 deployment can still finish a journey that has Phase 2 steps.
 */
public final class JourneyEngine {

    private JourneyEngine() {
    }

    /** Problems with the journey's structure: unknown {@code after} references, self-references and cycles. */
    public static List<String> structuralProblems(JourneyDefinition journey) {
        List<String> problems = new ArrayList<>();
        Map<String, Milestone> byKey = new HashMap<>();
        for (Milestone m : journey.milestones()) {
            if (byKey.put(m.key(), m) != null) {
                problems.add("milestone " + m.key() + " is defined twice");
            }
        }
        for (Milestone m : journey.milestones()) {
            for (String dependency : m.after()) {
                if (dependency.equals(m.key())) {
                    problems.add("milestone " + m.key() + " comes after itself");
                } else if (!byKey.containsKey(dependency)) {
                    problems.add("milestone " + m.key() + " comes after unknown milestone " + dependency);
                }
            }
        }
        cycle(byKey).ifPresent(path -> problems.add("milestones form a cycle: " + String.join(" -> ", path)));
        return problems;
    }

    private static Optional<List<String>> cycle(Map<String, Milestone> byKey) {
        Map<String, Integer> state = new HashMap<>(); // 1 = visiting, 2 = done
        for (String start : byKey.keySet()) {
            List<String> path = new ArrayList<>();
            if (visit(start, byKey, state, path)) {
                return Optional.of(path);
            }
        }
        return Optional.empty();
    }

    private static boolean visit(String key, Map<String, Milestone> byKey, Map<String, Integer> state,
            List<String> path) {
        Integer s = state.get(key);
        if (s != null && s == 2) {
            return false;
        }
        path.add(key);
        if (s != null && s == 1) {
            return true;
        }
        state.put(key, 1);
        Milestone m = byKey.get(key);
        if (m != null) {
            for (String dependency : m.after()) {
                if (byKey.containsKey(dependency) && visit(dependency, byKey, state, path)) {
                    return true;
                }
            }
        }
        state.put(key, 2);
        path.remove(path.size() - 1);
        return false;
    }

    /** Milestones applicable to a deployment running phases up to {@code maxPhase}. */
    public static List<Milestone> applicable(JourneyDefinition journey, int maxPhase) {
        return journey.milestones().stream().filter(m -> m.phase() <= maxPhase).toList();
    }

    /**
     * The milestones that can start now: applicable, not yet completed, and whose applicable dependencies are all
     * completed. A dependency in a later phase than {@code maxPhase} is treated as satisfied.
     */
    public static List<Milestone> available(JourneyDefinition journey, Set<String> completed, int maxPhase) {
        Set<String> applicableKeys = new HashSet<>();
        applicable(journey, maxPhase).forEach(m -> applicableKeys.add(m.key()));
        return applicable(journey, maxPhase).stream()
                .filter(m -> !completed.contains(m.key()))
                .filter(m -> m.after().stream().allMatch(d -> completed.contains(d) || !applicableKeys.contains(d)))
                .toList();
    }

    /** Whether every applicable milestone is completed. */
    public static boolean isComplete(JourneyDefinition journey, Set<String> completed, int maxPhase) {
        return applicable(journey, maxPhase).stream().allMatch(m -> completed.contains(m.key()));
    }
}
