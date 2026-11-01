package ai.halience.aql.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.halience.aql.Fakes;
import ai.halience.aql.config.BrainProperties;
import ai.halience.aql.domain.ActionRecord;
import ai.halience.aql.domain.ApprovalRecord;
import ai.halience.aql.domain.Repositories.ActionRepository;
import ai.halience.aql.domain.Repositories.ApprovalRepository;
import ai.halience.aql.domain.Repositories.AuditRepository;
import ai.halience.aql.governance.Actor;
import ai.halience.aql.governance.ApprovalService;
import ai.halience.aql.governance.AuditLog;
import ai.halience.aql.governance.AuthorityPolicy;
import ai.halience.aql.governance.GovernanceException;
import ai.halience.aql.ops.HealthRegistry;
import ai.halience.aql.spec.Vocabulary.ApprovalMode;
import ai.halience.aql.spec.Vocabulary.Consequence;
import ai.halience.aql.spec.Vocabulary.FailureState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActionServiceTest {

    ActionRepository actions = mock(ActionRepository.class);
    ApprovalRepository approvalRepo = mock(ApprovalRepository.class);
    AuditRepository audits = mock(AuditRepository.class);
    ApprovalService approvals;
    HealthRegistry health = new HealthRegistry(Fakes.CLOCK);
    List<ActionRecord> sent = new ArrayList<>();
    ActionService service;
    Actor agent = Actor.agent("action_executor", Fakes.person("ana"));

    @BeforeEach
    void setUp() {
        Fakes.backed(actions);
        Fakes.backed(approvalRepo);
        Fakes.backed(audits);
        AuditLog audit = new AuditLog(audits);
        approvals = new ApprovalService(approvalRepo, actions, audit, Fakes.CLOCK);
        Connector email = new Connector() {
            public String channel() {
                return "email";
            }

            public String execute(ActionRecord action) {
                sent.add(action);
                return "sent:msg_1";
            }
        };
        Connector broken = new Connector() {
            public String channel() {
                return "crm";
            }

            public String execute(ActionRecord action) throws Exception {
                throw new java.io.IOException("crm unreachable");
            }
        };
        service = new ActionService(actions, approvals, new AuthorityPolicy(BrainProperties.defaults()), audit, health,
                List.of(email, broken), Fakes.CLOCK);
    }

    ActionRecord propose(String channel, Consequence consequence) {
        return service.propose("send_message", channel, "seller@example.com", Map.of("body", "Is the boiler new?"),
                consequence, ApprovalMode.POLICY, "PA-014 draft approved by the buyer", agent);
    }

    @Test
    void aConsequentialActionDoesNotRunWithoutApproval() {
        ActionRecord a = propose("email", Consequence.HIGH);
        assertThat(service.needsApproval(a)).isTrue();
        assertThatThrownBy(() -> service.execute(a.getId(), "email", "send_message", agent))
                .isInstanceOf(GovernanceException.class);
        assertThat(sent).isEmpty();

        ApprovalRecord request = approvals.request(a, agent, null, "send to seller");
        approvals.decide(request.getId(), Fakes.person("ana_as_person"), true, "yes");
        ActionRecord done = service.execute(a.getId(), "email", "send_message", agent);
        assertThat(done.getStatus()).isEqualTo(ActionRecord.Status.EXECUTED);
        assertThat(done.getResult()).isEqualTo("sent:msg_1");
        assertThat(sent).hasSize(1);
    }

    @Test
    void executionIsIdempotent() {
        ActionRecord a = propose("email", Consequence.LOW);
        service.execute(a.getId(), "email", "send_message", agent);
        service.execute(a.getId(), "email", "send_message", agent);
        assertThat(sent).hasSize(1);
    }

    @Test
    void aFailureIsRecordedAsAFailureNotSuccess() {
        ActionRecord a = propose("crm", Consequence.LOW);
        ActionRecord done = service.execute(a.getId(), "crm", "send_message", agent);
        assertThat(done.getStatus()).isEqualTo(ActionRecord.Status.FAILED);
        assertThat(done.getResult()).contains("crm unreachable");
        assertThat(health.state("connector:crm")).isEqualTo(FailureState.FAILED);
    }

    @Test
    void withoutAConnectorTheActionGoesToTheOutboxAndSaysSo() {
        ActionRecord a = propose("post", Consequence.LOW);
        ActionRecord done = service.execute(a.getId(), "post", "send_message", agent);
        assertThat(done.getResult()).startsWith("queued_for_manual_completion");
    }

    @Test
    void oneToolCantRunAnotherKindOfAction() {
        ActionRecord a = propose("email", Consequence.LOW);
        assertThatThrownBy(() -> service.execute(a.getId(), "publish", "publish_content", agent))
                .hasMessageContaining("not publish_content");
    }
}
