package ai.halience.aql.halie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ai.halience.aql.Fakes;
import ai.halience.aql.config.BrainProperties;
import ai.halience.aql.domain.Conversation;
import ai.halience.aql.domain.ConversationMessage;
import ai.halience.aql.domain.Repositories.AuditRepository;
import ai.halience.aql.domain.Repositories.ConversationRepository;
import ai.halience.aql.domain.Repositories.MessageRepository;
import ai.halience.aql.governance.Actor;
import ai.halience.aql.governance.AuditLog;
import ai.halience.aql.governance.GovernanceException;
import ai.halience.aql.governance.PermissionService;
import ai.halience.aql.halie.CanvasComposer.Item;
import ai.halience.aql.halie.CanvasComposer.Region;
import ai.halience.aql.spec.Vocabulary.Classification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Conversation state, channels, canvas composition and bounded navigation (PA-039 to PA-043). */
class HalieInteractionTest {

    ConversationRepository conversationRepo = mock(ConversationRepository.class);
    MessageRepository messages = mock(MessageRepository.class);
    AuditRepository audits = mock(AuditRepository.class);
    ConversationService conversations;
    Actor ana = Fakes.person("ana");

    @BeforeEach
    void setUp() {
        Map<String, Conversation> store = Fakes.backed(conversationRepo);
        Fakes.backed(messages);
        Fakes.backed(audits);
        when(conversationRepo.findByChannelAndThreadRef(any(), any())).thenAnswer(i -> store.values().stream()
                .filter(c -> c.getChannel() == i.getArgument(0) && i.getArgument(1).equals(c.getThreadRef()))
                .findFirst());
        conversations = new ConversationService(conversationRepo, messages, Fakes.CLOCK);
    }

    @Test
    void referentsAreNewestFirstDeduplicatedAndBounded() {
        Conversation c = conversations.startHalie(ana, Map.of("venture", "settle"));
        conversations.refer(c, "req_1", "Watch requirement", "requirement");
        conversations.refer(c, "req_2", "Score requirement", "requirement");
        conversations.refer(c, "req_1", "Watch requirement", "requirement");
        assertThat(c.getReferents()).extracting(r -> r.get("ref")).containsExactly("req_1", "req_2");
        for (int i = 0; i < 30; i++) {
            conversations.refer(c, "x_" + i, "x", "artefact");
        }
        assertThat(c.getReferents()).hasSize(ConversationService.MAX_REFERENTS);
    }

    @Test
    void aHalieConversationIsPrivateToItsOwner() {
        Conversation c = conversations.startHalie(ana, null);
        assertThat(c.getClassification()).isEqualTo(Classification.PRIVATE);
        assertThatThrownBy(() -> conversations.requireOwned(Fakes.person("ben"), c.getId()))
                .hasMessageContaining("private");
        assertThat(conversations.requireOwned(Actor.agent("halie", ana), c.getId())).isSameAs(c);
    }

    @Test
    void personThreadsAreASeparateChannelHalieNeverPostsInto() {
        Conversation thread = conversations.resolve(ana, null, "slack:C123");
        assertThat(thread.getChannel()).isEqualTo(Conversation.Channel.PEOPLE);
        assertThat(conversations.resolve(ana, null, "slack:C123")).isSameAs(thread);
        assertThatThrownBy(() -> conversations.append(thread, ConversationMessage.Role.HALIE, "halie", "hi", null))
                .isInstanceOf(GovernanceException.class);
        Conversation halie = conversations.resolve(ana, null, null);
        assertThat(halie.getChannel()).isEqualTo(Conversation.Channel.HALIE);
        assertThat(halie.getId()).isNotEqualTo(thread.getId());
    }

    @Test
    void referentsTheUserCanNoLongerSeeArePruned() {
        Conversation c = conversations.startHalie(ana, null);
        conversations.refer(c, "keep", "k", "t");
        conversations.refer(c, "drop", "d", "t");
        conversations.pruneReferents(c, ref -> !ref.equals("drop"));
        assertThat(c.getReferents()).extracting(r -> r.get("ref")).containsExactly("keep");
    }

    @Test
    void theCanvasOnlyPlacesWhatTheActorMaySeeInTheActiveScope() {
        Actor settleOnly = Actor.person("ben", Set.of(), Set.of("venture:settle"));
        List<Item> items = List.of(
                new Item("dec_1", "Pricing decision", Region.ATTENTION, "venture:settle", Classification.INTERNAL,
                        "canonical", "needs your decision"),
                new Item("sig_1", "Competitor launch", Region.PULSE, "venture:settle", Classification.INTERNAL,
                        "working", "material change"),
                new Item("deal_1", "Confidential deal", Region.WORK, "deal:x", Classification.RESTRICTED, null, null),
                new Item("other_1", "Other venture", Region.WORK, "venture:other", Classification.INTERNAL, null, null));
        CanvasComposer.Canvas canvas = CanvasComposer.compose(settleOnly, Map.of("venture", "settle"),
                "venture:settle", items, new PermissionService(), 5);
        assertThat(canvas.regions().get(Region.ATTENTION)).extracting(Item::ref).containsExactly("dec_1");
        assertThat(canvas.regions().get(Region.PULSE)).extracting(Item::ref).containsExactly("sig_1");
        assertThat(canvas.regions().get(Region.WORK)).isEmpty();
        assertThat(canvas.excluded()).doesNotContain("deal_1", "other_1");
    }

    @Test
    void navigationIsBoundedByPermissionAndRole() {
        NavigationService navigation = new NavigationService(new PermissionService(), new AuditLog(audits),
                BrainProperties.defaults());
        NavigationService.Target open = navigation.route(ana, "req_1", NavigationService.OsAction.OPEN,
                Classification.INTERNAL, "venture:settle", "canonical", null);
        assertThat(open.permitted()).isTrue();
        assertThat(open.destination()).isEqualTo("/open/req_1");
        NavigationService.Target approve = navigation.route(ana, "apr_1", NavigationService.OsAction.APPROVE,
                Classification.INTERNAL, null, null, "finance");
        assertThat(approve.permitted()).isFalse();
        assertThat(approve.reason()).contains("finance");
        NavigationService.Target hidden = navigation.route(ana, "deal_1", NavigationService.OsAction.OPEN,
                Classification.RESTRICTED, "deal:x", null, null);
        assertThat(hidden.permitted()).isFalse();
        assertThat(hidden.destination()).isNull();
        assertThat(Optional.ofNullable(hidden.status())).isEmpty();
    }
}
