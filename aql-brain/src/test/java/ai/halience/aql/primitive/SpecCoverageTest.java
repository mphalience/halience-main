package ai.halience.aql.primitive;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ai.halience.aql.journey.JourneyDefinition;
import ai.halience.aql.routine.RoutineDefinition;
import ai.halience.aql.spec.ContextDomain;
import ai.halience.aql.spec.GovernanceControl;
import ai.halience.aql.spec.Loop;
import ai.halience.aql.spec.LoopChain;
import ai.halience.aql.spec.OutcomeFamily;
import ai.halience.aql.spec.OutputType;
import ai.halience.aql.spec.PromptActivity;
import ai.halience.aql.spec.PromptClass;
import ai.halience.aql.spec.SignalClass;
import ai.halience.aql.spec.SpecCode;
import ai.halience.aql.spec.Trigger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The behaviours cover the AQL Phase 1 Execution Spec: every Prompt Activity has a Phase 1 behaviour, every Prompt
 * Class and Loop is realised by some primitive, every governance control is enforced by a skill or named by a
 * primitive, and every output, context, signal, trigger, chain and outcome family is described somewhere.
 */
class SpecCoverageTest {

    static PrimitiveSet set;
    static String allText;

    @BeforeAll
    static void load() {
        set = StarterPrimitives.parse(StarterPrimitives.load("classpath*:brain/**/*.md"));
        allText = String.join("\n", set.all().stream().map(Primitive::markdown).toList());
    }

    @Test
    void everyPromptActivityHasAPhaseOneBehaviour() {
        Set<String> implemented = new HashSet<>();
        set.agents().stream().filter(p -> p.phase() == 1).forEach(p -> implemented.addAll(p.list("activities")));
        List<String> missing = Arrays.stream(PromptActivity.values()).map(PromptActivity::code)
                .filter(code -> !implemented.contains(code)).toList();
        assertThat(missing).as("Prompt Activities with no Phase 1 behaviour").isEmpty();
    }

    @Test
    void everyPromptClassIsPerformedBySomeBehaviour() {
        Set<String> performed = new HashSet<>();
        set.agents().forEach(p -> performed.addAll(p.list("prompt_classes")));
        assertThat(missing(PromptClass.values(), performed)).as("Prompt Classes no behaviour performs").isEmpty();
    }

    @Test
    void everyLoopIsRealisedByABehaviourJourneyOrRoutine() {
        Set<String> realised = new HashSet<>();
        set.agents().forEach(p -> realised.addAll(p.list("loops")));
        set.ofType(PrimitiveType.JOURNEY).forEach(p -> {
            realised.addAll(p.list("loops"));
            JourneyDefinition.of(p).milestones().stream().filter(m -> m.loop() != null)
                    .forEach(m -> realised.add(m.loop()));
        });
        set.ofType(PrimitiveType.ROUTINE).forEach(p -> realised.addAll(RoutineDefinition.of(p).loops()));
        assertThat(missing(Loop.values(), realised)).as("Loops with no primitive").isEmpty();
    }

    @Test
    void phaseOneLoopsHavePhaseOneBehaviours() {
        Set<String> realised = new HashSet<>();
        set.agents().stream().filter(p -> p.phase() == 1).forEach(p -> realised.addAll(p.list("loops")));
        List<String> missing = Arrays.stream(Loop.values()).filter(l -> l.phase() == 1).map(Loop::code)
                .filter(c -> !realised.contains(c)).toList();
        assertThat(missing).as("Phase 1 loops with no Phase 1 behaviour").isEmpty();
    }

    @Test
    void everyLoopChainHasAJourney() {
        Set<String> chains = new HashSet<>();
        set.ofType(PrimitiveType.JOURNEY).forEach(p -> {
            String chain = JourneyDefinition.of(p).chain();
            if (chain != null) {
                chains.add(chain);
            }
        });
        assertThat(missing(LoopChain.values(), chains)).as("Loop Chains with no journey").isEmpty();
    }

    @Test
    void everyGovernanceControlIsEnforcedByASkillOrNamedByAPrimitive() {
        Set<String> enforced = new HashSet<>();
        set.skills().forEach(p -> enforced.addAll(p.list("governance")));
        Set<String> named = new HashSet<>(enforced);
        set.all().forEach(p -> named.addAll(p.list("governance")));
        assertThat(missing(GovernanceControl.values(), named)).as("Governance controls nowhere").isEmpty();
        // The controls every answer depends on must be skills, not just mentions.
        assertThat(enforced).contains("GV-01", "GV-02", "GV-03", "GV-04", "GV-05", "GV-06", "GV-07", "GV-13",
                "GV-14", "GV-17", "GV-18", "GV-19", "GV-22");
    }

    @Test
    void everyOutputContextSignalAndTriggerIsDescribed() {
        List<String> missing = new ArrayList<>();
        missing.addAll(mentioned(OutputType.values()));
        missing.addAll(mentioned(ContextDomain.values()));
        missing.addAll(mentioned(SignalClass.values()));
        missing.addAll(mentioned(Trigger.values()));
        assertThat(missing).as("Spec codes no primitive mentions").isEmpty();
    }

    @Test
    void theOutcomeTaxonomyListsAllNineteenFamilies() {
        String taxonomy = set.named(PrimitiveType.KNOWLEDGE, "outcome_taxonomy").orElseThrow().markdown();
        List<String> missing = Arrays.stream(OutcomeFamily.values()).map(OutcomeFamily::title)
                .filter(title -> !taxonomy.contains(title)).toList();
        assertThat(OutcomeFamily.values()).hasSize(19);
        assertThat(missing).as("Outcome families missing from outcome_taxonomy").isEmpty();
    }

    @Test
    void theBuildsFromTheSlidesHaveJourneys() {
        assertThat(set.named(PrimitiveType.JOURNEY, "venture_build")).as("Build").isPresent();
        assertThat(set.named(PrimitiveType.JOURNEY, "company_acquire")).as("Acquire").isPresent();
        assertThat(set.named(PrimitiveType.JOURNEY, "investment_situation")).as("Invest").isPresent();
    }

    @Test
    void halieHasTheSelfEditingBehaviourAndItCannotApply() {
        Primitive editor = set.agent("primitive_editor").orElseThrow();
        assertThat(editor.list("tools")).contains("primitive_view", "primitive_propose")
                .doesNotContain("execute_action");
        assertThat(BehaviourSpec.of(editor).authority().isExecution()).isFalse();
    }

    private static <E extends Enum<E> & SpecCode> List<String> missing(E[] values, Set<String> present) {
        return Arrays.stream(values).map(SpecCode::code).filter(code -> !present.contains(code)).toList();
    }

    private static <E extends Enum<E> & SpecCode> List<String> mentioned(E[] values) {
        return Arrays.stream(values).map(SpecCode::code).filter(code -> !allText.contains(code)).toList();
    }
}
