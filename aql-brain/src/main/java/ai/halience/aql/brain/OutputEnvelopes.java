package ai.halience.aql.brain;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.halience.aql.brain.OutputEnvelope.Claim;
import ai.halience.aql.brain.OutputEnvelope.ProposedAction;
import ai.halience.aql.spec.Vocabulary.ClaimClass;
import ai.halience.aql.spec.Vocabulary.FailureState;
import ai.halience.aql.spec.Vocabulary.Route;
import ai.halience.aql.spec.Vocabulary.Sufficiency;

/**
 * The JSON schema the runtime asks the agent loop to fill for every behaviour run, and the lenient parser that turns
 * the structured answer back into an {@link OutputEnvelope}. Unknown enum values degrade to the safest value (unknown
 * sufficiency, interpretive claim) rather than failing the run.
 */
public final class OutputEnvelopes {

    private OutputEnvelopes() {
    }

    public static Map<String, Object> schema() {
        Map<String, Object> claim = Map.of("type", "object", "properties", Map.of(
                        "text", Map.of("type", "string"),
                        "claim_class", Map.of("type", "string", "enum", List.of("observed", "derived", "interpretive")),
                        "evidence_refs", Map.of("type", "array", "items", Map.of("type", "string")),
                        "confidence", Map.of("type", List.of("number", "null")),
                        "sufficiency", Map.of("type", "string",
                                "enum", List.of("sufficient", "partial", "insufficient", "unknown"))),
                "required", List.of("text", "claim_class", "evidence_refs", "sufficiency"));
        Map<String, Object> action = Map.of("type", "object", "properties", Map.of(
                        "action_type", Map.of("type", "string"),
                        "target", Map.of("type", "string"),
                        "payload_summary", Map.of("type", "string"),
                        "authority", Map.of("type", "string"),
                        "approval_required", Map.of("type", "boolean")),
                "required", List.of("action_type", "target", "authority", "approval_required"));
        return Map.of("type", "object", "properties", Map.of(
                        "output_type", Map.of("type", "string", "description", "The OUT code, e.g. OUT-08"),
                        "summary", Map.of("type", "string", "description", "The answer or output, as the consumer reads it"),
                        "claims", Map.of("type", "array", "items", claim),
                        "known_unknowns", Map.of("type", "array", "items", Map.of("type", "string")),
                        "failure_state", Map.of("type", "string",
                                "enum", List.of("none", "degraded", "failed", "blocked", "unknown")),
                        "proposed_actions", Map.of("type", "array", "items", action),
                        "route", Map.of("type", "string",
                                "enum", List.of("none", "human_review", "fallback", "escalate")),
                        "route_reason", Map.of("type", "string"),
                        "method_versions", Map.of("type", "array", "items", Map.of("type", "string"))),
                "required", List.of("output_type", "summary", "claims", "known_unknowns", "failure_state", "route"));
    }

    public static OutputEnvelope parse(Map<String, Object> answer, String fallbackOutputType, String fallbackText) {
        if (answer == null || answer.isEmpty()) {
            return OutputEnvelope.textOnly(fallbackOutputType, fallbackText);
        }
        List<Claim> claims = new ArrayList<>();
        for (Map<String, Object> c : maps(answer.get("claims"))) {
            claims.add(new Claim(string(c.get("text"), ""),
                    enumOf(ClaimClass.class, c.get("claim_class"), ClaimClass.INTERPRETIVE),
                    strings(c.get("evidence_refs")),
                    c.get("confidence") instanceof Number n ? Math.max(0, Math.min(1, n.doubleValue())) : null,
                    enumOf(Sufficiency.class, c.get("sufficiency"), Sufficiency.UNKNOWN)));
        }
        List<ProposedAction> actions = new ArrayList<>();
        for (Map<String, Object> a : maps(answer.get("proposed_actions"))) {
            actions.add(new ProposedAction(string(a.get("action_type"), "unspecified"), string(a.get("target"), ""),
                    string(a.get("payload_summary"), ""), string(a.get("authority"), "recommend"),
                    !Boolean.FALSE.equals(a.get("approval_required"))));
        }
        String summary = string(answer.get("summary"), fallbackText);
        return new OutputEnvelope(
                string(answer.get("output_type"), fallbackOutputType),
                summary == null || summary.isBlank() ? fallbackText : summary,
                claims,
                strings(answer.get("known_unknowns")),
                enumOf(FailureState.class, answer.get("failure_state"), FailureState.NONE),
                actions,
                enumOf(Route.class, answer.get("route"), Route.NONE),
                string(answer.get("route_reason"), null),
                strings(answer.get("method_versions")));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> maps(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().filter(Map.class::isInstance).map(m -> (Map<String, Object>) m).toList();
    }

    private static List<String> strings(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().filter(java.util.Objects::nonNull).map(String::valueOf).filter(s -> !s.isBlank()).toList();
    }

    private static String string(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private static <E extends Enum<E>> E enumOf(Class<E> type, Object value, E fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, String.valueOf(value).strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
