package ai.halience.aql.governance;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import ai.halience.aql.domain.Evidence;
import ai.halience.aql.spec.Vocabulary.Classification;
import org.junit.jupiter.api.Test;

class PermissionServiceTest {

    final PermissionService permissions = new PermissionService();
    final Actor broad = Actor.person("ana", Set.of(), Set.of(Actor.ALL_SCOPES));
    final Actor settleOnly = Actor.person("ben", Set.of(), Set.of("venture:settle"));

    @Test
    void internalRecordsFollowScopes() {
        assertThat(permissions.canSee(broad, Classification.INTERNAL, "venture:other")).isTrue();
        assertThat(permissions.canSee(settleOnly, Classification.INTERNAL, "venture:settle")).isTrue();
        assertThat(permissions.canSee(settleOnly, Classification.INTERNAL, "venture:other")).isFalse();
    }

    @Test
    void restrictedNeedsTheExactScopeNotTheWildcard() {
        assertThat(permissions.canSee(broad, Classification.RESTRICTED, "deal:x")).isFalse();
        assertThat(permissions.canSee(Actor.person("c", Set.of(), Set.of("deal:x")), Classification.RESTRICTED,
                "deal:x")).isTrue();
    }

    @Test
    void privateContextIsOnlyItsOwnersEvenForTheSystemAndAgents() {
        assertThat(permissions.canSee(broad, Classification.PRIVATE, "user:ana")).isTrue();
        assertThat(permissions.canSee(broad, Classification.PRIVATE, "user:ben")).isFalse();
        assertThat(permissions.canSee(Actor.system("routine"), Classification.PRIVATE, "user:ana")).isFalse();
        assertThat(permissions.canSee(Actor.agent("halie", broad), Classification.PRIVATE, "user:ana"))
                .as("an agent acting for Ana sees what Ana sees").isTrue();
        assertThat(permissions.canSee(Actor.agent("halie", settleOnly), Classification.PRIVATE, "user:ana")).isFalse();
    }

    @Test
    void otherTenantsAreNeverVisible() {
        Evidence e = new Evidence();
        e.setTenantId("someone-else");
        e.setClassification(Classification.PUBLIC);
        assertThat(permissions.canSee(broad, e)).isFalse();
    }

    @Test
    void anAgentNeverHasMoreScopeThanItsPrincipal() {
        assertThat(Actor.agent("halie", settleOnly).scopes()).containsExactly("venture:settle");
    }
}
