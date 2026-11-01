package ai.halience.aql.brain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.halience.aql.spec.Vocabulary.ClaimClass;
import ai.halience.aql.spec.Vocabulary.FailureState;
import ai.halience.aql.spec.Vocabulary.Route;
import ai.halience.aql.spec.Vocabulary.Sufficiency;

/**
 * The structured answer every behaviour run returns (the {@code output_envelope} skill tells the model how to write
 * it). It keeps the spec's distinctions in machine-readable form: claim classes, evidence refs, sufficiency, known
 * unknowns, failure state, proposed actions with their authority, and the route.
 */
public record OutputEnvelope(String outputType, String summary, List<Claim> claims, List<String> knownUnknowns,
                             FailureState failureState, List<ProposedAction> proposedActions, Route route,
                             String routeReason, List<String> methodVersions) {

    public record Claim(String text, ClaimClass claimClass, List<String> evidenceRefs, Double confidence,
                        Sufficiency sufficiency) {

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("text", text);
            map.put("claim_class", claimClass.name().toLowerCase(Locale.ROOT));
            map.put("evidence_refs", evidenceRefs);
            map.put("confidence", confidence);
            map.put("sufficiency", sufficiency.name().toLowerCase(Locale.ROOT));
            return map;
        }
    }

    public record ProposedAction(String actionType, String target, String payloadSummary, String authority,
                                 boolean approvalRequired) {
    }

    /** An envelope for a run that produced only text (no structured answer could be read). */
    public static OutputEnvelope textOnly(String outputType, String text) {
        return new OutputEnvelope(outputType, text, List.of(), List.of("The answer could not be structured, so its "
                + "claims are not individually grounded"), FailureState.DEGRADED, List.of(), Route.ANSWER_WITH_LIMITS,
                "unstructured answer", List.of());
    }

    /** The overall sufficiency: the weakest claim's, or unknown when there are no claims. */
    public Sufficiency overallSufficiency() {
        if (claims.isEmpty()) {
            return Sufficiency.UNKNOWN;
        }
        Sufficiency worst = Sufficiency.SUFFICIENT;
        for (Claim c : claims) {
            if (c.sufficiency().ordinal() > worst.ordinal()) {
                worst = c.sufficiency();
            }
        }
        return worst;
    }

    /** The lowest stated confidence, or null when no claim states one. */
    public Double minConfidence() {
        return claims.stream().map(Claim::confidence).filter(java.util.Objects::nonNull).min(Double::compare)
                .orElse(null);
    }

    public List<String> evidenceRefs() {
        List<String> refs = new ArrayList<>();
        claims.forEach(c -> c.evidenceRefs().stream().filter(r -> !refs.contains(r)).forEach(refs::add));
        return refs;
    }

    public OutputEnvelope withRoute(Route newRoute, String reason) {
        return new OutputEnvelope(outputType, summary, claims, knownUnknowns, failureState, proposedActions, newRoute,
                reason, methodVersions);
    }

    public OutputEnvelope withClaimsAndUnknowns(List<Claim> newClaims, List<String> newUnknowns) {
        return new OutputEnvelope(outputType, summary, newClaims, newUnknowns, failureState, proposedActions, route,
                routeReason, methodVersions);
    }
}
