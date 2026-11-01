package ai.halience.aql.halie;

import java.util.Locale;
import java.util.Map;

import ai.halience.aql.governance.Actor;
import ai.halience.aql.governance.AuditLog;
import ai.halience.aql.governance.PermissionService;
import ai.halience.aql.spec.Vocabulary.Classification;
import org.springframework.stereotype.Service;

/**
 * Bounded OS actions (PA-043, OUT-22): Open, Review, Ask Halie, Approve and Navigate. Each is checked against the
 * target's permission before a navigation target is returned; an explicit denial is returned (and audited) otherwise.
 * Routing to an action never implies authority to do more than that action: Approve, for example, only opens the
 * approval for the person to decide.
 */
@Service
public class NavigationService {

    public enum OsAction { OPEN, REVIEW, ASK_HALIE, APPROVE, NAVIGATE }

    public record Target(String ref, OsAction action, String destination, String status, boolean permitted,
                         String reason) {
    }

    private final PermissionService permissions;
    private final AuditLog audit;
    private final String adminRole;

    public NavigationService(PermissionService permissions, AuditLog audit,
            ai.halience.aql.config.BrainProperties properties) {
        this.permissions = permissions;
        this.audit = audit;
        this.adminRole = properties.brain().adminRole();
    }

    /**
     * @param requiredRole the role the target's action needs (e.g. an approval's required role), or null
     */
    public Target route(Actor actor, String ref, OsAction action, Classification classification, String scopeRef,
            String status, String requiredRole) {
        if (!permissions.canSee(actor, classification, scopeRef)) {
            audit.record(actor, "navigation_denied", ref, "denied", Map.of("action", action.name()));
            return new Target(ref, action, null, null, false, "Not permitted to access this");
        }
        if (action == OsAction.APPROVE && requiredRole != null && !actor.hasRole(requiredRole)
                && !actor.hasRole(adminRole)) {
            audit.record(actor, "navigation_denied", ref, "denied", Map.of("action", action.name()));
            return new Target(ref, action, null, status, false, "Approving this needs the " + requiredRole
                    + " role; it can be escalated to someone who has it");
        }
        String destination = switch (action) {
            case OPEN -> "/open/" + ref;
            case REVIEW -> "/review/" + ref;
            case ASK_HALIE -> "/halie?about=" + ref;
            case APPROVE -> "/approvals/" + ref;
            case NAVIGATE -> "/" + ref.replace('_', '/');
        };
        return new Target(ref, action, destination, status, true, action.name().toLowerCase(Locale.ROOT)
                + " permitted");
    }
}
