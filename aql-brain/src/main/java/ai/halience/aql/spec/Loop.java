package ai.halience.aql.spec;

/** The recurring operating cycles AQL supports (spec sheet "Loops"), with the phase each belongs to. */
public enum Loop implements SpecCode {
    LP_01("LP-01", "Canonical intelligence loop", 1),
    LP_02("LP-02", "Companion interaction loop", 1),
    LP_03("LP-03", "Monitoring & materiality loop", 1),
    LP_04("LP-04", "Supervised GTM loop", 1),
    LP_05("LP-05", "Evaluation & control loop", 1),
    LP_06("LP-06", "Outcome & calibration loop", 1),
    LP_07("LP-07", "Knowledge promotion & reconciliation loop", 2),
    LP_08("LP-08", "Attention & exception loop", 2),
    LP_09("LP-09", "Human & agent work loop", 2),
    LP_10("LP-10", "Decision-to-outcome loop", 2),
    LP_11("LP-11", "Strategy & planning loop", 2),
    LP_12("LP-12", "Portfolio value-creation loop", 3),
    LP_13("LP-13", "Cross-product learning loop", 2),
    LP_14("LP-14", "Research & comparison loop", 1),
    LP_15("LP-15", "Approval & reapproval loop", 1),
    LP_16("LP-16", "Information reclassification & remediation loop", 2),
    LP_17("LP-17", "Agent execution & recovery loop", 2),
    LP_18("LP-18", "Investment decision loop", 2),
    LP_19("LP-19", "Venture creation & lifecycle loop", 2),
    LP_20("LP-20", "Operating health & management loop", 3);

    private final String code;
    private final String title;
    private final int phase;

    Loop(String code, String title, int phase) {
        this.code = code;
        this.title = title;
        this.phase = phase;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String title() {
        return title;
    }

    public int phase() {
        return phase;
    }
}
