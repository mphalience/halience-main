package ai.halience.aql.primitive;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class PrimitiveValidatorTest {

    static Primitive agent(String folder, String name, String extra) {
        return Primitive.parse("behaviours/" + folder + "/" + name + ".md", "---\nname: " + name
                + "\ndescription: An agent\nthinking: low\nprompt_classes: PC-01\nloops: LP-01\nauthority: read\n"
                + extra + "---\n## Who and what\nDoes a job.");
    }

    static Primitive tool(String name, String authority, String approval, int phase) {
        return Primitive.parse("tools/" + name + ".md", "---\nname: " + name + "\ndescription: A tool\nphase: " + phase
                + "\nkind: brain\nauthority: " + authority + "\napproval: " + approval + "\n---\nUse it.");
    }

    static Primitive skill(String name) {
        return Primitive.parse("behaviours/skills/" + name + ".md", "---\nname: " + name
                + "\ndescription: A skill\ngovernance: GV-01\n---\nBe grounded.");
    }

    static List<String> errors(Primitive... primitives) {
        return PrimitiveValidator.errors(PrimitiveSet.of(primitives)).stream().map(ValidationIssue::message).toList();
    }

    @Test
    void aWellFormedSetIsClean() {
        assertThat(errors(agent("core", "a", "skills: grounding\ntools: search\n"), skill("grounding"),
                tool("search", "read", "none", 1))).isEmpty();
    }

    @Test
    void unknownReferencesAndCodesAreErrors() {
        assertThat(errors(agent("core", "a", "skills: nope\ntools: missing\nknowledge: gone\noutputs: OUT-99\n")))
                .anyMatch(m -> m.contains("unknown skill 'nope'"))
                .anyMatch(m -> m.contains("unknown tool 'missing'"))
                .anyMatch(m -> m.contains("unknown knowledge 'gone'"))
                .anyMatch(m -> m.contains("'OUT-99' is not a OutputType code"));
    }

    @Test
    void anAgentCantUseAToolAboveItsAuthority() {
        assertThat(errors(agent("core", "a", "tools: send\n"), tool("send", "execute", "required", 1)))
                .anyMatch(m -> m.contains("tool send needs authority execute but the behaviour only has read"));
    }

    @Test
    void anActivityNeedsItsAuthorityAndPromptClass() {
        assertThat(errors(agent("ops", "a", "activities: PA-019\n")))
                .anyMatch(m -> m.contains("PA-019 needs authority execute"))
                .anyMatch(m -> m.contains("PA-019 is a PC-15 activity"));
    }

    @Test
    void executeToolsMustDeclareApproval() {
        assertThat(errors(tool("send", "execute", "none", 1)))
                .anyMatch(m -> m.contains("needs approval: policy or required"));
        assertThat(errors(tool("publish", "execute_with_approval", "policy", 1)))
                .anyMatch(m -> m.contains("needs approval: required"));
    }

    @Test
    void aPrimitiveCantDependOnALaterPhaseOne() {
        assertThat(errors(agent("core", "a", "tools: future\n"), tool("future", "read", "none", 2)))
                .anyMatch(m -> m.contains("is phase 2 but this is phase 1"));
    }

    @Test
    void namesMustMatchFilesAndBeUnique() {
        Primitive misnamed = Primitive.parse("knowledge/one.md", "---\nname: two\ndescription: d\n---\nb");
        assertThat(errors(misnamed)).anyMatch(m -> m.contains("must match the file name"));
    }

    @Test
    void delegatesMustBeRealFolders() {
        assertThat(errors(agent("halie", "h", "delegates: nobody, skills\n")))
                .anyMatch(m -> m.contains("delegates folder 'nobody' has no agents"))
                .anyMatch(m -> m.contains("can't list the skills folder"));
    }

    @Test
    void journeysMustBeAcyclicAndUseRealBehaviours() {
        Primitive journey = Primitive.parse("journeys/j.md", "---\nname: j\ndescription: d\n---\n"
                + "## a\n- after: b\n- behaviour: a\n\n## b\n- after: a\n- behaviour: ghost\n");
        List<String> errors = new ArrayList<>(errors(journey, agent("core", "a", "")));
        assertThat(errors).anyMatch(m -> m.contains("cycle")).anyMatch(m -> m.contains("unknown behaviour ghost"));
    }

    @Test
    void aMilestoneMustBeFlaggedWithItsBehavioursPhase() {
        Primitive later = Primitive.parse("behaviours/work/w.md", "---\nname: w\ndescription: d\nphase: 2\n"
                + "prompt_classes: PC-14\nloops: LP-09\nauthority: plan\n---\nb");
        Primitive journey = Primitive.parse("journeys/j.md", "---\nname: j\ndescription: d\n---\n## step\n- behaviour: w\n");
        assertThat(errors(journey, later)).anyMatch(m -> m.contains("flag the milestone with phase: 2"));
    }

    @Test
    void routinesNeedAKnownBehaviourAndCadence() {
        Primitive routine = Primitive.parse("routines/r.md", "---\nname: r\ndescription: d\ncadence: fortnightly\n"
                + "behaviour: a\n---\nCheck.");
        assertThat(errors(routine, agent("core", "a", ""))).anyMatch(m -> m.contains("cadence must be"));
    }

    @Test
    void knowledgeStatusIsControlled() {
        Primitive k = Primitive.parse("knowledge/k.md", "---\nname: k\ndescription: d\nstatus: gospel\nowner: AQL\n---\nb");
        assertThat(errors(k)).anyMatch(m -> m.contains("status 'gospel' is unknown"));
    }
}
