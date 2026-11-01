package ai.halience.aql.journey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import ai.halience.aql.journey.JourneyDefinition.Milestone;
import ai.halience.aql.primitive.Primitive;
import ai.halience.aql.spec.Vocabulary.ApprovalMode;
import org.junit.jupiter.api.Test;

class JourneyEngineTest {

    static JourneyDefinition journey(String body) {
        return JourneyDefinition.of(Primitive.parse("journeys/j.md", "---\nname: j\ndescription: d\ngoal: g\n---\n"
                + body));
    }

    static final JourneyDefinition LAUNCH = journey("""
            ## research
            - title: Research the market
            - behaviour: gtm_researcher
            - done_when: output

            ## draft
            - after: research
            - behaviour: gtm_content_writer

            ## record_decision
            - after: research
            - behaviour: decision_recorder
            - phase: 2

            ## publish
            - after: draft, record_decision
            - behaviour: gtm_publisher
            - approval: required
            - done_when: approval
            """);

    @Test
    void parsesMilestones() {
        Milestone publish = LAUNCH.milestone("publish");
        assertThat(publish.after()).containsExactly("draft", "record_decision");
        assertThat(publish.approval()).isEqualTo(ApprovalMode.REQUIRED);
        assertThat(publish.doneWhen()).isEqualTo(JourneyDefinition.DoneWhen.APPROVAL);
        assertThat(LAUNCH.milestone("record_decision").phase()).isEqualTo(2);
        assertThat(LAUNCH.goal()).isEqualTo("g");
    }

    @Test
    void availableFollowsDependencies() {
        assertThat(JourneyEngine.available(LAUNCH, Set.of(), 2)).extracting(Milestone::key).containsExactly("research");
        assertThat(JourneyEngine.available(LAUNCH, Set.of("research"), 2)).extracting(Milestone::key)
                .containsExactly("draft", "record_decision");
    }

    @Test
    void laterPhaseMilestonesAreSkippedAndDontBlock() {
        assertThat(JourneyEngine.available(LAUNCH, Set.of("research", "draft"), 1)).extracting(Milestone::key)
                .containsExactly("publish");
        assertThat(JourneyEngine.isComplete(LAUNCH, Set.of("research", "draft", "publish"), 1)).isTrue();
        assertThat(JourneyEngine.isComplete(LAUNCH, Set.of("research", "draft", "publish"), 2)).isFalse();
    }

    @Test
    void detectsCyclesAndUnknownDependencies() {
        JourneyDefinition bad = journey("## a\n- after: c\n\n## b\n- after: a\n\n## c\n- after: b\n\n## d\n- after: ghost\n");
        assertThat(JourneyEngine.structuralProblems(bad)).anyMatch(p -> p.contains("cycle"))
                .anyMatch(p -> p.contains("unknown milestone ghost"));
        assertThat(JourneyEngine.structuralProblems(LAUNCH)).isEmpty();
    }

    @Test
    void rejectsBadMilestones() {
        assertThatThrownBy(() -> journey("## Bad-Key\n- title: x\n")).hasMessageContaining("must match");
        assertThatThrownBy(() -> journey("## a\n- approval: maybe\n")).hasMessageContaining("approval");
        assertThatThrownBy(() -> journey("no milestones")).hasMessageContaining("at least one");
    }
}
