package ai.halience.aql.spec;

/** Representative end-to-end loop compositions (spec sheet "Loop Chains"); realised as journeys. */
public enum LoopChain implements SpecCode {
    CH_01("CH-01", "Research → decision → action → learning"),
    CH_02("CH-02", "Monitoring → materiality → attention → response"),
    CH_03("CH-03", "Companion request → intelligence → bounded action → response"),
    CH_04("CH-04", "Capture → organisational knowledge → later remediation"),
    CH_05("CH-05", "Supervised GTM lifecycle"),
    CH_06("CH-06", "Investment opportunity → decision → outcome"),
    CH_07("CH-07", "Venture creation → launch → intervention/learning"),
    CH_08("CH-08", "Operating health → exception → coordinated response"),
    CH_09("CH-09", "Strategy → plan → execution → replan"),
    CH_10("CH-10", "Local learning → cross-product learning → governed knowledge"),
    CH_11("CH-11", "Evaluation failure → remediation/recovery"),
    CH_12("CH-12", "Information correction/reclassification → downstream remediation");

    private final String code;
    private final String title;

    LoopChain(String code, String title) {
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
