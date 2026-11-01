package ai.halience.aql.spec;

/** The controls that bound AQL behaviour (spec sheet "Governance"). */
public enum GovernanceControl implements SpecCode {
    GV_01("GV-01", "Grounding"),
    GV_02("GV-02", "Claim honesty"),
    GV_03("GV-03", "Confidence & sufficiency"),
    GV_04("GV-04", "Permissions & entitlement"),
    GV_05("GV-05", "Authority"),
    GV_06("GV-06", "Human approval"),
    GV_07("GV-07", "Failure honesty"),
    GV_08("GV-08", "Privacy boundaries"),
    GV_09("GV-09", "Retention & erasure"),
    GV_10("GV-10", "Auditability"),
    GV_11("GV-11", "Versioning"),
    GV_12("GV-12", "Evaluation"),
    GV_13("GV-13", "Materiality"),
    GV_14("GV-14", "Personalisation honesty"),
    GV_15("GV-15", "Regulated perimeter"),
    GV_16("GV-16", "Learning boundary"),
    GV_17("GV-17", "Financial calculation & regulated boundary"),
    GV_18("GV-18", "Work/context permission boundary"),
    GV_19("GV-19", "Organisational knowledge boundary"),
    GV_20("GV-20", "Privacy-safe organisational learning"),
    GV_21("GV-21", "Capture & recording"),
    GV_22("GV-22", "Non-surveillance & psychological safety");

    private final String code;
    private final String title;

    GovernanceControl(String code, String title) {
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
