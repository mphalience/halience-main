package ai.halience.aql.signal;

import ai.halience.aql.spec.SignalClass;
import ai.halience.aql.spec.SignalClass.Gate;
import ai.halience.aql.spec.Vocabulary.Materiality;
import ai.halience.aql.spec.Vocabulary.UserRelevance;

/**
 * The deterministic part of the monitoring &amp; materiality loop (LP-03, GV-13). It separates raw update, evidence
 * change, intelligence change and user-relevant material change:
 * <ul>
 *   <li>a raw source event is never material by itself: it must first be turned into an evidence/intelligence change;</li>
 *   <li>operational signals (source health, model/rule versions, incidents) go to AQL Operations, never to users;</li>
 *   <li>a user's own edits (preferences, saved state, affordability inputs), and score changes they cause, may trigger
 *       recomputation but are never external Watch changes;</li>
 *   <li>otherwise the size of the change against the thresholds decides materiality, and only a material change that is
 *       relevant to the user (they watch or own the entity) may notify.</li>
 * </ul>
 * Thresholds are an implementation decision per signal; these are first-cut defaults.
 */
public final class MaterialityAssessor {

    public static final double MATERIAL = 0.10;
    public static final double CRITICAL = 0.30;

    private MaterialityAssessor() {
    }

    /**
     * @param signal             the signal class of the change
     * @param relativeChange     the size of the change as a fraction (e.g. 0.12 for a 12% move), or 1.0 for a
     *                           categorical change (a new risk, a state that flipped)
     * @param causedByUserEdit   whether the change was caused by the user's own edit rather than external evidence
     * @param entityWatched      whether the user watches, owns or is working on the affected entity
     */
    public record Change(SignalClass signal, double relativeChange, boolean causedByUserEdit, boolean entityWatched) {
    }

    /**
     * @param notifyUser   whether this may become a user-facing alert (Pulse/Watch/notification)
     * @param routeToOps   whether it goes to AQL Operations
     * @param recompute    whether dependent intelligence should be recomputed
     */
    public record Verdict(Materiality materiality, UserRelevance userRelevance, boolean notifyUser, boolean routeToOps,
                          boolean recompute, String reason) {
    }

    public static Verdict assess(Change change) {
        SignalClass s = change.signal();
        if (s.layer() == SignalClass.Layer.RAW_EVENT && !s.isOperational()) {
            return new Verdict(Materiality.NOT_MATERIAL, UserRelevance.NOT_RELEVANT, false, false, true,
                    "raw source event: normalise and recompute evidence before assessing materiality");
        }
        if (s.isOperational()) {
            Materiality m = change.relativeChange() >= CRITICAL ? Materiality.CRITICAL : Materiality.MATERIAL;
            return new Verdict(m, UserRelevance.NOT_RELEVANT, false, true, s == SignalClass.SG_026,
                    "operational signal: route to AQL Operations");
        }
        if (s.isUserEdit() || change.causedByUserEdit()) {
            return new Verdict(Materiality.NOT_MATERIAL, UserRelevance.NOT_RELEVANT, false, false, true,
                    "user edit: recompute personal fit, but it is never an external Watch change");
        }
        if (s.materialityGate() == Gate.NO) {
            return new Verdict(Materiality.NOT_MATERIAL, UserRelevance.NOT_RELEVANT, false, false, false,
                    "signal class has no materiality gate: record only");
        }
        Materiality m = change.relativeChange() >= CRITICAL ? Materiality.CRITICAL
                : change.relativeChange() >= MATERIAL ? Materiality.MATERIAL : Materiality.NOT_MATERIAL;
        UserRelevance relevance = change.entityWatched() ? UserRelevance.RELEVANT : UserRelevance.NOT_RELEVANT;
        boolean notify = m != Materiality.NOT_MATERIAL && relevance == UserRelevance.RELEVANT;
        String reason = m == Materiality.NOT_MATERIAL ? "change below materiality threshold: hold and keep monitoring"
                : notify ? "material change to a watched entity" : "material change, but not relevant to this user";
        return new Verdict(m, relevance, notify, false, true, reason);
    }
}
