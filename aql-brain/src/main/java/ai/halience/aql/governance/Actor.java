package ai.halience.aql.governance;

import java.util.Objects;
import java.util.Set;

/**
 * Who is acting: a person, an agent (a behaviour running on someone's behalf) or the system (routines, automation).
 * Identity, role, permission and authority are related but distinct: roles say who someone is, {@code scopes} say what
 * they may see, and authority is checked separately at the point of action.
 *
 * @param scopes the typed refs this actor may access (venture, company, product, work context...); {@code *} is all
 *               internal scopes, never private ones
 */
public record Actor(String id, Type type, String tenantId, Set<String> roles, Set<String> scopes, String onBehalfOf) {

    public enum Type { PERSON, AGENT, SYSTEM }

    public static final String ALL_SCOPES = "*";

    public Actor {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        tenantId = tenantId == null || tenantId.isBlank() ? "halience" : tenantId;
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        scopes = scopes == null ? Set.of() : Set.copyOf(scopes);
    }

    public static Actor person(String id, Set<String> roles, Set<String> scopes) {
        return new Actor(id, Type.PERSON, null, roles, scopes, null);
    }

    public static Actor system(String id) {
        return new Actor(id, Type.SYSTEM, null, Set.of("system"), Set.of(ALL_SCOPES), null);
    }

    /** An agent (behaviour) acting for {@code principal}: it never has more access than its principal. */
    public static Actor agent(String behaviour, Actor principal) {
        return new Actor("agent:" + behaviour, Type.AGENT, principal.tenantId(), Set.of(), principal.scopes(),
                principal.id());
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean isPerson() {
        return type == Type.PERSON;
    }
}
