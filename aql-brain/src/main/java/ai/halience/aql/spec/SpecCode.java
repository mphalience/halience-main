package ai.halience.aql.spec;

import java.util.Arrays;
import java.util.Optional;

/**
 * A code from the AQL Phase 1 Execution Spec, such as {@code PC-01}, {@code LP-14} or {@code GV-18}. Every enum in this
 * package implements it so primitives can reference the spec by code and the runtime can check the reference exists.
 */
public interface SpecCode {

    /** The code exactly as the spec writes it, e.g. {@code PA-021}. */
    String code();

    /** The spec's short title for the code. */
    String title();

    /** The constant of {@code type} whose code is {@code code} (trimmed, case-insensitive), if any. */
    static <E extends Enum<E> & SpecCode> Optional<E> parse(Class<E> type, String code) {
        if (code == null) {
            return Optional.empty();
        }
        String wanted = code.strip();
        return Arrays.stream(type.getEnumConstants()).filter(e -> e.code().equalsIgnoreCase(wanted)).findFirst();
    }

    /** As {@link #parse}, failing with a message naming the type when the code is unknown. */
    static <E extends Enum<E> & SpecCode> E require(Class<E> type, String code) {
        return parse(type, code).orElseThrow(() -> new IllegalArgumentException(
                "Unknown " + type.getSimpleName() + " code '" + code + "'"));
    }
}
