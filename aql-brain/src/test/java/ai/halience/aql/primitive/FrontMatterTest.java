package ai.halience.aql.primitive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class FrontMatterTest {

    @Test
    void splitsFieldsAndBody() {
        FrontMatter fm = FrontMatter.parse("---\nname: halie\nskills: grounding, halie_voice  # inline comment\n"
                + "Thinking: low\n---\n\n## Who and what\nBody text.\n");
        assertThat(fm.get("name")).isEqualTo("halie");
        assertThat(fm.get("thinking")).isEqualTo("low");
        assertThat(fm.list("skills")).containsExactly("grounding", "halie_voice");
        assertThat(fm.body()).isEqualTo("## Who and what\nBody text.");
    }

    @Test
    void keepsHashesThatAreNotComments() {
        assertThat(FrontMatter.parse("---\nlanguage: C#\n---\nx").get("language")).isEqualTo("C#");
    }

    @Test
    void withoutFrontMatterTheWholeTextIsBody() {
        FrontMatter fm = FrontMatter.parse("Just text");
        assertThat(fm.fields()).isEmpty();
        assertThat(fm.body()).isEqualTo("Just text");
    }

    @Test
    void handlesWindowsLineEndingsAndBom() {
        assertThat(FrontMatter.parse("﻿---\r\nname: x\r\n---\r\nbody").get("name")).isEqualTo("x");
    }

    @Test
    void rejectsUnclosedDuplicateAndMalformedFrontMatter() {
        assertThatThrownBy(() -> FrontMatter.parse("---\nname: x\n")).hasMessageContaining("never closed");
        assertThatThrownBy(() -> FrontMatter.parse("---\nname: x\nname: y\n---\n")).hasMessageContaining("twice");
        assertThatThrownBy(() -> FrontMatter.parse("---\nnot a field\n---\n")).hasMessageContaining("key: value");
    }

    @Test
    void parsesAPrimitiveFromItsPath() {
        Primitive p = Primitive.parse("behaviours/halie/halie.md", "---\nname: halie\ndescription: d\nphase: 2\n---\nb");
        assertThat(p.type()).isEqualTo(PrimitiveType.BEHAVIOUR);
        assertThat(p.key()).isEqualTo("behaviours/halie/halie");
        assertThat(p.folder()).isEqualTo("halie");
        assertThat(p.phase()).isEqualTo(2);
        assertThat(p.isAgent()).isTrue();
        assertThat(Primitive.parse("behaviours/skills/grounding.md", "---\nname: grounding\n---\nb").isSkill()).isTrue();
        assertThatThrownBy(() -> Primitive.parse("nowhere/x.md", "b")).hasMessageContaining("Unknown primitive folder");
        assertThatThrownBy(() -> Primitive.parse("knowledge/x.md", "---\nphase: 4\n---\nb"))
                .hasMessageContaining("phase");
    }
}
