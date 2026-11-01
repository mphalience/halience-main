package ai.halience.aql.spec;

/**
 * Small controlled vocabularies from the spec's Signal Contract, Governance and Options sheets. Grouped in one place so
 * the shared contracts read the same everywhere.
 */
public final class Vocabulary {

    private Vocabulary() {
    }

    /** Who or what caused capture (Signal Contract {@code initiator}). Initiator is not a prompt class. */
    public enum Initiator { USER, SYSTEM, AQL, AUTOMATION, EXTERNAL_EVENT }

    /** Signal Contract {@code materiality}. */
    public enum Materiality { NOT_MATERIAL, MATERIAL, CRITICAL }

    /** Signal Contract {@code user_relevance}. */
    public enum UserRelevance { RELEVANT, NOT_RELEVANT }

    /** Signal Contract {@code correction_revocation_state}. Learning/evaluation consumers honour current validity. */
    public enum CorrectionState { CURRENT, CORRECTED, REVOKED, ERASED_ANONYMISED }

    /** Signal Contract {@code outcome_source}: how an outcome is known. There is deliberately no "no feedback". */
    public enum OutcomeSource { USER_CONFIRMED, PUBLIC_RECORD, SYSTEM_EVENT, PROFESSIONAL_DOCUMENT, AQL_OUTPUT, INFERENCE }

    /** GV-02 claim honesty: sourced/observed, derived/modelled, or interpretive/judgement. */
    public enum ClaimClass { OBSERVED, DERIVED, INTERPRETIVE }

    /** GV-03: whether enough relevant evidence exists. Separate from confidence. */
    public enum Sufficiency { SUFFICIENT, PARTIAL, INSUFFICIENT, UNKNOWN }

    /** GV-07 failure honesty: typed failure/degradation states. */
    public enum FailureState { NONE, DEGRADED, FAILED, BLOCKED, UNKNOWN }

    /** Consequence class of an action or output, used for approval policy and escalation. */
    public enum Consequence { NONE, LOW, MEDIUM, HIGH, CRITICAL }

    /** How a tool or milestone is approved: never, by policy threshold, or always. */
    public enum ApprovalMode { NONE, POLICY, REQUIRED }

    /** Governed status of knowledge (GV-19): accessible material is not organisational truth by default. */
    public enum KnowledgeStatus { SOURCE, WORKING, PROPOSED, APPROVED, CANONICAL, SUPERSEDED, RESTRICTED }

    /** Sensitivity/access classification carried on governed records (GV-08, GV-18). */
    public enum Classification { PUBLIC, INTERNAL, RESTRICTED, PRIVATE }

    /** The routing outcome of PC-17 Route & Escalate, driven by confidence, sufficiency and consequence (GV-03). */
    public enum Route { NONE, ANSWER_WITH_LIMITS, HUMAN_REVIEW, FALLBACK, ESCALATE, BLOCK }
}
