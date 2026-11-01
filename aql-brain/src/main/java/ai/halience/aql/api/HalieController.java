package ai.halience.aql.api;

import java.util.List;
import java.util.Map;

import ai.halience.aql.brain.OutputEnvelope;
import ai.halience.aql.domain.Conversation;
import ai.halience.aql.governance.Actor;
import ai.halience.aql.halie.ConversationService;
import ai.halience.aql.halie.HalieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The API Halie's front end calls: conversations, messages and active context. */
@RestController
@RequestMapping("/api/halie/conversations")
public class HalieController {

    private final HalieService halie;
    private final ConversationService conversations;
    private final ActorResolver actors;

    public HalieController(HalieService halie, ConversationService conversations, ActorResolver actors) {
        this.halie = halie;
        this.conversations = conversations;
        this.actors = actors;
    }

    public record StartRequest(Map<String, Object> activeContext) {
    }

    public record MessageRequest(String message, String behaviour, Map<String, Object> context) {
    }

    public record Reply(String conversationId, String behaviour, String text, OutputEnvelope envelope, String outputId,
                        List<String> groundingProblems) {
    }

    @PostMapping
    Map<String, Object> start(@RequestBody(required = false) StartRequest body, HttpServletRequest request) {
        Conversation c = conversations.startHalie(actors.resolve(request), body == null ? null : body.activeContext());
        return Map.of("conversationId", c.getId(), "channel", c.getChannel().name());
    }

    @PostMapping("/{id}/messages")
    Reply message(@PathVariable String id, @RequestBody MessageRequest body, HttpServletRequest request) {
        HalieService.Turn turn = halie.ask(actors.resolve(request), id, body.message(), body.behaviour(),
                body.context());
        return new Reply(turn.conversation().getId(), turn.result().behaviour(), turn.result().text(),
                turn.result().envelope(), turn.result().outputId(), turn.result().groundingProblems());
    }

    @GetMapping("/{id}/messages")
    List<Map<String, Object>> history(@PathVariable String id, HttpServletRequest request) {
        Actor actor = actors.resolve(request);
        return conversations.history(conversations.requireOwned(actor, id)).stream()
                .map(m -> Map.<String, Object>of("role", m.getRole().name(), "content", String.valueOf(m.getContent()),
                        "at", String.valueOf(m.getCapturedTime()), "outputRef", String.valueOf(m.getOutputRef())))
                .toList();
    }

    @PutMapping("/{id}/context")
    Map<String, Object> setContext(@PathVariable String id, @RequestBody Map<String, Object> activeContext,
            HttpServletRequest request) {
        Conversation c = conversations.setActiveContext(conversations.requireOwned(actors.resolve(request), id),
                activeContext);
        return Map.of("conversationId", c.getId(), "activeContext", c.getActiveContext());
    }
}
