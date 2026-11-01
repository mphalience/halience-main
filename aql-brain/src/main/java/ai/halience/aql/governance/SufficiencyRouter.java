package ai.halience.aql.governance;

import ai.halience.aql.spec.Vocabulary.Consequence;
import ai.halience.aql.spec.Vocabulary.FailureState;
import ai.halience.aql.spec.Vocabulary.Route;
import ai.halience.aql.spec.Vocabulary.Sufficiency;

/**
 * GV-03 and GV-07 as a deterministic rule: low confidence, insufficient evidence or a failure changes what happens,
 * not just what is displayed, and the higher the consequence the earlier a person is brought in. A consequential task
 * is never silently degraded (PC-17).
 */
public final class SufficiencyRouter {

    /** Below this confidence a result is treated as weak. Confidence never compensates for missing evidence. */
    public static final double WEAK_CONFIDENCE = 0.5;

    private SufficiencyRouter() {
    }

    public static Route route(Sufficiency sufficiency, Double confidence, Consequence consequence,
            FailureState failure) {
        Sufficiency s = sufficiency == null ? Sufficiency.UNKNOWN : sufficiency;
        Consequence c = consequence == null ? Consequence.NONE : consequence;
        FailureState f = failure == null ? FailureState.NONE : failure;
        boolean high = c.compareTo(Consequence.HIGH) >= 0;
        boolean medium = c.compareTo(Consequence.MEDIUM) >= 0;

        switch (f) {
            case BLOCKED -> {
                return Route.BLOCK;
            }
            case FAILED, UNKNOWN -> {
                return medium ? Route.ESCALATE : Route.FALLBACK;
            }
            case DEGRADED -> {
                return high ? Route.HUMAN_REVIEW : Route.ANSWER_WITH_LIMITS;
            }
            case NONE -> {
                // fall through to evidence and confidence
            }
        }
        if (s == Sufficiency.INSUFFICIENT || s == Sufficiency.UNKNOWN) {
            return medium ? Route.ESCALATE : Route.ANSWER_WITH_LIMITS;
        }
        if (s == Sufficiency.PARTIAL) {
            return high ? Route.HUMAN_REVIEW : Route.ANSWER_WITH_LIMITS;
        }
        if (confidence != null && confidence < WEAK_CONFIDENCE) {
            return high ? Route.HUMAN_REVIEW : Route.ANSWER_WITH_LIMITS;
        }
        return Route.NONE;
    }
}
