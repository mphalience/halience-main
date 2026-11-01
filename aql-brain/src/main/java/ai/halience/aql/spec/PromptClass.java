package ai.halience.aql.spec;

/** The reusable brain jobs AQL can perform (spec sheet "Prompt Classes"). They describe the job, not the surface. */
public enum PromptClass implements SpecCode {
    PC_01("PC-01", "Retrieve & Ground", ActionPotential.NO),
    PC_02("PC-02", "Extract & Structure", ActionPotential.INDIRECT),
    PC_03("PC-03", "Classify & Resolve", ActionPotential.INDIRECT),
    PC_04("PC-04", "Reconcile & Validate Evidence", ActionPotential.INDIRECT),
    PC_05("PC-05", "Analyse & Derive", ActionPotential.INDIRECT),
    PC_06("PC-06", "Evaluate & Judge", ActionPotential.INDIRECT),
    PC_07("PC-07", "Score & Rank", ActionPotential.INDIRECT),
    PC_08("PC-08", "Personalise & Prioritise", ActionPotential.INDIRECT),
    PC_09("PC-09", "Compare", ActionPotential.INDIRECT),
    PC_10("PC-10", "Explain & Answer", ActionPotential.NO),
    PC_11("PC-11", "Recommend & Next-Best-Action", ActionPotential.YES),
    PC_12("PC-12", "Generate & Transform", ActionPotential.YES),
    PC_13("PC-13", "Monitor & Detect Change", ActionPotential.YES),
    PC_14("PC-14", "Plan & Orchestrate", ActionPotential.YES),
    PC_15("PC-15", "Execute & Communicate", ActionPotential.YES),
    PC_16("PC-16", "Evaluate System Quality", ActionPotential.YES),
    PC_17("PC-17", "Route & Escalate", ActionPotential.YES),
    PC_18("PC-18", "Learn & Calibrate", ActionPotential.INDIRECT),
    PC_19("PC-19", "Calculate", ActionPotential.INDIRECT);

    /** The spec's "May Trigger Action?" column. */
    public enum ActionPotential { NO, INDIRECT, YES }

    private final String code;
    private final String title;
    private final ActionPotential mayTriggerAction;

    PromptClass(String code, String title, ActionPotential mayTriggerAction) {
        this.code = code;
        this.title = title;
        this.mayTriggerAction = mayTriggerAction;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String title() {
        return title;
    }

    public ActionPotential mayTriggerAction() {
        return mayTriggerAction;
    }
}
