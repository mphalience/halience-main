package ai.halience.aql.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ai.halience.aql.spec.Vocabulary.FailureState;
import ai.halience.aql.spec.Vocabulary.Route;
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
 * The common output envelope for what AQL produces: Intelligence, Judgement, Score, Personalisation, Recommendation,
 * comparisons, drafts, evaluations and routes. One envelope lets deterministic calculations, rules and LLM-derived
 * analysis share lineage, confidence and versioning without pretending they are the same mechanism; {@link #kind} and
 * {@link #outputType} say which it is.
 */
@Getter
@Setter
@Entity
@Table(name = "aql_output")
public class OutputRecord extends AqlRecord {

    /** The shared object contract this output is an instance of. */
    public enum Kind {
        EVIDENCE_SET, CLASSIFICATION, INTELLIGENCE, JUDGEMENT, SCORE, PERSONALISATION, COMPARISON, ANSWER,
        RECOMMENDATION, DRAFT, ARTEFACT, CHANGE_SIGNAL, PLAN, COMMUNICATION, ACTION_RESULT, EVALUATION, ROUTE,
        OUTCOME, CALCULATION, CANVAS, INTERACTION_STATE, NAVIGATION
    }

    /** The OUT code, e.g. {@code OUT-04}. */
    @Column(length = 16)
    private String outputType;

    @Enumerated(EnumType.STRING)
    @Column(length = 24)
    private Kind kind;

    /** The behaviour that produced it, and the revision of that behaviour (GV-11). */
    @Column(length = 64)
    private String behaviour;

    private Integer behaviourRevision;

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> promptClasses = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Convert(converter = Json.ObjectListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Map<String, Object>> claims = new ArrayList<>();

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> evidenceRefs = new ArrayList<>();

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> knownUnknowns = new ArrayList<>();

    private Double confidence;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Sufficiency sufficiency = Sufficiency.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private FailureState failureState = FailureState.NONE;

    @Enumerated(EnumType.STRING)
    @Column(length = 24)
    private Route route = Route.NONE;

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> methodVersions = new ArrayList<>();

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> entityRefs = new ArrayList<>();

    @Column(length = 64)
    private String conversationId;

    /** Structured payload specific to the kind (score breakdown, calculation outputs, canvas regions...). */
    @Convert(converter = Json.MapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> data = new LinkedHashMap<>();

    @Override
    protected String idPrefix() {
        return "out";
    }
}
