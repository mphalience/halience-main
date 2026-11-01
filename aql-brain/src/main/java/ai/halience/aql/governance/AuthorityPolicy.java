package ai.halience.aql.governance;

import ai.halience.aql.config.BrainProperties;
import ai.halience.aql.primitive.ToolManifest;
import ai.halience.aql.spec.Authority;
import ai.halience.aql.spec.Vocabulary.ApprovalMode;
import ai.halience.aql.spec.Vocabulary.Consequence;
import org.springframework.stereotype.Component;

/**
 * Point-of-action authority (GV-05, GV-06). Recommendation, draft and execution are separate levels; capability never
 * creates authority. Approval policy: {@code required} always needs an approval, {@code policy} needs one at or above
 * the configured consequence threshold, {@code none} never does.
 */
@Component
public class AuthorityPolicy {

    private final Consequence threshold;

    public AuthorityPolicy(BrainProperties properties) {
        this.threshold = properties.brain().approvalThreshold();
    }

    public boolean requiresApproval(ApprovalMode mode, Consequence consequence) {
        return switch (mode) {
            case REQUIRED -> true;
            case POLICY -> consequence.compareTo(threshold) >= 0;
            case NONE -> false;
        };
    }

    public boolean requiresApproval(ToolManifest tool) {
        return requiresApproval(tool.approval(), tool.consequence());
    }

    /** Throws unless {@code granted} covers {@code needed}. */
    public void requireAuthority(String who, Authority granted, Authority needed) {
        if (!granted.covers(needed)) {
            throw new GovernanceException(GovernanceException.Reason.AUTHORITY, who + " has authority "
                    + granted.wireValue() + " and can't do something that needs " + needed.wireValue());
        }
    }
}
