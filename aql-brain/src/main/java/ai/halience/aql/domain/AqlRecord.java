package ai.halience.aql.domain;

import java.time.Instant;

import ai.halience.aql.spec.Vocabulary.Classification;
import ai.halience.aql.spec.Vocabulary.CorrectionState;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

/**
 * Fields every governed AQL object carries (Shared Object Contracts, AR-01, AR-02, GV-08/09):
 * <ul>
 *   <li>a typed id and the tenant/scope it belongs to;</li>
 *   <li>the three times the spec separates: when it happened ({@code eventTime}), when AQL captured it
 *       ({@code capturedTime}) and when it is valid ({@code effectiveTime});</li>
 *   <li>classification and retention class, so access and erasure rules can be applied;</li>
 *   <li>correction state and supersession, so history is corrected without being silently rewritten.</li>
 * </ul>
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AqlRecord {

    @Id
    @Column(length = 64)
    private String id;

    @Column(length = 64)
    private String tenantId;

    /** The scope this belongs to: a venture, company, product, user, work context... as a typed ref. */
    @Column(length = 128)
    private String scopeRef;

    private Instant eventTime;

    @Column(nullable = false)
    private Instant capturedTime;

    private Instant effectiveTime;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private Classification classification = Classification.INTERNAL;

    @Column(length = 64, nullable = false)
    private String retentionClass = "standard";

    @Enumerated(EnumType.STRING)
    @Column(length = 24, nullable = false)
    private CorrectionState correctionState = CorrectionState.CURRENT;

    /** The id of the record that replaced this one as current, if any. */
    @Column(length = 64)
    private String supersededBy;

    @Version
    private long version;

    /** The id prefix for this record type, e.g. {@code evd}. */
    protected abstract String idPrefix();

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = Ids.next(idPrefix());
        }
        if (capturedTime == null) {
            capturedTime = Instant.now();
        }
    }

    /** Assigns the id now (before persisting) so it can be referenced, e.g. in an approval hash. */
    public String ensureId() {
        if (id == null) {
            id = Ids.next(idPrefix());
        }
        return id;
    }

    public boolean isCurrent() {
        return correctionState == CorrectionState.CURRENT && supersededBy == null;
    }
}
