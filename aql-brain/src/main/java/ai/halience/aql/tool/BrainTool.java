package ai.halience.aql.tool;

import java.util.List;
import java.util.Map;

import ai.halience.agent.loop.tool.ToolInputException;

/**
 * A tool implemented in this application, bound by name to a {@code kind: brain} tool primitive. The primitive carries
 * the governance (authority, consequence, approval) and the guidance the model reads; the Java side carries the
 * parameters and the behaviour. {@link ToolCatalogue} wraps every call in the gate, so implementations can assume the
 * caller is permitted to call the tool at all, but must still check permission on the records they touch.
 */
public interface BrainTool {

    String name();

    /** JSON schema properties for the tool's parameters. */
    Map<String, Object> properties();

    /** Required parameter names. */
    default List<String> required() {
        return List.of();
    }

    /** Runs the call. Throw {@link ToolInputException} for a problem the model can fix. */
    String call(Map<String, Object> arguments, BrainCall call);

    /** Builds a simple brain tool from a lambda. */
    static BrainTool of(String name, Map<String, Object> properties, List<String> required, Handler handler) {
        return new BrainTool() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public Map<String, Object> properties() {
                return properties;
            }

            @Override
            public List<String> required() {
                return required;
            }

            @Override
            public String call(Map<String, Object> arguments, BrainCall call) {
                return handler.call(arguments, call);
            }
        };
    }

    @FunctionalInterface
    interface Handler {
        String call(Map<String, Object> arguments, BrainCall call);
    }

    /** Schema helpers. */
    static Map<String, Object> str(String description) {
        return Map.of("type", "string", "description", description);
    }

    static Map<String, Object> num(String description) {
        return Map.of("type", "number", "description", description);
    }

    static Map<String, Object> strs(String description) {
        return Map.of("type", "array", "items", Map.of("type", "string"), "description", description);
    }

    static Map<String, Object> obj(String description) {
        return Map.of("type", "object", "description", description);
    }

    static Map<String, Object> bool(String description) {
        return Map.of("type", "boolean", "description", description);
    }

    static ToolInputException input(String message) {
        return new ToolInputException(message);
    }
}
