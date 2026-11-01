package ai.halience.aql.spec;

/** The governed results AQL can produce or execute (spec sheet "Outputs & Actions"), with persistence expectation. */
public enum OutputType implements SpecCode {
    OUT_01("OUT-01", "Evidence set", Persistence.PERSISTENT, Authority.READ),
    OUT_02("OUT-02", "Classification / resolution", Persistence.PERSISTENT, Authority.COMPUTE),
    OUT_03("OUT-03", "Derived analysis", Persistence.PERSISTENT, Authority.COMPUTE),
    OUT_04("OUT-04", "Verdict / judgement", Persistence.PERSISTENT, Authority.COMPUTE),
    OUT_05("OUT-05", "Score / ranking", Persistence.PERSISTENT, Authority.COMPUTE),
    OUT_06("OUT-06", "Personalised priority / fit", Persistence.PERSISTENT, Authority.COMPUTE),
    OUT_07("OUT-07", "Comparison / delta", Persistence.CONDITIONAL, Authority.COMPUTE),
    OUT_08("OUT-08", "Answer / explanation", Persistence.CONVERSATION, Authority.READ),
    OUT_09("OUT-09", "Recommendation", Persistence.PERSISTENT, Authority.RECOMMEND),
    OUT_10("OUT-10", "Draft content / communication", Persistence.PERSISTENT, Authority.DRAFT),
    OUT_11("OUT-11", "Report / Pack / Guide artefact", Persistence.PERSISTENT, Authority.CREATE),
    OUT_12("OUT-12", "Material change signal", Persistence.PERSISTENT, Authority.MONITOR),
    OUT_13("OUT-13", "Plan / workflow", Persistence.PERSISTENT, Authority.PLAN),
    OUT_14("OUT-14", "External communication", Persistence.AUDIT, Authority.EXECUTE),
    OUT_15("OUT-15", "System state/tool action", Persistence.AUDIT, Authority.EXECUTE),
    OUT_16("OUT-16", "Evaluation result", Persistence.PERSISTENT, Authority.EVALUATE),
    OUT_17("OUT-17", "Route / escalation", Persistence.AUDIT, Authority.ROUTE),
    OUT_18("OUT-18", "Outcome / calibration record", Persistence.PERSISTENT, Authority.LEARN),
    OUT_19("OUT-19", "Affordability analysis", Persistence.CONDITIONAL, Authority.COMPUTE),
    OUT_20("OUT-20", "Dynamic canvas composition", Persistence.RUNTIME_ONLY, Authority.PLAN),
    OUT_21("OUT-21", "Conversation / interaction state", Persistence.PERSISTENT, Authority.COMPUTE),
    OUT_22("OUT-22", "Contextual artefact / navigation target", Persistence.CONDITIONAL, Authority.ROUTE);

    /** The spec's "Persisted?" column (OPT-14). */
    public enum Persistence {
        PERSISTENT, CONDITIONAL, CONVERSATION, AUDIT, RUNTIME_ONLY;

        /** Whether an output of this kind is written to the output store by default. */
        public boolean storedByDefault() {
            return this == PERSISTENT || this == AUDIT;
        }
    }

    private final String code;
    private final String title;
    private final Persistence persistence;
    private final Authority authority;

    OutputType(String code, String title, Persistence persistence, Authority authority) {
        this.code = code;
        this.title = title;
        this.persistence = persistence;
        this.authority = authority;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String title() {
        return title;
    }

    public Persistence persistence() {
        return persistence;
    }

    /** The execution authority the spec gives this output type. */
    public Authority authority() {
        return authority;
    }
}
