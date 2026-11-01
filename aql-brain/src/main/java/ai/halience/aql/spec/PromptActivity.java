package ai.halience.aql.spec;

import static ai.halience.aql.spec.Authority.COMPUTE;
import static ai.halience.aql.spec.Authority.CREATE;
import static ai.halience.aql.spec.Authority.DRAFT;
import static ai.halience.aql.spec.Authority.EVALUATE;
import static ai.halience.aql.spec.Authority.EXECUTE;
import static ai.halience.aql.spec.Authority.EXECUTE_WITH_APPROVAL;
import static ai.halience.aql.spec.Authority.LEARN;
import static ai.halience.aql.spec.Authority.MONITOR;
import static ai.halience.aql.spec.Authority.PLAN;
import static ai.halience.aql.spec.Authority.READ;
import static ai.halience.aql.spec.Authority.RECOMMEND;
import static ai.halience.aql.spec.Authority.ROUTE;
import static ai.halience.aql.spec.PromptClass.*;

/**
 * The concrete Phase 1 activities (spec sheet "Prompt Activities"), each instantiating a Prompt Class for one
 * consumer with the authority the spec gives it. Every one must be implemented by at least one Phase 1 behaviour.
 */
public enum PromptActivity implements SpecCode {
    PA_001("PA-001", PC_01, "Settle", READ, "Retrieve current Property/Area evidence for a lookup"),
    PA_002("PA-002", PC_05, "Settle", COMPUTE, "Derive property fundamentals, costs and intelligence dimensions"),
    PA_003("PA-003", PC_06, "Settle", COMPUTE, "Determine directional Price Verdict from valuation vs asking price"),
    PA_004("PA-004", PC_07, "Settle", COMPUTE, "Compute Property Score and Area Score"),
    PA_005("PA-005", PC_08, "Settle", COMPUTE, "Compute personalised Settle Score / priorities"),
    PA_006("PA-006", PC_09, "Settle", COMPUTE, "Compare Properties, Areas or reference baselines"),
    PA_007("PA-007", PC_10, "Ask Settle", READ, "Explain a fact, risk, score, verdict, document item or next step"),
    PA_008("PA-008", PC_11, "Settle", RECOMMEND, "Recommend questions/actions arising from property intelligence"),
    PA_009("PA-009", PC_13, "Settle Watch", MONITOR, "Detect material Property/Area intelligence change"),
    PA_010("PA-010", PC_13, "Settle Watch", MONITOR, "Determine whether score movement is consequence of external change"),
    PA_011("PA-011", PC_02, "Settle", COMPUTE, "Extract facts/terms/entities from uploaded document"),
    PA_012("PA-012", PC_04, "Settle", COMPUTE, "Reconcile document evidence against canonical/public evidence"),
    PA_013("PA-013", PC_10, "Ask Settle", READ, "Explain a document or contradiction"),
    PA_014("PA-014", PC_12, "Settle", DRAFT, "Draft communication from current Process/property evidence"),
    PA_015("PA-015", PC_12, "Settle Reveal", CREATE, "Generate report narrative/artefact from current intelligence"),
    PA_016("PA-016", PC_12, "Settle", CREATE, "Generate/pre-populate Sales Pack or Tenancy Pack"),
    PA_017("PA-017", PC_14, "Settle", PLAN, "Generate/update next-step plan for Buy/Sell/Rent/Let"),
    PA_018("PA-018", PC_11, "Settle", RECOMMEND, "Prioritise what matters now in a Process/Relationship"),
    PA_019("PA-019", PC_15, "Settle", EXECUTE, "Execute approved communication/action via connected tool"),
    PA_020("PA-020", PC_17, "Settle; AQL", ROUTE, "Escalate consequential, ambiguous or low-confidence case"),
    PA_021("PA-021", PC_01, "Halie", READ, "Retrieve canonical requirements, decisions, docs and dependencies"),
    PA_022("PA-022", PC_09, "Halie", COMPUTE, "Compare requirement state with issue tracker, repository and technical evidence"),
    PA_023("PA-023", PC_13, "Halie", MONITOR, "Detect blocker, stale status or material delivery change"),
    PA_024("PA-024", PC_10, "Halie", READ, "Explain current Halience surface/object and navigate to source"),
    PA_025("PA-025", PC_01, "GTM", READ, "Research audience, market, competitors, channels or claim evidence"),
    PA_026("PA-026", PC_12, "GTM", DRAFT, "Generate campaign/content asset from approved brief/evidence"),
    PA_027("PA-027", PC_16, "GTM", EVALUATE, "Validate content for grounding, brand, privacy and commercial rules"),
    PA_028("PA-028", PC_15, "GTM", EXECUTE_WITH_APPROVAL, "Publish approved asset/campaign"),
    PA_029("PA-029", PC_05, "GTM", COMPUTE, "Analyse campaign/content performance"),
    PA_030("PA-030", PC_18, "GTM", LEARN, "Connect content/campaign choices to observed outcomes"),
    PA_031("PA-031", PC_16, "AQL Operations", EVALUATE, "Evaluate generated output against grounding/quality criteria"),
    PA_032("PA-032", PC_13, "AQL Operations", MONITOR, "Monitor source/model/integration health"),
    PA_033("PA-033", PC_17, "AQL Operations", ROUTE, "Route failed/degraded capability to fallback or human"),
    PA_034("PA-034", PC_18, "AQL", LEARN, "Capture observed Settle outcome against prior prediction/recommendation"),
    PA_035("PA-035", PC_19, "Settle Explore; Ask Settle", COMPUTE, "Calculate indicative and Property-contextual affordability"),
    PA_036("PA-036", PC_19, "Settle Reveal", COMPUTE, "Calculate Reveal affordability scenarios and sensitivity"),
    PA_037("PA-037", PC_07, "Settle Reveal", COMPUTE, "Use affordability as a personal-fit dimension within Settle Score"),
    PA_038("PA-038", PC_11, "Settle", RECOMMEND, "Identify budgeting, cost-reduction and property-finance actions"),
    PA_039("PA-039", PC_10, "Halie", READ, "Continue a Halie conversation using recent referents and active context"),
    PA_040("PA-040", PC_14, "Halience OS; Halie", PLAN, "Compose or recompose the Halience OS canvas"),
    PA_041("PA-041", PC_03, "Halience OS; Halie", COMPUTE, "Resolve Halie vs person/message channel and retain its state"),
    PA_042("PA-042", PC_01, "Halience OS; Halie", READ, "Retrieve and surface an authorised artefact with provenance/status"),
    PA_043("PA-043", PC_17, "Halience OS; Halie", ROUTE, "Route a bounded OS action to the permitted target");

    private final String code;
    private final PromptClass promptClass;
    private final String consumer;
    private final Authority authority;
    private final String title;

    PromptActivity(String code, PromptClass promptClass, String consumer, Authority authority, String title) {
        this.code = code;
        this.promptClass = promptClass;
        this.consumer = consumer;
        this.authority = authority;
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

    public PromptClass promptClass() {
        return promptClass;
    }

    public String consumer() {
        return consumer;
    }

    public Authority authority() {
        return authority;
    }
}
