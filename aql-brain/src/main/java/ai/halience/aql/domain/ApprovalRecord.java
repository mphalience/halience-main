package ai.halience.aql.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Approval: governed authorisation or rejection of one exact proposed action/version, bound by {@link #payloadHash}.
 * Approval does not execute anything and cannot broaden authority; a material change to the payload invalidates it
 * and needs reapproval (LP-15).
 */
@Getter
@Setter
@Entity
@Table(name = "aql_approval")
public class ApprovalRecord extends AqlRecord {

    public enum State { PENDING, APPROVED, REJECTED, EXPIRED, SUPERSEDED }

    /** The action this approves. */
    @Column(length = 64, nullable = false)
    private String subjectRef;

    @Column(length = 64, nullable = false)
    private String payloadHash;

    @Column(length = 128)
    private String requestedBy;

    /** The role an approver must hold, e.g. {@code admin} for primitive edits. */
    @Column(length = 64)
    private String requiredRole;

    @Column(length = 512)
    private String policyBasis;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private State state = State.PENDING;

    @Column(length = 128)
    private String decidedBy;

    private Instant decidedAt;

    @Column(columnDefinition = "TEXT")
    private String reason;

    private Instant expiresAt;

    @Override
    protected String idPrefix() {
        return "apr";
    }
}
