package ai.halience.aql.brain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ai.halience.aql.brain.OutputEnvelope.Claim;
import ai.halience.aql.spec.Vocabulary.ClaimClass;
import ai.halience.aql.spec.Vocabulary.Sufficiency;

/**
 * A deterministic grounding check (GV-01, GV-02) applied to every envelope before it leaves the brain:
 * <ul>
 *   <li>an observed claim with no evidence ref is not grounded: it is downgraded to insufficient and noted as a known
 *       unknown, rather than passed off as fact;</li>
 *   <li>a claim citing an evidence ref the run never saw (not in {@code knownRefs}) is flagged the same way, so a model
 *       can't invent provenance. When {@code knownRefs} is null the check is skipped (nothing to compare with).</li>
 * </ul>
 */
public final class GroundingCheck {

    private GroundingCheck() {
    }

    public record Result(OutputEnvelope envelope, List<String> problems) {
        public boolean grounded() {
            return problems.isEmpty();
        }
    }

    public static Result apply(OutputEnvelope envelope, Set<String> knownRefs) {
        List<Claim> claims = new ArrayList<>();
        List<String> unknowns = new ArrayList<>(envelope.knownUnknowns());
        List<String> problems = new ArrayList<>();
        for (Claim claim : envelope.claims()) {
            List<String> unseen = knownRefs == null ? List.of()
                    : claim.evidenceRefs().stream().filter(r -> !knownRefs.contains(r)).toList();
            boolean ungrounded = claim.claimClass() == ClaimClass.OBSERVED && claim.evidenceRefs().isEmpty();
            if (ungrounded || !unseen.isEmpty()) {
                String problem = ungrounded ? "Observed claim has no evidence: \"" + claim.text() + "\""
                        : "Claim cites evidence the run never retrieved " + unseen + ": \"" + claim.text() + "\"";
                problems.add(problem);
                unknowns.add(problem);
                List<String> kept = claim.evidenceRefs().stream().filter(r -> !unseen.contains(r)).toList();
                claims.add(new Claim(claim.text(), claim.claimClass(), kept, claim.confidence(),
                        Sufficiency.INSUFFICIENT));
            } else {
                claims.add(claim);
            }
        }
        return new Result(envelope.withClaimsAndUnknowns(claims, unknowns), problems);
    }
}
