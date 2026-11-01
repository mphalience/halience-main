package ai.halience.aql.governance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.LinkedHashMap;
import java.util.Map;

import ai.halience.aql.Fakes;
import ai.halience.aql.domain.ActionRecord;
import ai.halience.aql.domain.ApprovalRecord;
import ai.halience.aql.domain.Repositories.ActionRepository;
import ai.halience.aql.domain.Repositories.ApprovalRepository;
import ai.halience.aql.domain.Repositories.AuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApprovalServiceTest {

    ActionRepository actions = mock(ActionRepository.class);
    ApprovalRepository approvalRepo = mock(ApprovalRepository.class);
    AuditRepository audits = mock(AuditRepository.class);
    ApprovalService approvals;
    Actor agent = Actor.agent("gtm_publisher", Fakes.person("ana"));

    @BeforeEach
    void setUp() {
        Fakes.backed(actions);
        Fakes.backed(approvalRepo);
        Fakes.backed(audits);
        approvals = new ApprovalService(approvalRepo, actions, new AuditLog(audits), Fakes.CLOCK);
    }

    ActionRecord action() {
        ActionRecord a = new ActionRecord();
        a.setTenantId("halience");
        a.setActionType("publish_content");
        a.setTarget("channel:blog");
        a.setActorId(agent.id());
        a.setPayload(new LinkedHashMap<>(Map.of("title", "Launch", "body", "Settle is live")));
        return actions.save(a);
    }

    @Test
    void anApprovalAuthorisesExactlyThePayloadItWasRequestedFor() {
        ActionRecord a = action();
        ApprovalRecord request = approvals.request(a, agent, "marketing", "publishing needs approval");
        assertThat(a.getStatus()).isEqualTo(ActionRecord.Status.AWAITING_APPROVAL);
        assertThat(approvals.isAuthorised(a)).isFalse();

        approvals.decide(request.getId(), Actor.person("mo", java.util.Set.of("marketing"), java.util.Set.of()), true,
                "ok");
        assertThat(approvals.isAuthorised(a)).isTrue();
        assertThat(a.getStatus()).isEqualTo(ActionRecord.Status.APPROVED);

        a.getPayload().put("body", "Settle is live and the best in the world");
        assertThat(approvals.isAuthorised(a)).as("a material change needs reapproval").isFalse();
        assertThatThrownBy(() -> approvals.requireAuthorised(a)).isInstanceOf(GovernanceException.class);
    }

    @Test
    void onlyAPersonWithTheRoleCanApprove() {
        ApprovalRecord request = approvals.request(action(), agent, "marketing", "basis");
        assertThatThrownBy(() -> approvals.decide(request.getId(), Actor.system("auto"), true, "x"))
                .hasMessageContaining("only a person");
        assertThatThrownBy(() -> approvals.decide(request.getId(), Fakes.person("zed"), true, "x"))
                .hasMessageContaining("marketing role");
    }

    @Test
    void nobodyApprovesTheirOwnRequest() {
        Actor ana = Actor.person("ana", java.util.Set.of("marketing"), java.util.Set.of());
        ActionRecord a = action();
        a.setActorId("ana");
        ApprovalRecord request = approvals.request(a, ana, "marketing", "basis");
        assertThatThrownBy(() -> approvals.decide(request.getId(), ana, true, "mine"))
                .hasMessageContaining("own request");
    }

    @Test
    void aChangeBeforeTheDecisionSupersedesTheRequest() {
        ActionRecord a = action();
        ApprovalRecord request = approvals.request(a, agent, null, "basis");
        a.getPayload().put("title", "Changed");
        assertThatThrownBy(() -> approvals.decide(request.getId(), Fakes.person("mo"), true, "ok"))
                .hasMessageContaining("changed after approval");
        assertThat(approvalRepo.findById(request.getId()).orElseThrow().getState())
                .isEqualTo(ApprovalRecord.State.SUPERSEDED);
    }

    @Test
    void rejectionNeverAuthorises() {
        ActionRecord a = action();
        ApprovalRecord request = approvals.request(a, agent, null, "basis");
        approvals.decide(request.getId(), Fakes.person("mo"), false, "not yet");
        assertThat(approvals.isAuthorised(a)).isFalse();
        assertThat(a.getStatus()).isEqualTo(ActionRecord.Status.REJECTED);
    }

    @Test
    void theHashIgnoresKeyOrderButNotContent() {
        Map<String, Object> one = new LinkedHashMap<>();
        one.put("a", 1);
        one.put("b", Map.of("y", 2, "x", 1));
        Map<String, Object> two = new LinkedHashMap<>();
        two.put("b", Map.of("x", 1, "y", 2));
        two.put("a", 1);
        assertThat(PayloadHasher.hash("t", "x", one)).isEqualTo(PayloadHasher.hash("t", "x", two));
        assertThat(PayloadHasher.hash("t", "x", one)).isNotEqualTo(PayloadHasher.hash("t", "y", one));
    }
}
