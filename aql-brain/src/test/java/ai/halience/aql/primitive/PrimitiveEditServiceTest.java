package ai.halience.aql.primitive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ai.halience.aql.Fakes;
import ai.halience.aql.config.BrainProperties;
import ai.halience.aql.domain.ActionRecord;
import ai.halience.aql.domain.PrimitiveRecord;
import ai.halience.aql.domain.PrimitiveRevision;
import ai.halience.aql.domain.Repositories.ActionRepository;
import ai.halience.aql.domain.Repositories.ApprovalRepository;
import ai.halience.aql.domain.Repositories.AuditRepository;
import ai.halience.aql.domain.Repositories.PrimitiveRepository;
import ai.halience.aql.domain.Repositories.PrimitiveRevisionRepository;
import ai.halience.aql.governance.Actor;
import ai.halience.aql.governance.ApprovalService;
import ai.halience.aql.governance.AuditLog;
import ai.halience.aql.governance.GovernanceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** The one door: propose, approve, apply and roll back, with an Action record each time. */
class PrimitiveEditServiceTest {

    static final String KEY = "knowledge/launch_notes";
    static final String V1 = "---\nname: launch_notes\ndescription: Launch notes\nstatus: working\nowner: AQL\n---\nFirst.";
    static final String V2 = V1.replace("First.", "Second.");

    PrimitiveRepository records = mock(PrimitiveRepository.class);
    PrimitiveRevisionRepository revisions = mock(PrimitiveRevisionRepository.class);
    ActionRepository actions = mock(ActionRepository.class);
    ApprovalRepository approvalRepo = mock(ApprovalRepository.class);
    AuditRepository audits = mock(AuditRepository.class);
    Map<String, PrimitiveRecord> recordStore;
    Map<String, PrimitiveRevision> revisionStore;
    ApprovalService approvals;
    PrimitiveLibrary library;
    PrimitiveEditService edits;

    Actor admin = Fakes.admin("ana");
    Actor halie = Actor.agent("primitive_editor", admin);

    @BeforeEach
    void setUp() {
        recordStore = Fakes.backed(records);
        revisionStore = Fakes.backed(revisions);
        Fakes.backed(actions);
        Fakes.backed(approvalRepo);
        Fakes.backed(audits);
        when(records.findByKey(anyString())).thenAnswer(i -> recordStore.values().stream()
                .filter(r -> r.getKey().equals(i.getArgument(0))).findFirst());
        when(revisions.findByPrimitiveKeyAndRevision(anyString(), anyInt())).thenAnswer(i -> revisionStore.values()
                .stream().filter(r -> r.getPrimitiveKey().equals(i.getArgument(0))
                        && r.getRevision() == (int) i.getArgument(1)).findFirst());
        when(revisions.findByPrimitiveKeyOrderByRevisionDesc(anyString())).thenAnswer(i -> revisionStore.values()
                .stream().filter(r -> r.getPrimitiveKey().equals(i.getArgument(0)))
                .sorted(Comparator.comparingInt(PrimitiveRevision::getRevision).reversed()).toList());
        BrainProperties properties = BrainProperties.defaults();
        library = new PrimitiveLibrary(records, properties);
        approvals = new ApprovalService(approvalRepo, actions, new AuditLog(audits), Fakes.CLOCK);
        edits = new PrimitiveEditService(records, revisions, actions, approvals, library, new AuditLog(audits),
                properties, Fakes.CLOCK);
    }

    PrimitiveRecord applyNew(String markdown) {
        var proposal = edits.propose(KEY, markdown, "add launch notes", halie);
        approvals.decide(proposal.approval().getId(), admin, true, "fine");
        return edits.apply(proposal.action().getId(), admin);
    }

    @Test
    void aProposalChangesNothingUntilAnAdminApprovesAndApplies() {
        var proposal = edits.propose(KEY, V1, "add launch notes", halie);
        assertThat(proposal.action().getStatus()).isEqualTo(ActionRecord.Status.AWAITING_APPROVAL);
        assertThat(proposal.approval().getRequiredRole()).isEqualTo("admin");
        assertThat(recordStore).isEmpty();
        assertThatThrownBy(() -> edits.apply(proposal.action().getId(), admin)).hasMessageContaining("not approved");

        approvals.decide(proposal.approval().getId(), admin, true, "fine");
        PrimitiveRecord applied = edits.apply(proposal.action().getId(), admin);
        assertThat(applied.getRevision()).isEqualTo(1);
        assertThat(applied.isEdited()).isTrue();
        assertThat(library.current().byKey(KEY)).isPresent();
        assertThat(revisionStore.values()).anyMatch(r -> r.getActionRef().equals(proposal.action().getId()));
    }

    @Test
    void halieCannotApproveOrApplyItsOwnProposal() {
        var proposal = edits.propose(KEY, V1, "x", halie);
        assertThatThrownBy(() -> approvals.decide(proposal.approval().getId(), halie, true, "self"))
                .isInstanceOf(GovernanceException.class);
        assertThatThrownBy(() -> edits.apply(proposal.action().getId(), halie)).hasMessageContaining("Halience admin");
        assertThatThrownBy(() -> approvals.decide(proposal.approval().getId(), Fakes.person("not_admin"), true, "x"))
                .hasMessageContaining("admin role");
    }

    @Test
    void aProposalThatWouldBreakTheBrainIsRefused() {
        String broken = "---\nname: helper\ndescription: d\nprompt_classes: PC-01\nloops: LP-01\ntools: ghost\n---\nb";
        assertThatThrownBy(() -> edits.propose("behaviours/core/helper", broken, "x", halie))
                .hasMessageContaining("unknown tool 'ghost'");
        assertThatThrownBy(() -> edits.propose("knowledge/x", "---\nname: x\n", "x", halie))
                .hasMessageContaining("doesn't parse");
    }

    @Test
    void aStaleProposalMustBeProposedAgain() {
        applyNew(V1);
        var first = edits.propose(KEY, V2, "edit one", halie);
        var second = edits.propose(KEY, V2.replace("Second", "Third"), "edit two", halie);
        approvals.decide(first.approval().getId(), admin, true, "ok");
        approvals.decide(second.approval().getId(), admin, true, "ok");
        edits.apply(first.action().getId(), admin);
        assertThatThrownBy(() -> edits.apply(second.action().getId(), admin)).hasMessageContaining("propose it again");
    }

    @Test
    void anyPrimitiveCanBeRolledBackWithAnActionRecord() {
        applyNew(V1);
        var proposal = edits.propose(KEY, V2, "edit", halie);
        approvals.decide(proposal.approval().getId(), admin, true, "ok");
        edits.apply(proposal.action().getId(), admin);
        assertThat(library.current().byKey(KEY).orElseThrow().body()).isEqualTo("Second.");

        PrimitiveRecord rolled = edits.rollback(KEY, 1, admin, "the edit was wrong");
        assertThat(rolled.getRevision()).isEqualTo(3);
        assertThat(library.current().byKey(KEY).orElseThrow().body()).isEqualTo("First.");
        List<PrimitiveRevision> history = edits.history(KEY);
        assertThat(history).extracting(PrimitiveRevision::getRevision).containsExactly(3, 2, 1);
        assertThatThrownBy(() -> edits.rollback(KEY, 1, Fakes.person("bob"), "x")).hasMessageContaining("admin");
        assertThat(Optional.ofNullable(history.getFirst().getActionRef())).isPresent();
    }
}
