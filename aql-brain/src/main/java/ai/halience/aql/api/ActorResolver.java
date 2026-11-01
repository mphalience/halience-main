package ai.halience.aql.api;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import ai.halience.aql.governance.Actor;
import ai.halience.aql.governance.GovernanceException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Resolves the calling actor. FIRST CUT: identity comes from trusted headers set by the gateway in front of AQL
 * ({@code X-Halience-Actor}, {@code -Roles}, {@code -Scopes}, {@code -Tenant}). Replace with the real identity provider
 * (e.g. an OAuth2 resource server) before AQL is exposed beyond a trusted network.
 */
@Component
public class ActorResolver {

    public Actor resolve(HttpServletRequest request) {
        String id = request.getHeader("X-Halience-Actor");
        if (id == null || id.isBlank()) {
            throw new GovernanceException(GovernanceException.Reason.PERMISSION, "X-Halience-Actor is required");
        }
        return new Actor(id.strip(), Actor.Type.PERSON, request.getHeader("X-Halience-Tenant"),
                split(request.getHeader("X-Halience-Roles")), split(request.getHeader("X-Halience-Scopes")), null);
    }

    private static Set<String> split(String header) {
        if (header == null || header.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(header.split(",")).map(String::strip).filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
