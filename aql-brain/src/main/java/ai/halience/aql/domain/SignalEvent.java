package ai.halience.aql.domain;

import java.util.ArrayList;
import java.util.List;

import ai.halience.aql.spec.ObservedInferred;
import ai.halience.aql.spec.Vocabulary.Initiator;
import ai.halience.aql.spec.Vocabulary.Materiality;
import ai.halience.aql.spec.Vocabulary.OutcomeSource;
import ai.halience.aql.spec.Vocabulary.UserRelevance;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * A captured signal/event in the common Signal Contract shape. Event, captured and effective time, retention class
 * and correction state come from {@link AqlRecord}. {@link ai.halience.aql.signal.SignalContractValidator} enforces
 * which conditional fields are required for which events.
 */
@Getter
@Setter
@Entity
@Table(name = "aql_signal_event")
public class SignalEvent extends AqlRecord {

    /** The SG code this event instantiates, e.g. {@code SG-006}. */
    @Column(length = 16)
    private String signalClass;

    @Column(length = 64, nullable = false)
    private String eventFamily;

    /** Versioned event subtype, e.g. {@code material_risk_change.v1}. */
    @Column(length = 128, nullable = false)
    private String eventType;

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> entityRefs = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Initiator initiator;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private ObservedInferred observedInferred;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private OutcomeSource outcomeSource;

    @Column(length = 64)
    private String evidenceSnapshotRef;

    @Column(length = 128)
    private String modelRuleVersion;

    @Column(length = 64)
    private String predictionRecommendationRef;

    private Double confidence;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Materiality materiality;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private UserRelevance userRelevance;

    @Column(length = 128)
    private String oldValueRef;

    @Column(length = 128)
    private String newValueRef;

    @Column(length = 128)
    private String causeRef;

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> lineageRefs = new ArrayList<>();

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> permissionConsentBasis = new ArrayList<>();

    /** Whether the event involves personal, behavioural, private or otherwise permission-bound data. */
    private boolean personalData;

    /** Whether the event will be used for evaluation or learning (then a frozen snapshot is required). */
    private boolean usedForLearning;

    @Override
    protected String idPrefix() {
        return "evt";
    }
}
