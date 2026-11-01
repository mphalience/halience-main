package ai.halience.aql.halie;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

import ai.halience.aql.brain.BrainRequest;
import ai.halience.aql.brain.BrainResult;
import ai.halience.aql.brain.BrainService;
import ai.halience.aql.context.ContextEnvelope;
import ai.halience.aql.domain.Conversation;
import ai.halience.aql.domain.ConversationMessage;
import ai.halience.aql.governance.Actor;
import ai.halience.aql.governance.GovernanceException;
import ai.halience.aql.spec.Trigger;
import org.springframework.stereotype.Service;

/**
 * Halie's turn handling (LP-02: context → intent → retrieve/reason → respond/compose → route → persist). It assembles
 * the context envelope from the conversation's interaction state, lets the brain classify and run the work (usually the
 * {@code halie} companion behaviour, which delegates to specialists), and persists the exchange and its output ref.
 */
@Service
public class HalieService {

    private final ConversationService conversations;
    private final BrainService brain;
    private final Clock clock;

    public HalieService(ConversationService conversations, BrainService brain, Clock clock) {
        this.conversations = conversations;
        this.brain = brain;
        this.clock = clock;
    }

    public record Turn(Conversation conversation, BrainResult result) {
    }

    /**
     * @param behaviour  a specific behaviour, or null to classify (normally Halie)
     * @param extra      caller context such as the current surface/object (CX-03) or entitlement
     */
    public Turn ask(Actor actor, String conversationId, String message, String behaviour, Map<String, Object> extra) {
        Conversation conversation = conversations.resolve(actor, conversationId, null);
        if (conversation.getChannel() != Conversation.Channel.HALIE) {
            throw new GovernanceException(GovernanceException.Reason.INVALID,
                    "That is a person thread; Halie answers in a Halie conversation");
        }
        conversations.append(conversation, ConversationMessage.Role.USER, actor.id(), message, null);
        ContextEnvelope context = new ContextEnvelope(actor, Trigger.TR_01, Instant.now(clock), conversation.getId(),
                conversation.getChannel().name(), conversation.getActiveContext(), conversation.getReferents(),
                extra == null ? Map.of() : extra);
        String prompt = withHistory(conversation, message);
        BrainResult result = brain.run(new BrainRequest(behaviour, prompt, context, null, null));
        conversations.append(conversation, ConversationMessage.Role.HALIE, "halie", result.text(), result.outputId());
        return new Turn(conversation, result);
    }

    /** The last few turns, so a follow-up resolves against what was just said. */
    private String withHistory(Conversation conversation, String message) {
        var history = conversations.history(conversation);
        if (history.size() <= 1) {
            return message;
        }
        StringBuilder out = new StringBuilder("Earlier in this conversation:\n");
        history.subList(Math.max(0, history.size() - 9), history.size() - 1).forEach(m -> out.append("- ")
                .append(m.getRole().name().toLowerCase()).append(": ")
                .append(m.getContent() == null ? "" : m.getContent().length() > 600 ? m.getContent().substring(0, 600)
                        + "..." : m.getContent()).append('\n'));
        return out.append("\nNow: ").append(message).toString();
    }
}
