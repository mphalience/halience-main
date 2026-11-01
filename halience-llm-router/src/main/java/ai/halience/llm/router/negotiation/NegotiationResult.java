package ai.halience.llm.router.negotiation;

import java.util.List;

import ai.halience.llm.router.model.DroppedAttachment;
import ai.halience.llm.router.model.ReasoningSettings;
import ai.halience.llm.router.model.Request;

import lombok.Builder;
import lombok.Value;

/**
 * The outcome of adapting one {@link Request} for one candidate model — see
 * {@code LIBRARY_SPEC.md} §4.
 */
@Value
@Builder
public class NegotiationResult {

    /** The request as it should actually be sent for this attempt. */
    Request adaptedRequest;

    /** Canonical §12.7 values for whatever couldn't be honored for this model. */
    List<String> droppedFeatures;

    /** Each attachment left out for this model, and why; empty when all were kept. */
    @Builder.Default
    List<DroppedAttachment> droppedAttachments = List.of();

    /** The reasoning control being sent (§7.4), or {@code null} if none. */
    ReasoningSettings reasoning;

    /**
     * {@code true} if {@code responseSchema} is being honored via prompt-fallback (schema
     * injected into {@code systemInstructions}) rather than the model's native structured-output
     * mechanism — tells the router core to best-effort parse the resulting text as JSON into
     * {@code Response.structuredOutput}.
     */
    boolean structuredOutputViaPromptFallback;
}
