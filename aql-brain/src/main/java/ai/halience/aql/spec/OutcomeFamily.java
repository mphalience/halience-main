package ai.halience.aql.spec;

/** The canonical 19 Phase 1 outcome families (spec sheet "Outcome Taxonomy"). */
public enum OutcomeFamily implements SpecCode {
    LISTING("Listing"),
    VALUATION("Valuation"),
    OFFER("Offer"),
    TRANSACTION("Transaction"),
    SURVEY_CONDITION("Survey / condition"),
    NEGOTIATION("Negotiation"),
    LEGAL_CONVEYANCING("Legal / conveyancing"),
    MORTGAGE_FINANCE("Mortgage / finance"),
    RENTAL("Rental"),
    OWNERSHIP("Ownership"),
    SELLER("Seller"),
    RISK("Risk"),
    RECOMMENDATION("Recommendation"),
    PROCESS_RELATIONSHIP("Process/Relationship"),
    WATCH("Watch"),
    DOCUMENT_INTELLIGENCE("Document intelligence"),
    SCORE("Score"),
    USER_FEEDBACK("User feedback"),
    GTM("GTM");

    private final String title;

    OutcomeFamily(String title) {
        this.title = title;
    }

    /** Outcome families have no spec code; the canonical title doubles as the code. */
    @Override
    public String code() {
        return title;
    }

    @Override
    public String title() {
        return title;
    }
}
