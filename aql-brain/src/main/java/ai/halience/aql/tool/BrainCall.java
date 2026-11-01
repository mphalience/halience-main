package ai.halience.aql.tool;

import java.time.Instant;
import java.util.Collection;

import ai.halience.agent.loop.tool.ToolContext;
import ai.halience.aql.brain.RunRegistry;
import ai.halience.aql.context.ContextEnvelope;
import ai.halience.aql.governance.Actor;
import ai.halience.aql.primitive.ToolManifest;
import ai.halience.aql.spec.Authority;

/**
 * Everything a brain tool knows about the call it is serving: which behaviour called it with what authority, the
 * person or system the run is for (the principal) and the agent acting for them, and the run's context. Tools record
 * the refs they return with {@link #saw}, so the grounding check can tell retrieved provenance from invented provenance.
 */
public record BrainCall(String behaviour, Authority behaviourAuthority, ToolManifest manifest, Actor principal,
                        Actor agent, RunRegistry.RunState run, ToolContext loopContext) {

    public void saw(String ref) {
        if (run != null) {
            run.saw(ref);
        }
    }

    public void saw(Collection<String> refs) {
        refs.forEach(this::saw);
    }

    public ContextEnvelope context() {
        return run == null ? ContextEnvelope.of(principal, null, Instant.now()) : run.context();
    }

    public String tenantId() {
        return principal.tenantId();
    }

    public String conversationId() {
        return run == null ? null : run.context().conversationId();
    }
}
