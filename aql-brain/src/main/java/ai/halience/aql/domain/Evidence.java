package ai.halience.aql.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ai.halience.aql.spec.ObservedInferred;
import ai.halience.aql.spec.Vocabulary.ClaimClass;
import ai.halience.aql.spec.Vocabulary.Sufficiency;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Evidence: a governed observed or extracted claim, fact or value. It keeps provenance and uncertainty; it is not
 * inference, judgement or organisational knowledge. Conflict and absence stay explicit ({@link #conflictState}).
 */
@Getter
@Setter
@Entity
@Table(name = "aql_evidence")
public class Evidence extends AqlRecord {

    public enum ConflictState { NONE, CONFLICT, RECONCILED, UNRESOLVED }

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> sourceRefs = new ArrayList<>();

    @Column(length = 128)
    private String claimType;

    @Column(columnDefinition = "TEXT")
    private String claim;

    @Convert(converter = Json.MapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> value = new LinkedHashMap<>();

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> entityRefs = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private ObservedInferred observedInferred = ObservedInferred.OBSERVED;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private ClaimClass claimClass = ClaimClass.OBSERVED;

    private Double confidence;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Sufficiency sufficiency = Sufficiency.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private ConflictState conflictState = ConflictState.NONE;

    /** When the underlying value was last confirmed fresh at source. */
    private Instant freshAsOf;

    /** Where in the source this came from (page, clause, field), for extraction provenance. */
    @Column(length = 512)
    private String sourceLocation;

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> lineageRefs = new ArrayList<>();

    @Override
    protected String idPrefix() {
        return "evd";
    }
}
