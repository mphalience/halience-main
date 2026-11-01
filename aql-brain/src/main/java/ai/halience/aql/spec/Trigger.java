package ai.halience.aql.spec;

/** What starts or re-runs AQL work (spec sheet "Triggers & Automation"). A trigger never implies authority. */
public enum Trigger implements SpecCode {
    TR_01("TR-01", "Explicit user request", false),
    TR_02("TR-02", "Contextual UI action", true),
    TR_03("TR-03", "Source/data event", true),
    TR_04("TR-04", "Document event", false),
    TR_05("TR-05", "Process state event", true),
    TR_06("TR-06", "Time/schedule", false),
    TR_07("TR-07", "Material change", true),
    TR_08("TR-08", "Evaluation/control event", false),
    TR_09("TR-09", "Human approval", true),
    TR_10("TR-10", "Outcome event", true),
    TR_11("TR-11", "Delivery-system event", true),
    TR_12("TR-12", "GTM workflow event", true),
    TR_13("TR-13", "Halience context/channel event", true);

    private final String code;
    private final String title;
    private final boolean requiresPriorState;

    Trigger(String code, String title, boolean requiresPriorState) {
        this.code = code;
        this.title = title;
        this.requiresPriorState = requiresPriorState;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String title() {
        return title;
    }

    /** The spec's "Requires Prior State?" column, where it is a firm Yes. */
    public boolean requiresPriorState() {
        return requiresPriorState;
    }
}
