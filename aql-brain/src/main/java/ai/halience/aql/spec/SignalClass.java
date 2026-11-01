package ai.halience.aql.spec;

/**
 * The governed changes and states AQL can observe (spec sheet "Data Signals"). The layer and gate encode the signal
 * model: raw source event → evidence change → intelligence change → material signal → trigger → outcome.
 */
public enum SignalClass implements SpecCode {
    SG_001("SG-001", Layer.RAW_EVENT, "Source record update", ObservedInferred.OBSERVED, Gate.NO),
    SG_002("SG-002", Layer.RAW_EVENT, "Source freshness/health", ObservedInferred.OBSERVED, Gate.OPERATIONAL),
    SG_003("SG-003", Layer.EVIDENCE, "Normalised fact change", ObservedInferred.OBSERVED, Gate.CONDITIONAL),
    SG_004("SG-004", Layer.EVIDENCE, "Evidence conflict", ObservedInferred.OBSERVED, Gate.YES),
    SG_005("SG-005", Layer.EVIDENCE, "Known unknown / evidence gap", ObservedInferred.INFERRED, Gate.YES),
    SG_006("SG-006", Layer.INTELLIGENCE, "Risk finding/change", ObservedInferred.INFERRED, Gate.YES),
    SG_007("SG-007", Layer.INTELLIGENCE, "Price/value finding/change", ObservedInferred.INFERRED, Gate.YES),
    SG_008("SG-008", Layer.INTELLIGENCE, "Cost finding/change", ObservedInferred.INFERRED, Gate.YES),
    SG_009("SG-009", Layer.INTELLIGENCE, "Property Score change", ObservedInferred.INFERRED, Gate.YES),
    SG_010("SG-010", Layer.INTELLIGENCE, "Area Score change", ObservedInferred.INFERRED, Gate.YES),
    SG_011("SG-011", Layer.PERSONALISATION, "Preference / must-have change", ObservedInferred.OBSERVED, Gate.NO),
    SG_012("SG-012", Layer.PERSONALISATION, "Settle Score change", ObservedInferred.INFERRED, Gate.CONDITIONAL),
    SG_013("SG-013", Layer.DOCUMENT, "Document uploaded/received/versioned", ObservedInferred.OBSERVED, Gate.NO),
    SG_014("SG-014", Layer.DOCUMENT, "Document finding/conflict", ObservedInferred.INFERRED, Gate.YES),
    SG_015("SG-015", Layer.PROCESS, "Process state transition", ObservedInferred.OBSERVED, Gate.CONDITIONAL),
    SG_016("SG-016", Layer.PROCESS, "Deadline / time-window signal", ObservedInferred.INFERRED, Gate.YES),
    SG_017("SG-017", Layer.RELATIONSHIP, "Ongoing home-management signal", ObservedInferred.INFERRED, Gate.YES),
    SG_018("SG-018", Layer.BEHAVIOUR, "User intent/interaction", ObservedInferred.OBSERVED, Gate.NO),
    SG_019("SG-019", Layer.OUTCOME, "Property/process outcome", ObservedInferred.OBSERVED, Gate.YES),
    SG_020("SG-020", Layer.OUTCOME, "Action outcome", ObservedInferred.OBSERVED, Gate.YES),
    SG_021("SG-021", Layer.GTM, "Research/market signal", ObservedInferred.OBSERVED, Gate.YES),
    SG_022("SG-022", Layer.GTM, "Content/campaign event", ObservedInferred.OBSERVED, Gate.OPERATIONAL),
    SG_023("SG-023", Layer.GTM, "Performance outcome", ObservedInferred.OBSERVED, Gate.YES),
    SG_024("SG-024", Layer.DELIVERY, "Requirement/delivery state change", ObservedInferred.OBSERVED, Gate.YES),
    SG_025("SG-025", Layer.CONTROL, "Evaluation result", ObservedInferred.INFERRED, Gate.YES),
    SG_026("SG-026", Layer.CONTROL, "Model/rule/version change", ObservedInferred.OBSERVED, Gate.OPERATIONAL),
    SG_027("SG-027", Layer.CONTROL, "Permission/authority change", ObservedInferred.OBSERVED, Gate.YES),
    SG_028("SG-028", Layer.CONTROL, "Failure/degradation/incident", ObservedInferred.OBSERVED, Gate.YES),
    SG_029("SG-029", Layer.BEHAVIOUR, "User correction / challenge", ObservedInferred.OBSERVED, Gate.YES),
    SG_030("SG-030", Layer.BEHAVIOUR, "User preference/edit event", ObservedInferred.OBSERVED, Gate.NO),
    SG_031("SG-031", Layer.KNOWLEDGE, "Governed knowledge change", ObservedInferred.OBSERVED, Gate.CONDITIONAL),
    SG_032("SG-032", Layer.OUTCOME, "Outcome-family event", ObservedInferred.INFERRED, Gate.NO),
    SG_033("SG-033", Layer.PERSONALISATION, "Affordability input/change", ObservedInferred.OBSERVED, Gate.NO),
    SG_034("SG-034", Layer.INTELLIGENCE, "Affordability finding/change", ObservedInferred.INFERRED, Gate.CONDITIONAL),
    SG_035("SG-035", Layer.RELATIONSHIP, "Household/property budget & cost optimisation signal", ObservedInferred.INFERRED, Gate.YES),
    SG_036("SG-036", Layer.INTERACTION, "Active context / referent change", ObservedInferred.OBSERVED, Gate.NO),
    SG_037("SG-037", Layer.INTERACTION, "Conversation/channel state change", ObservedInferred.OBSERVED, Gate.NO),
    SG_038("SG-038", Layer.INTERFACE, "Explicit canvas/context recomposition request", ObservedInferred.OBSERVED, Gate.NO);

    public enum Layer {
        RAW_EVENT, EVIDENCE, INTELLIGENCE, PERSONALISATION, DOCUMENT, PROCESS, RELATIONSHIP, BEHAVIOUR, OUTCOME, GTM,
        DELIVERY, CONTROL, KNOWLEDGE, INTERACTION, INTERFACE
    }

    /** The spec's "Materiality Gate" column (OPT-09). */
    public enum Gate { NO, YES, CONDITIONAL, OPERATIONAL }

    private final String code;
    private final Layer layer;
    private final String title;
    private final ObservedInferred epistemicStatus;
    private final Gate materialityGate;

    SignalClass(String code, Layer layer, String title, ObservedInferred epistemicStatus, Gate materialityGate) {
        this.code = code;
        this.layer = layer;
        this.title = title;
        this.epistemicStatus = epistemicStatus;
        this.materialityGate = materialityGate;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String title() {
        return title;
    }

    public Layer layer() {
        return layer;
    }

    public ObservedInferred epistemicStatus() {
        return epistemicStatus;
    }

    public Gate materialityGate() {
        return materialityGate;
    }

    /**
     * Whether this is a user-controlled edit (preferences, saved state, affordability inputs). These may recompute
     * personal fit but must never be emitted as external Watch change events (SG-011, SG-012 note, SG-030, SG-033).
     */
    public boolean isUserEdit() {
        return this == SG_011 || this == SG_030 || this == SG_033;
    }

    /** Whether the signal is operational: it may route to AQL Operations without a user-facing materiality pass. */
    public boolean isOperational() {
        return materialityGate == Gate.OPERATIONAL || this == SG_028;
    }
}
