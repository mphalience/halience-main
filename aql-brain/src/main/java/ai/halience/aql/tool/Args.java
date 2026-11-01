package ai.halience.aql.tool;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.halience.agent.loop.tool.ToolInputException;

/** Lenient argument readers for brain tools; bad input is a {@link ToolInputException} the model can fix. */
final class Args {

    private Args() {
    }

    static String str(Map<String, Object> args, String name) {
        Object v = args.get(name);
        return v == null || String.valueOf(v).isBlank() ? null : String.valueOf(v).strip();
    }

    static String required(Map<String, Object> args, String name) {
        String v = str(args, name);
        if (v == null) {
            throw new ToolInputException(name + " is required");
        }
        return v;
    }

    static BigDecimal decimal(Map<String, Object> args, String name) {
        Object v = args.get(name);
        if (v == null || String.valueOf(v).isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(v).strip());
        } catch (NumberFormatException e) {
            throw new ToolInputException(name + " must be a number");
        }
    }

    static Double number(Map<String, Object> args, String name) {
        BigDecimal d = decimal(args, name);
        return d == null ? null : d.doubleValue();
    }

    static Integer integer(Map<String, Object> args, String name) {
        BigDecimal d = decimal(args, name);
        return d == null ? null : d.intValue();
    }

    static boolean bool(Map<String, Object> args, String name) {
        Object v = args.get(name);
        return v instanceof Boolean b ? b : v != null && Boolean.parseBoolean(String.valueOf(v));
    }

    static List<String> list(Map<String, Object> args, String name) {
        Object v = args.get(name);
        if (v == null) {
            return List.of();
        }
        if (v instanceof List<?> l) {
            return l.stream().filter(java.util.Objects::nonNull).map(String::valueOf).toList();
        }
        return java.util.Arrays.stream(String.valueOf(v).split(",")).map(String::strip).filter(s -> !s.isEmpty())
                .toList();
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> map(Map<String, Object> args, String name) {
        Object v = args.get(name);
        if (v instanceof Map<?, ?> m) {
            return new LinkedHashMap<>((Map<String, Object>) m);
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> maps(Map<String, Object> args, String name) {
        Object v = args.get(name);
        if (!(v instanceof List<?> l)) {
            return List.of();
        }
        return l.stream().filter(Map.class::isInstance).map(m -> (Map<String, Object>) m).toList();
    }

    static <E extends Enum<E>> E enumOf(Class<E> type, Map<String, Object> args, String name, E fallback) {
        String v = str(args, name);
        if (v == null) {
            if (fallback == null) {
                throw new ToolInputException(name + " is required");
            }
            return fallback;
        }
        try {
            return Enum.valueOf(type, v.toUpperCase(Locale.ROOT).replace(' ', '_').replace('/', '_'));
        } catch (IllegalArgumentException e) {
            throw new ToolInputException(name + " must be one of " + java.util.Arrays.toString(type.getEnumConstants())
                    .toLowerCase(Locale.ROOT));
        }
    }
}
