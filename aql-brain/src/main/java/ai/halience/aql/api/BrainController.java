package ai.halience.aql.api;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import ai.halience.aql.brain.BrainRequest;
import ai.halience.aql.brain.BrainResult;
import ai.halience.aql.brain.BrainService;
import ai.halience.aql.context.ContextEnvelope;
import ai.halience.aql.primitive.PrimitiveLibrary;
import ai.halience.aql.spec.SpecCode;
import ai.halience.aql.spec.Trigger;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Direct access to brain jobs for products and systems (e.g. Settle calling Ask Settle or a scorer). The trigger and
 * context say who or what is asking; they never add permission.
 */
@RestController
@RequestMapping("/api/brain")
public class BrainController {

    private final BrainService brain;
    private final PrimitiveLibrary library;
    private final ActorResolver actors;
    private final Clock clock;

    public BrainController(BrainService brain, PrimitiveLibrary library, ActorResolver actors, Clock clock) {
        this.brain = brain;
        this.library = library;
        this.actors = actors;
        this.clock = clock;
    }

    public record RunRequest(String behaviour, String prompt, String trigger, Map<String, Object> activeContext,
                             Map<String, Object> context) {
    }

    @PostMapping("/run")
    BrainResult run(@RequestBody RunRequest body, HttpServletRequest request) {
        Trigger trigger = body.trigger() == null ? Trigger.TR_01 : SpecCode.require(Trigger.class, body.trigger());
        ContextEnvelope context = new ContextEnvelope(actors.resolve(request), trigger, Instant.now(clock), null, null,
                body.activeContext(), List.of(), body.context());
        return brain.run(BrainRequest.of(body.behaviour(), body.prompt(), context));
    }

    @GetMapping("/behaviours")
    List<Map<String, Object>> behaviours() {
        return library.current().agents().stream().map(p -> Map.<String, Object>of("name", p.name(),
                "folder", p.folder(), "phase", p.phase(), "enabled", library.isEnabled(p),
                "description", p.description())).toList();
    }
}
