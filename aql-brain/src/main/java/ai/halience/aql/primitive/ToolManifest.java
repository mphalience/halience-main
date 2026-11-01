package ai.halience.aql.primitive;

import java.util.Locale;

import ai.halience.aql.spec.Authority;
import ai.halience.aql.spec.Vocabulary.ApprovalMode;
import ai.halience.aql.spec.Vocabulary.Consequence;

/**
 * The typed front matter of a tool primitive: what kind of actuator it is, the authority it needs, its consequence and
 * whether it needs approval. The parameters and handler live in Java; the Markdown carries the governance and the
 * guidance the model sees.
 *
 * @param kind {@code brain} (a Java handler in this application), {@code builtin} (a tool from the agent loop library)
 *             or {@code http} (a generic API call configured by environment variables)
 */
public record ToolManifest(String name, String description, int phase, Kind kind, Authority authority,
                           Consequence consequence, ApprovalMode approval, boolean reversible, String method,
                           String baseUrlEnv, String authEnv, String guidance) {

    public enum Kind { BRAIN, BUILTIN, HTTP }

    public static ToolManifest of(Primitive primitive) {
        if (primitive.type() != PrimitiveType.TOOL) {
            throw new IllegalArgumentException(primitive.key() + " is not a tool");
        }
        FrontMatter fm = primitive.frontMatter();
        Kind kind = parse(Kind.class, "kind", fm.getOrDefault("kind", "brain"));
        ToolManifest manifest = new ToolManifest(
                primitive.name(),
                primitive.description(),
                primitive.phase(),
                kind,
                Authority.fromWire(fm.getOrDefault("authority", "read")),
                parse(Consequence.class, "consequence", fm.getOrDefault("consequence", "none")),
                parse(ApprovalMode.class, "approval", fm.getOrDefault("approval", "none")),
                Boolean.parseBoolean(fm.getOrDefault("reversible", "true")),
                fm.getOrDefault("method", "GET").toUpperCase(Locale.ROOT),
                fm.get("base_url_env"),
                fm.get("auth_env"),
                primitive.body());
        if (kind == Kind.HTTP && (manifest.baseUrlEnv == null || manifest.baseUrlEnv.isBlank())) {
            throw new IllegalArgumentException("an http tool needs base_url_env");
        }
        if (manifest.authority.isExecution() && manifest.approval == ApprovalMode.NONE) {
            throw new IllegalArgumentException("a tool with execute authority needs approval: policy or required");
        }
        if (manifest.authority == Authority.EXECUTE_WITH_APPROVAL && manifest.approval != ApprovalMode.REQUIRED) {
            throw new IllegalArgumentException("a tool with execute_with_approval authority needs approval: required");
        }
        return manifest;
    }

    static <E extends Enum<E>> E parse(Class<E> type, String field, String value) {
        try {
            return Enum.valueOf(type, value.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(field + " has unknown value '" + value + "'");
        }
    }
}
