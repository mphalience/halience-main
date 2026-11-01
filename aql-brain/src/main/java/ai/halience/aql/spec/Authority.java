package ai.halience.aql.spec;

import java.util.Locale;

/**
 * The authority ladder (spec "Execution Authority", OPT-05/OPT-16), lowest first. A behaviour declares the highest
 * authority it may exercise; a tool declares the authority it needs. Knowing, recommending or preparing never implies
 * authority to execute (GV-05), so the execute levels sit at the top and also need approval where configured.
 */
public enum Authority {
    READ,
    COMPUTE,
    RECOMMEND,
    DRAFT,
    CREATE,
    PLAN,
    MONITOR,
    ROUTE,
    EVALUATE,
    LEARN,
    EXECUTE,
    EXECUTE_WITH_APPROVAL;

    /** Whether this authority covers {@code needed}. */
    public boolean covers(Authority needed) {
        return compareTo(needed) >= 0;
    }

    /** Whether this level changes system or external state (and so is checked at the point of action). */
    public boolean isExecution() {
        return this == EXECUTE || this == EXECUTE_WITH_APPROVAL;
    }

    public String wireValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Authority fromWire(String value) {
        try {
            return valueOf(value.strip().toUpperCase(Locale.ROOT));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("authority must be one of read, compute, recommend, draft, create, plan,"
                    + " monitor, route, evaluate, learn, execute, execute_with_approval; was '" + value + "'");
        }
    }
}
