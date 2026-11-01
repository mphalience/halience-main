package ai.halience.aql.brain;

import java.util.Map;

import ai.halience.aql.context.ContextEnvelope;
import ai.halience.aql.spec.Vocabulary.Consequence;

/**
 * One request for brain work.
 *
 * @param behaviour    the behaviour to run, or null to let the intent classifier choose
 * @param prompt       the task: a user's words, a structured instruction, an event to evaluate
 * @param consequence  how consequential the result is, for routing (null: derived from the behaviour's authority)
 * @param answerSchema a custom answer schema (null: the standard output envelope)
 */
public record BrainRequest(String behaviour, String prompt, ContextEnvelope context, Consequence consequence,
                           Map<String, Object> answerSchema) {

    public static BrainRequest of(String behaviour, String prompt, ContextEnvelope context) {
        return new BrainRequest(behaviour, prompt, context, null, null);
    }

    public BrainRequest withBehaviour(String name) {
        return new BrainRequest(name, prompt, context, consequence, answerSchema);
    }
}
