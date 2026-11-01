package ai.halience.aql.brain;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.halience.aql.context.ContextEnvelope;
import ai.halience.aql.spec.PromptClass;
import ai.halience.aql.spec.SpecCode;
import ai.halience.aql.spec.Vocabulary.Consequence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Classification before a large model is called: which prompt class(es) a request is, which behaviour should take it,
 * and how consequential it is. It runs the low-thinking {@code intent_classifier} behaviour when available and falls
 * back to a deterministic rule (the consumer's companion) when the model is unavailable or answers with a behaviour
 * that doesn't exist, is disabled, or isn't allowed for the actor.
 */
@Slf4j
@Component
public class IntentClassifier {

    public static final String CLASSIFIER = "intent_classifier";
    public static final String HALIE = "halie";
    public static final String ASK_SETTLE = "ask_settle";

    public record Classification(List<PromptClass> promptClasses, String behaviour, boolean needsMoreContext,
                                 Consequence consequence, double confidence, String source) {
    }

    private final BehaviourRunner runner;
    private final BrainAgents agents;

    public IntentClassifier(BehaviourRunner runner, BrainAgents agents) {
        this.runner = runner;
        this.agents = agents;
    }

    public static Map<String, Object> schema() {
        return Map.of("type", "object", "properties", Map.of(
                        "prompt_classes", Map.of("type", "array", "items", Map.of("type", "string"),
                                "description", "PC codes, most important first"),
                        "behaviour", Map.of("type", "string", "description", "The behaviour that should take it"),
                        "needs_more_context", Map.of("type", "boolean"),
                        "consequence", Map.of("type", "string", "enum",
                                List.of("none", "low", "medium", "high", "critical")),
                        "confidence", Map.of("type", "number")),
                "required", List.of("prompt_classes", "behaviour", "needs_more_context", "consequence", "confidence"));
    }

    public Classification classify(String prompt, ContextEnvelope context, boolean admin) {
        try {
            agents.requireEnabled(CLASSIFIER);
            BrainResult result = runner.run(new BrainRequest(CLASSIFIER, prompt, context, Consequence.LOW, schema()));
            Classification parsed = parse(result.structuredAnswer());
            if (parsed != null && allowed(parsed.behaviour(), admin)) {
                return parsed;
            }
        } catch (RuntimeException e) {
            log.debug("Intent classifier unavailable, using the rule: {}", e.toString());
        }
        return byRule(prompt, context);
    }

    private boolean allowed(String behaviour, boolean admin) {
        try {
            var p = agents.requireEnabled(behaviour);
            return admin || !BrainAgents.ADMIN_FOLDER.equals(p.folder());
        } catch (RuntimeException e) {
            return false;
        }
    }

    static Classification parse(Map<String, Object> answer) {
        if (answer == null || answer.get("behaviour") == null) {
            return null;
        }
        List<PromptClass> classes = new ArrayList<>();
        if (answer.get("prompt_classes") instanceof List<?> list) {
            list.forEach(c -> SpecCode.parse(PromptClass.class, String.valueOf(c)).ifPresent(classes::add));
        }
        Consequence consequence;
        try {
            consequence = Consequence.valueOf(String.valueOf(answer.get("consequence")).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            consequence = Consequence.MEDIUM;
        }
        double confidence = answer.get("confidence") instanceof Number n ? n.doubleValue() : 0.5;
        return new Classification(classes, String.valueOf(answer.get("behaviour")).strip(),
                Boolean.TRUE.equals(answer.get("needs_more_context")), consequence, confidence, "model");
    }

    /**
     * The deterministic fallback: hand the request to the consumer's companion (Ask Settle inside Settle, Halie
     * everywhere else), with a rough prompt-class guess from the wording for the record.
     */
    static Classification byRule(String prompt, ContextEnvelope context) {
        String text = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);
        Object product = context.extra().get("product");
        String companion = product != null && "settle".equalsIgnoreCase(String.valueOf(product)) ? ASK_SETTLE : HALIE;
        List<PromptClass> classes = new ArrayList<>();
        if (text.matches(".*\\b(afford|mortgage|borrow|repayment|ltv)\\b.*")) {
            classes.add(PromptClass.PC_19);
        }
        if (text.matches(".*\\b(compare|versus|vs|difference)\\b.*")) {
            classes.add(PromptClass.PC_09);
        }
        if (text.matches(".*\\b(draft|write|summari[sz]e)\\b.*")) {
            classes.add(PromptClass.PC_12);
        }
        if (text.matches(".*\\b(should|recommend|next step|what now)\\b.*")) {
            classes.add(PromptClass.PC_11);
        }
        if (text.matches(".*\\b(send|publish|post|submit)\\b.*")) {
            classes.add(PromptClass.PC_15);
        }
        classes.add(PromptClass.PC_10);
        Consequence consequence = classes.contains(PromptClass.PC_15) ? Consequence.HIGH
                : classes.contains(PromptClass.PC_11) || classes.contains(PromptClass.PC_19) ? Consequence.MEDIUM
                : Consequence.LOW;
        return new Classification(classes, companion, false, consequence, 0.3, "rule");
    }
}
