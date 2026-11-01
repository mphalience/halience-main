package ai.halience.aql.halie;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import ai.halience.aql.governance.Actor;
import ai.halience.aql.governance.PermissionService;
import ai.halience.aql.spec.Vocabulary.Classification;

/**
 * The deterministic part of dynamic canvas composition (PA-040, OUT-20): place authorised items into the reusable
 * Attention, Work, Pulse and Team regions for the active context. Every item is permission-checked before it is placed,
 * and context narrows what is shown but never widens access. The composition is runtime-only state.
 */
public final class CanvasComposer {

    public enum Region { ATTENTION, WORK, PULSE, TEAM }

    /**
     * A candidate item for the canvas.
     *
     * @param scopeRef the scope the item belongs to; with no active context every visible item qualifies
     * @param status   canonical or working (provenance shown to the user)
     */
    public record Item(String ref, String label, Region region, String scopeRef, Classification classification,
                       String status, String reason) {
    }

    public record Canvas(Map<String, Object> activeContext, Map<Region, List<Item>> regions, List<String> excluded) {
    }

    private CanvasComposer() {
    }

    /**
     * @param activeScope the active scoped Context (e.g. {@code venture:settle}), or null for "All Halience"
     */
    public static Canvas compose(Actor actor, Map<String, Object> activeContext, String activeScope,
            List<Item> candidates, PermissionService permissions, int maxPerRegion) {
        Map<Region, List<Item>> regions = new EnumMap<>(Region.class);
        for (Region r : Region.values()) {
            regions.put(r, new ArrayList<>());
        }
        List<String> excluded = new ArrayList<>();
        for (Item item : candidates) {
            if (!permissions.canSee(actor, item.classification(), item.scopeRef())) {
                continue; // never reveal what the actor may not see, not even as an exclusion
            }
            if (activeScope != null && item.scopeRef() != null && !activeScope.equals(item.scopeRef())) {
                excluded.add(item.ref());
                continue;
            }
            List<Item> region = regions.get(item.region());
            if (region.size() < maxPerRegion) {
                region.add(item);
            } else {
                excluded.add(item.ref());
            }
        }
        return new Canvas(activeContext == null ? Map.of() : Map.copyOf(activeContext), regions, excluded);
    }
}
