package ai.halience.aql.halie;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ai.halience.aql.domain.Conversation;
import ai.halience.aql.domain.Conversation.Channel;
import ai.halience.aql.domain.ConversationMessage;
import ai.halience.aql.domain.Repositories.ConversationRepository;
import ai.halience.aql.domain.Repositories.MessageRepository;
import ai.halience.aql.governance.Actor;
import ai.halience.aql.governance.GovernanceException;
import ai.halience.aql.governance.GovernanceException.Reason;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistent conversation and interaction state (OUT-21, PA-039, PA-041): history, active context, recent referents
 * and the channel. Halie conversations and People/Message threads are separate channels: a person thread is never
 * merged into a Halie conversation, and a Halie conversation is private to its owner (GV-22).
 */
@Service
public class ConversationService {

    /** How many referents to keep, newest first. */
    public static final int MAX_REFERENTS = 20;

    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final Clock clock;

    public ConversationService(ConversationRepository conversations, MessageRepository messages, Clock clock) {
        this.conversations = conversations;
        this.messages = messages;
        this.clock = clock;
    }

    @Transactional
    public Conversation startHalie(Actor owner, Map<String, Object> activeContext) {
        Conversation c = new Conversation();
        c.setTenantId(owner.tenantId());
        c.setOwnerId(owner.id());
        c.setChannel(Channel.HALIE);
        c.setScopeRef("user:" + owner.id());
        c.setClassification(ai.halience.aql.spec.Vocabulary.Classification.PRIVATE);
        c.setActiveContext(activeContext == null ? new LinkedHashMap<>() : new LinkedHashMap<>(activeContext));
        c.setLastActivity(Instant.now(clock));
        return conversations.save(c);
    }

    /**
     * Resolves which channel an interaction belongs to (PA-041). A {@code threadRef} means a People/Message thread,
     * which gets its own conversation record and is never the Halie conversation.
     */
    @Transactional
    public Conversation resolve(Actor actor, String conversationId, String threadRef) {
        if (threadRef != null && !threadRef.isBlank()) {
            return conversations.findByChannelAndThreadRef(Channel.PEOPLE, threadRef).orElseGet(() -> {
                Conversation c = new Conversation();
                c.setTenantId(actor.tenantId());
                c.setOwnerId(actor.id());
                c.setChannel(Channel.PEOPLE);
                c.setThreadRef(threadRef);
                c.setScopeRef("thread:" + threadRef);
                c.setLastActivity(Instant.now(clock));
                return conversations.save(c);
            });
        }
        if (conversationId == null) {
            return startHalie(actor, Map.of());
        }
        return requireOwned(actor, conversationId);
    }

    public Conversation requireOwned(Actor actor, String conversationId) {
        Conversation c = conversations.findById(conversationId)
                .orElseThrow(() -> new GovernanceException(Reason.NOT_FOUND, "No conversation " + conversationId));
        String principal = actor.type() == Actor.Type.AGENT && actor.onBehalfOf() != null ? actor.onBehalfOf()
                : actor.id();
        if (c.getChannel() == Channel.HALIE && !c.getOwnerId().equals(principal)) {
            throw new GovernanceException(Reason.PERMISSION, "A Halie conversation is private to its owner");
        }
        return c;
    }

    @Transactional
    public ConversationMessage append(Conversation c, ConversationMessage.Role role, String authorId, String content,
            String outputRef) {
        if (c.getChannel() == Channel.PEOPLE && role == ConversationMessage.Role.HALIE) {
            throw new GovernanceException(Reason.INVALID, "Halie doesn't post into a person thread");
        }
        ConversationMessage m = new ConversationMessage();
        m.setTenantId(c.getTenantId());
        m.setConversationId(c.getId());
        m.setRole(role);
        m.setAuthorId(authorId);
        m.setContent(content);
        m.setOutputRef(outputRef);
        m.setScopeRef(c.getScopeRef());
        m.setClassification(c.getClassification());
        c.setLastActivity(Instant.now(clock));
        conversations.save(c);
        return messages.save(m);
    }

    public List<ConversationMessage> history(Conversation c) {
        return messages.findByConversationIdOrderByCapturedTimeAsc(c.getId());
    }

    /** Puts a referent at the front (deduplicated by ref), keeping at most {@link #MAX_REFERENTS}. */
    @Transactional
    public Conversation refer(Conversation c, String ref, String label, String type) {
        List<Map<String, Object>> next = new ArrayList<>();
        Map<String, Object> referent = new LinkedHashMap<>();
        referent.put("ref", ref);
        referent.put("label", label);
        referent.put("type", type);
        referent.put("at", Instant.now(clock).toString());
        next.add(referent);
        for (Map<String, Object> existing : c.getReferents()) {
            if (!ref.equals(existing.get("ref")) && next.size() < MAX_REFERENTS) {
                next.add(existing);
            }
        }
        c.setReferents(next);
        return conversations.save(c);
    }

    /** Explicit context entry/exit (SG-036, TR-13). A null context returns to "All Halience". */
    @Transactional
    public Conversation setActiveContext(Conversation c, Map<String, Object> activeContext) {
        c.setActiveContext(activeContext == null ? new LinkedHashMap<>() : new LinkedHashMap<>(activeContext));
        return conversations.save(c);
    }

    /** Drops referents the actor may no longer see (only authorised context may be retained). */
    @Transactional
    public Conversation pruneReferents(Conversation c, java.util.function.Predicate<String> stillVisible) {
        c.setReferents(new ArrayList<>(c.getReferents().stream()
                .filter(r -> stillVisible.test(String.valueOf(r.get("ref")))).toList()));
        return conversations.save(c);
    }
}
