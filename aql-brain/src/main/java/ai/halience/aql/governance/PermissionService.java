package ai.halience.aql.governance;

import ai.halience.aql.domain.AqlRecord;
import ai.halience.aql.spec.Vocabulary.Classification;
import org.springframework.stereotype.Component;

/**
 * Deterministic permission checks (GV-04, GV-18): whether an actor may see a record, checked before anything reaches
 * a model or a tool result. An LLM cannot override these. Knowing or retrieving something never grants permission to
 * retain, share or act on it; that is {@link AuthorityPolicy}'s job.
 * <ul>
 *   <li>Records in another tenant are never visible.</li>
 *   <li>PUBLIC and INTERNAL records are visible to actors with that scope (or {@code *}).</li>
 *   <li>RESTRICTED records need the exact scope; {@code *} is not enough.</li>
 *   <li>PRIVATE records are visible only to their owner, recorded as the scope {@code user:<id>}. Nobody else,
 *       including the system, sees private context by default (GV-22 non-surveillance).</li>
 * </ul>
 */
@Component
public class PermissionService {

    public boolean canSee(Actor actor, AqlRecord record) {
        if (record.getTenantId() != null && !record.getTenantId().equals(actor.tenantId())) {
            return false;
        }
        return canSee(actor, record.getClassification(), record.getScopeRef());
    }

    public boolean canSee(Actor actor, Classification classification, String scopeRef) {
        Classification c = classification == null ? Classification.INTERNAL : classification;
        String principal = actor.type() == Actor.Type.AGENT && actor.onBehalfOf() != null ? actor.onBehalfOf()
                : actor.id();
        return switch (c) {
            case PUBLIC -> true;
            case INTERNAL -> scopeRef == null || actor.scopes().contains(Actor.ALL_SCOPES)
                    || actor.scopes().contains(scopeRef);
            case RESTRICTED -> scopeRef != null && actor.scopes().contains(scopeRef);
            case PRIVATE -> scopeRef != null && scopeRef.equals("user:" + principal);
        };
    }

    public void requireSee(Actor actor, AqlRecord record) {
        if (!canSee(actor, record)) {
            throw new GovernanceException(GovernanceException.Reason.PERMISSION,
                    "Not permitted to access " + record.getId());
        }
    }
}
