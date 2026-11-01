package ai.halience.aql.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ai.halience.aql.spec.Authority;
import ai.halience.aql.spec.Vocabulary.ApprovalMode;
import ai.halience.aql.spec.Vocabulary.Consequence;
import ai.halience.aql.spec.Vocabulary.Initiator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Action: a proposed, prepared or executed operation affecting system/external state or communicating externally.
 * Every primitive edit and every consequential tool call goes through one of these ("the one door"): the payload is
 * hashed so an approval binds to exactly what will run, and before/after state supports audit and rollback.
 */
@Getter
@Setter
@Entity
@Table(name = "aql_action")
public class ActionRecord extends AqlRecord {

    public enum Status {
        PROPOSED, AWAITING_APPROVAL, APPROVED, REJECTED, EXECUTING, EXECUTED, FAILED, ROLLED_BACK, CANCELLED;

        public boolean isTerminal() {
            return this == REJECTED || this == EXECUTED || this == FAILED || this == ROLLED_BACK || this == CANCELLED;
        }
    }

    @Column(length = 64, nullable = false)
    private String actionType;

    /** What the action is applied to: a primitive key, a recipient, a channel, an external resource ref. */
    @Column(length = 512)
    private String target;

    @Column(length = 64)
    private String tool;

    @Column(length = 128)
    private String actorId;

    @Column(length = 16)
    private String actorType;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Initiator initiator;

    @Column(length = 64)
    private String workContextRef;

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> entityRefs = new ArrayList<>();

    @Convert(converter = Json.MapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> payload = new LinkedHashMap<>();

    @Column(length = 64)
    private String payloadHash;

    @Enumerated(EnumType.STRING)
    @Column(length = 24)
    private Authority authorityRequired;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private ApprovalMode approvalMode = ApprovalMode.NONE;

    @Column(length = 64)
    private String approvalRef;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Consequence consequence = Consequence.NONE;

    private boolean reversible = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 24, nullable = false)
    private Status status = Status.PROPOSED;

    /** Why: the recommendation/decision/request this action carries out. */
    @Column(columnDefinition = "TEXT")
    private String basis;

    @Convert(converter = Json.MapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> beforeState = new LinkedHashMap<>();

    @Convert(converter = Json.MapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> afterState = new LinkedHashMap<>();

    @Column(columnDefinition = "TEXT")
    private String result;

    private Instant executedAt;

    @Column(length = 128)
    private String idempotencyKey;

    @Override
    protected String idPrefix() {
        return "act";
    }
}
