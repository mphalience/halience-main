package ai.halience.aql.spec;

/** The context AQL needs to interpret a task (spec sheet "Context & Inputs"). Context never expands permission. */
public enum ContextDomain implements SpecCode {
    CX_01("CX-01", "User / Household identity"),
    CX_02("CX-02", "Plan + capability entitlement"),
    CX_03("CX-03", "Current surface/object"),
    CX_04("CX-04", "Canonical entity identity"),
    CX_05("CX-05", "Evidence/provenance state"),
    CX_06("CX-06", "Current derived intelligence"),
    CX_07("CX-07", "Priorities/preferences/baseline"),
    CX_08("CX-08", "Bounded Process state"),
    CX_09("CX-09", "Ongoing Relationship state"),
    CX_10("CX-10", "Save / Library context"),
    CX_11("CX-11", "Conversation/task history"),
    CX_12("CX-12", "Action/approval state"),
    CX_13("CX-13", "Temporal context"),
    CX_14("CX-14", "Jurisdiction/effective legal context"),
    CX_15("CX-15", "Brand/companion configuration"),
    CX_16("CX-16", "Requirements/delivery context"),
    CX_17("CX-17", "Campaign/content context"),
    CX_18("CX-18", "Quality/control context"),
    CX_19("CX-19", "Affordability / household financial context"),
    CX_20("CX-20", "Active Halience context & referents"),
    CX_21("CX-21", "Conversation / channel state"),
    CX_22("CX-22", "Organisational artefact context");

    private final String code;
    private final String title;

    ContextDomain(String code, String title) {
        this.code = code;
        this.title = title;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String title() {
        return title;
    }
}
