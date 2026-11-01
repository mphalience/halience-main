package ai.halience.aql.routine;

import java.util.List;

import ai.halience.aql.primitive.Primitive;
import ai.halience.aql.primitive.PrimitiveType;

/**
 * A routine: a regular check that runs one behaviour on a cadence (TR-06). The behaviour may in turn use knowledge,
 * tools and journeys; the routine itself carries no authority beyond the behaviour's.
 */
public record RoutineDefinition(String name, String description, int phase, Cadence cadence, String behaviour,
                                String trigger, List<String> loops, String scope, List<String> signals,
                                String instructions) {

    public static RoutineDefinition of(Primitive primitive) {
        if (primitive.type() != PrimitiveType.ROUTINE) {
            throw new IllegalArgumentException(primitive.key() + " is not a routine");
        }
        String behaviour = primitive.field("behaviour");
        if (behaviour == null || behaviour.isBlank()) {
            throw new IllegalArgumentException("a routine needs behaviour");
        }
        String cadence = primitive.field("cadence");
        if (cadence == null) {
            throw new IllegalArgumentException("a routine needs cadence");
        }
        return new RoutineDefinition(primitive.name(), primitive.description(), primitive.phase(),
                Cadence.fromWire(cadence), behaviour.strip(), primitive.frontMatter().getOrDefault("trigger", "TR-06"),
                primitive.list("loops"), primitive.frontMatter().getOrDefault("scope", "system"),
                primitive.list("signals"), primitive.body());
    }
}
