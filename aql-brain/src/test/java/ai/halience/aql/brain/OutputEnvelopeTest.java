package ai.halience.aql.brain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ai.halience.aql.Fakes;
import ai.halience.aql.context.ContextEnvelope;
import ai.halience.aql.spec.PromptClass;
import ai.halience.aql.spec.Trigger;
import ai.halience.aql.spec.Vocabulary.ClaimClass;
import ai.halience.aql.spec.Vocabulary.Consequence;
import ai.halience.aql.spec.Vocabulary.FailureState;
import ai.halience.aql.spec.Vocabulary.Route;
import ai.halience.aql.spec.Vocabulary.Sufficiency;
import org.junit.jupiter.api.Test;

/** Parsing the structured answer, the grounding check, and the deterministic intent rule. */
class OutputEnvelopeTest {

    static Map<String, Object> answer() {
        return Map.of(
                "output_type", "OUT-08",
                "summary", "The Watch requirement is in progress.",
                "claims", List.of(
                        Map.of("text", "Requirement R12 is in progress", "claim_class", "observed",
                                "evidence_refs", List.of("kno_1"), "confidence", 0.9, "sufficiency", "sufficient"),
                        Map.of("text", "It will likely ship this sprint", "claim_class", "interpretive",
                                "evidence_refs", List.of(), "sufficiency", "partial")),
                "known_unknowns", List.of("No repository evidence yet"),
                "failure_state", "none",
                "route", "none");
    }

    @Test
    void parsesTheEnvelope() {
        OutputEnvelope e = OutputEnvelopes.parse(answer(), "OUT-08", "fallback");
        assertThat(e.claims()).hasSize(2);
        assertThat(e.claims().getFirst().claimClass()).isEqualTo(ClaimClass.OBSERVED);
        assertThat(e.overallSufficiency()).isEqualTo(Sufficiency.PARTIAL);
        assertThat(e.minConfidence()).isEqualTo(0.9);
        assertThat(e.evidenceRefs()).containsExactly("kno_1");
    }

    @Test
    void unknownValuesDegradeToTheSafestOption() {
        OutputEnvelope e = OutputEnvelopes.parse(Map.of("summary", "x", "claims",
                List.of(Map.of("text", "t", "claim_class", "gospel", "sufficiency", "lots", "confidence", 7))),
                "OUT-03", "fb");
        assertThat(e.claims().getFirst().claimClass()).isEqualTo(ClaimClass.INTERPRETIVE);
        assertThat(e.claims().getFirst().sufficiency()).isEqualTo(Sufficiency.UNKNOWN);
        assertThat(e.claims().getFirst().confidence()).isEqualTo(1.0);
        assertThat(e.outputType()).isEqualTo("OUT-03");
    }

    @Test
    void anUnstructuredAnswerIsMarkedDegraded() {
        OutputEnvelope e = OutputEnvelopes.parse(null, "OUT-08", "just text");
        assertThat(e.summary()).isEqualTo("just text");
        assertThat(e.failureState()).isEqualTo(FailureState.DEGRADED);
        assertThat(e.route()).isEqualTo(Route.ANSWER_WITH_LIMITS);
    }

    @Test
    void groundingFlagsObservedClaimsWithoutEvidenceAndInventedRefs() {
        OutputEnvelope e = OutputEnvelopes.parse(Map.of("summary", "s", "claims", List.of(
                Map.of("text", "No source", "claim_class", "observed", "evidence_refs", List.of(),
                        "sufficiency", "sufficient"),
                Map.of("text", "Made-up source", "claim_class", "derived", "evidence_refs", List.of("evd_fake"),
                        "sufficiency", "sufficient"),
                Map.of("text", "Real source", "claim_class", "observed", "evidence_refs", List.of("evd_real"),
                        "sufficiency", "sufficient"))), "OUT-01", "fb");
        GroundingCheck.Result r = GroundingCheck.apply(e, Set.of("evd_real"));
        assertThat(r.grounded()).isFalse();
        assertThat(r.problems()).hasSize(2);
        assertThat(r.envelope().claims().get(0).sufficiency()).isEqualTo(Sufficiency.INSUFFICIENT);
        assertThat(r.envelope().claims().get(1).evidenceRefs()).isEmpty();
        assertThat(r.envelope().claims().get(2).sufficiency()).isEqualTo(Sufficiency.SUFFICIENT);
        assertThat(r.envelope().knownUnknowns()).hasSize(2);
    }

    @Test
    void theRuleSendsSettleUsersToAskSettleAndEveryoneElseToHalie() {
        ContextEnvelope halie = ContextEnvelope.of(Fakes.person("ana"), Trigger.TR_01, Fakes.NOW);
        ContextEnvelope settle = halie.withExtra("product", "settle");
        IntentClassifier.Classification forHalie = IntentClassifier.byRule("Where are we with Watch?", halie);
        assertThat(forHalie.behaviour()).isEqualTo("halie");
        IntentClassifier.Classification forSettle = IntentClassifier.byRule("Can I afford this with a mortgage?",
                settle);
        assertThat(forSettle.behaviour()).isEqualTo("ask_settle");
        assertThat(forSettle.promptClasses()).contains(PromptClass.PC_19);
        assertThat(IntentClassifier.byRule("Please send the email", halie).consequence()).isEqualTo(Consequence.HIGH);
    }

    @Test
    void parsesTheClassifierAnswer() {
        IntentClassifier.Classification c = IntentClassifier.parse(Map.of("prompt_classes", List.of("PC-09", "PC-99"),
                "behaviour", "delivery_traceability", "needs_more_context", false, "consequence", "low",
                "confidence", 0.8));
        assertThat(c.promptClasses()).containsExactly(PromptClass.PC_09);
        assertThat(c.behaviour()).isEqualTo("delivery_traceability");
        assertThat(IntentClassifier.parse(Map.of())).isNull();
    }
}
