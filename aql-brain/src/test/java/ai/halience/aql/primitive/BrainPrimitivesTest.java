package ai.halience.aql.primitive;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** The shipped primitives parse and validate with no errors. */
class BrainPrimitivesTest {

    static PrimitiveSet set;

    @BeforeAll
    static void load() {
        set = StarterPrimitives.parse(StarterPrimitives.load("classpath*:brain/**/*.md"));
    }

    @Test
    void everyPrimitiveTypeIsPresent() {
        for (PrimitiveType type : PrimitiveType.values()) {
            assertThat(set.ofType(type)).as(type.name()).isNotEmpty();
        }
        assertThat(set.skills()).isNotEmpty();
    }

    @Test
    void theShippedSetHasNoValidationErrors() {
        List<ValidationIssue> errors = PrimitiveValidator.errors(set);
        assertThat(errors).as(String.join("\n", errors.stream().map(Object::toString).toList())).isEmpty();
    }
}
