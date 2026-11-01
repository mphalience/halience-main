package ai.halience.aql.llm;

import java.util.Locale;

import ai.halience.llm.router.config.ThinkingLevel;

/**
 * How hard a behaviour needs to think, chosen in its front matter ({@code thinking: low | medium | high}). Each level
 * maps to a configured, ordered list of LLMs ({@link ThinkingRoutes}) and to the router's own thinking level, so the
 * router tries those models first and falls back within the same depth.
 */
public enum Thinking {
    /** Classification, extraction, routing, short structured jobs. Cheapest, fastest models. */
    LOW(ThinkingLevel.LOW),
    /** Most companion answers, drafting, comparison, analysis. */
    MEDIUM(ThinkingLevel.MEDIUM),
    /** Judgement, evaluation, research synthesis, consequential recommendations. Strongest models. */
    HIGH(ThinkingLevel.HIGH);

    private final ThinkingLevel routerLevel;

    Thinking(ThinkingLevel routerLevel) {
        this.routerLevel = routerLevel;
    }

    public ThinkingLevel routerLevel() {
        return routerLevel;
    }

    public String wireValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Thinking fromWire(String value) {
        if (value == null || value.isBlank()) {
            return MEDIUM;
        }
        try {
            return valueOf(value.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("thinking must be low, medium or high; was '" + value + "'");
        }
    }
}
