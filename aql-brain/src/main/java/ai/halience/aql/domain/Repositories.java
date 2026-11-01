package ai.halience.aql.domain;

import java.util.List;
import java.util.Optional;

import ai.halience.aql.spec.Vocabulary.KnowledgeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data repositories over the shared contracts. The brain depends on these interfaces only; the physical store
 * (H2 locally, PostgreSQL in deployment, or something else entirely) is a configuration choice.
 */
public final class Repositories {

    private Repositories() {
    }

    public interface SourceRepository extends JpaRepository<SourceRecord, String> {
    }

    public interface EvidenceRepository extends JpaRepository<Evidence, String> {
        @Query("select e from Evidence e where e.tenantId = :tenant and e.correctionState = "
                + "ai.halience.aql.spec.Vocabulary.CorrectionState.CURRENT and e.supersededBy is null and "
                + "(lower(e.claim) like lower(concat('%', :text, '%')) or lower(e.claimType) like lower(concat('%', :text, '%')))")
        List<Evidence> searchCurrent(@Param("tenant") String tenant, @Param("text") String text);
    }

    public interface KnowledgeRepository extends JpaRepository<KnowledgeItem, String> {
        @Query("select k from KnowledgeItem k where k.tenantId = :tenant and k.status in :statuses and "
                + "(lower(k.title) like lower(concat('%', :text, '%')) or lower(k.content) like lower(concat('%', :text, '%')))")
        List<KnowledgeItem> search(@Param("tenant") String tenant, @Param("text") String text,
                @Param("statuses") List<KnowledgeStatus> statuses);
    }

    public interface OutputRepository extends JpaRepository<OutputRecord, String> {
        List<OutputRecord> findByConversationIdOrderByCapturedTimeAsc(String conversationId);
    }

    public interface ActionRepository extends JpaRepository<ActionRecord, String> {
        List<ActionRecord> findByTargetOrderByCapturedTimeDesc(String target);
    }

    public interface ApprovalRepository extends JpaRepository<ApprovalRecord, String> {
        List<ApprovalRecord> findBySubjectRef(String subjectRef);
    }

    public interface DecisionRepository extends JpaRepository<DecisionRecord, String> {
    }

    public interface OutcomeRepository extends JpaRepository<OutcomeRecord, String> {
        List<OutcomeRecord> findByPriorRef(String priorRef);
    }

    public interface LearningRepository extends JpaRepository<LearningRecord, String> {
    }

    public interface SignalEventRepository extends JpaRepository<SignalEvent, String> {
    }

    public interface SnapshotRepository extends JpaRepository<Snapshot, String> {
    }

    public interface WorkContextRepository extends JpaRepository<WorkContextRecord, String> {
    }

    public interface AgentRunRepository extends JpaRepository<AgentRunRecord, String> {
    }

    public interface ConversationRepository extends JpaRepository<Conversation, String> {
        Optional<Conversation> findByChannelAndThreadRef(Conversation.Channel channel, String threadRef);

        List<Conversation> findByOwnerIdAndChannelOrderByLastActivityDesc(String ownerId, Conversation.Channel channel);
    }

    public interface MessageRepository extends JpaRepository<ConversationMessage, String> {
        List<ConversationMessage> findByConversationIdOrderByCapturedTimeAsc(String conversationId);
    }

    public interface AuditRepository extends JpaRepository<AuditEvent, String> {
    }

    public interface PrimitiveRepository extends JpaRepository<PrimitiveRecord, String> {
        Optional<PrimitiveRecord> findByKey(String key);
    }

    public interface PrimitiveRevisionRepository extends JpaRepository<PrimitiveRevision, String> {
        List<PrimitiveRevision> findByPrimitiveKeyOrderByRevisionDesc(String primitiveKey);

        Optional<PrimitiveRevision> findByPrimitiveKeyAndRevision(String primitiveKey, int revision);
    }

    public interface JourneyRunRepository extends JpaRepository<JourneyRun, String> {
        List<JourneyRun> findBySubjectRef(String subjectRef);
    }

    public interface RoutineStateRepository extends JpaRepository<RoutineState, String> {
        Optional<RoutineState> findByRoutine(String routine);
    }
}
