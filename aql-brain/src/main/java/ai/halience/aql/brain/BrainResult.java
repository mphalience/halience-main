package ai.halience.aql.brain;

import java.util.List;
import java.util.Map;

/**
 * The result of one behaviour run: the governed envelope (after the grounding check and routing), the record ids it
 * was persisted under, and the raw structured answer for callers that asked for a custom schema.
 *
 * @param outputId the persisted output record, or null when the output type isn't stored (runtime-only)
 */
public record BrainResult(String behaviour, String runId, OutputEnvelope envelope, String outputId,
                          List<String> groundingProblems, Map<String, Object> structuredAnswer, double costCents) {

    public String text() {
        return envelope.summary();
    }
}
