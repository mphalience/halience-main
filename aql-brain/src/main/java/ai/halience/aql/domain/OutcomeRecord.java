package ai.halience.aql.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ai.halience.aql.spec.ObservedInferred;
import ai.halience.aql.spec.OutcomeFamily;
import ai.halience.aql.spec.Vocabulary.OutcomeSource;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Outcome: an observed or inferred result linked to the prior prediction, recommendation, decision or action it
 * evaluates, with a frozen snapshot of the prior state. Silence is never an outcome: {@link #outcomeSource} is required.
 */
@Getter
@Setter
@Entity
@Table(name = "aql_outcome")
public class OutcomeRecord extends AqlRecord {

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private OutcomeFamily family;

    @Column(length = 128)
    private String eventType;

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> entityRefs = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private ObservedInferred observedInferred;

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private OutcomeSource outcomeSource;

    /** The prior output, recommendation, decision or action this outcome evaluates. */
    @Column(length = 64)
    private String priorRef;

    @Column(length = 64)
    private String snapshotRef;

    @Convert(converter = Json.MapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> result = new LinkedHashMap<>();

    @Column(columnDefinition = "TEXT")
    private String attributionLimits;

    @Override
    protected String idPrefix() {
        return "ocm";
    }
}
