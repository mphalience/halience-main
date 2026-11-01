package ai.halience.aql.primitive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An immutable snapshot of all primitives, looked up by type and name. Agents and skills are both behaviours but have
 * separate namespaces (an agent and a skill may not share a name either, to keep references unambiguous).
 */
public final class PrimitiveSet {

    private final Map<String, Primitive> byKey;

    public PrimitiveSet(Collection<Primitive> primitives) {
        Map<String, Primitive> map = new LinkedHashMap<>();
        primitives.stream().sorted(Comparator.comparing(Primitive::key)).forEach(p -> map.put(p.key(), p));
        this.byKey = java.util.Collections.unmodifiableMap(map);
    }

    public static PrimitiveSet of(Primitive... primitives) {
        return new PrimitiveSet(List.of(primitives));
    }

    public Collection<Primitive> all() {
        return byKey.values();
    }

    public Optional<Primitive> byKey(String key) {
        return Optional.ofNullable(byKey.get(key));
    }

    public List<Primitive> ofType(PrimitiveType type) {
        return byKey.values().stream().filter(p -> p.type() == type).toList();
    }

    public List<Primitive> agents() {
        return byKey.values().stream().filter(Primitive::isAgent).toList();
    }

    public List<Primitive> skills() {
        return byKey.values().stream().filter(Primitive::isSkill).toList();
    }

    public Optional<Primitive> agent(String name) {
        return agents().stream().filter(p -> p.name().equals(name)).findFirst();
    }

    public Optional<Primitive> skill(String name) {
        return skills().stream().filter(p -> p.name().equals(name)).findFirst();
    }

    public Optional<Primitive> named(PrimitiveType type, String name) {
        return ofType(type).stream().filter(p -> p.name().equals(name)).findFirst();
    }

    /** The agents directly in {@code behaviours/<folder>/}. */
    public List<Primitive> agentsIn(String folder) {
        return agents().stream().filter(p -> p.folder().equals(folder)).toList();
    }

    /** A copy of this set with {@code changed} added or replacing the primitive with the same key. */
    public PrimitiveSet with(Primitive changed) {
        List<Primitive> next = new ArrayList<>(byKey.values());
        next.removeIf(p -> p.key().equals(changed.key()));
        next.add(changed);
        return new PrimitiveSet(next);
    }

    public int size() {
        return byKey.size();
    }
}
