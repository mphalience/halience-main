package ai.halience.aql.brain;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.halience.agent.loop.AgentLoopResult;
import ai.halience.agent.loop.LoopRequest;
import ai.halience.agent.loop.Scope;
import ai.halience.agent.loop.agent.Agent;
import ai.halience.agent.loop.execution.TerminationReason;
import ai.halience.aql.config.BrainProperties;
import ai.halience.aql.context.ContextEnvelope;
import ai.halience.aql.domain.AgentRunRecord;
import ai.halience.aql.domain.OutputRecord;
import ai.halience.aql.domain.Repositories.AgentRunRepository;
import ai.halience.aql.domain.Repositories.OutputRepository;
import ai.halience.aql.governance.SufficiencyRouter;
import ai.halience.aql.ops.HealthRegistry;
import ai.halience.aql.primitive.BehaviourSpec;
import ai.halience.aql.primitive.Primitive;
import ai.halience.aql.primitive.PrimitiveLibrary;
import ai.halience.aql.spec.Authority;
import ai.halience.aql.spec.OutputType;
import ai.halience.aql.spec.SpecCode;
import ai.halience.aql.spec.Vocabulary.Consequence;
import ai.halience.aql.spec.Vocabulary.FailureState;
import ai.halience.aql.spec.Vocabulary.Route;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Runs one behaviour as a bounded Agent Run and turns its answer into a governed output:
 * <ol>
 *   <li>records the run (behaviour and revision, initiator, permitted tools, authority, thinking);</li>
 *   <li>runs the agent with the context envelope as its operating context and the output envelope as its answer
 *       schema, with step and cost caps;</li>
 *   <li>applies the deterministic grounding check against the evidence the run actually retrieved;</li>
 *   <li>routes by sufficiency, confidence, consequence and failure (taking the stricter of the model's route and the
 *       rule's);</li>
 *   <li>persists the output by its type's persistence rule, with the behaviour revision for versioning.</li>
 * </ol>
 * A run that fails is recorded as failed and returns a failure envelope; it never comes back as a success.
 */
@Slf4j
@Component
public class BehaviourRunner {

    private final BrainAgents agents;
    private final PrimitiveLibrary library;
    private final RunRegistry runs;
    private final AgentRunRepository runRecords;
    private final OutputRepository outputs;
    private final HealthRegistry health;
    private final BrainProperties properties;
    private final Clock clock;

    public BehaviourRunner(BrainAgents agents, PrimitiveLibrary library, RunRegistry runs,
            AgentRunRepository runRecords, OutputRepository outputs, HealthRegistry health, BrainProperties properties,
            Clock clock) {
        this.agents = agents;
        this.library = library;
        this.runs = runs;
        this.runRecords = runRecords;
        this.outputs = outputs;
        this.health = health;
        this.properties = properties;
        this.clock = clock;
    }

    public BrainResult run(BrainRequest request) {
        Primitive primitive = agents.requireEnabled(request.behaviour());
        BehaviourSpec spec = BehaviourSpec.of(primitive);
        ContextEnvelope context = request.context();
        boolean admin = context.actor().hasRole(properties.brain().adminRole()) && context.actor().isPerson();
        String outputType = spec.outputs().isEmpty() ? OutputType.OUT_08.code() : spec.outputs().getFirst();
        Consequence consequence = request.consequence() != null ? request.consequence() : consequenceOf(spec);

        RunRegistry.RunState run = runs.open(context);
        AgentRunRecord record = new AgentRunRecord();
        record.setTenantId(context.actor().tenantId());
        record.setBehaviour(spec.name());
        record.setBehaviourRevision(library.revision(primitive.key()));
        record.setInitiatedBy(context.actor().id());
        record.setTrigger(context.trigger().code());
        record.setObjective(truncate(request.prompt(), 2000));
        record.setPermittedTools(new ArrayList<>(spec.tools()));
        record.setAuthority(spec.authority());
        record.setThinking(spec.thinking().wireValue());
        record.setBudgetCents(properties.brain().maxCostCentsPerRun());
        record.setEventTime(Instant.now(clock));
        record = runRecords.save(record);

        try {
            Agent agent = agents.agent(spec.name(), admin);
            String session = context.conversationId() != null ? context.conversationId() : run.runId();
            LoopRequest.LoopRequestBuilder loop = LoopRequest.builder()
                    .prompt(request.prompt())
                    .operatingContext(context.render())
                    .scope(Scope.of(context.actor().tenantId(), context.actor().id(), session))
                    .tags(Map.of(RunRegistry.RUN_TAG, run.runId(), "behaviour", spec.name()))
                    .answerSchema(request.answerSchema() != null ? request.answerSchema() : OutputEnvelopes.schema())
                    .maxCostUsdCents(properties.brain().maxCostCentsPerRun());
            AgentLoopResult result = agents.runtime().runAndWait(agent, loop);
            String text = result.finalResponse() == null ? "" : result.finalResponse().getContent();
            Map<String, Object> structured = result.structuredAnswer();

            OutputEnvelope envelope = request.answerSchema() != null
                    ? OutputEnvelope.textOnly(outputType, text).withRoute(Route.NONE, null)
                    : OutputEnvelopes.parse(structured, outputType, text);
            GroundingCheck.Result grounding = GroundingCheck.apply(envelope, run.seenRefs());
            envelope = grounding.envelope();
            if (result.execution() != null && result.execution().terminationReason() != TerminationReason.COMPLETED
                    && envelope.failureState() == FailureState.NONE) {
                envelope = new OutputEnvelope(envelope.outputType(), envelope.summary(), envelope.claims(),
                        append(envelope.knownUnknowns(), "The run stopped early ("
                                + result.execution().terminationReason() + "), so the answer may be incomplete"),
                        FailureState.DEGRADED, envelope.proposedActions(), envelope.route(), envelope.routeReason(),
                        envelope.methodVersions());
            }
            Route ruled = SufficiencyRouter.route(envelope.overallSufficiency(), envelope.minConfidence(),
                    consequence, envelope.failureState());
            if (envelope.claims().isEmpty() && request.answerSchema() == null && envelope.failureState() == FailureState.NONE) {
                ruled = Route.NONE; // a conversational answer with no factual claims needs no routing
            }
            if (ruled.ordinal() > envelope.route().ordinal()) {
                envelope = envelope.withRoute(ruled, "AQL routing rule: sufficiency " + envelope.overallSufficiency()
                        + ", consequence " + consequence + ", failure " + envelope.failureState());
            }
            String outputId = persist(envelope, spec, primitive, context, grounding);
            double cost = result.usage() == null ? 0 : result.usage().costUsdMicros() / 10_000.0;
            record.setState(result.execution() != null
                    && result.execution().terminationReason() == TerminationReason.COMPLETED
                    ? AgentRunRecord.State.COMPLETED : AgentRunRecord.State.WRAPPED_UP);
            if (envelope.route() == Route.ESCALATE || envelope.route() == Route.HUMAN_REVIEW) {
                record.setState(AgentRunRecord.State.ESCALATED);
            }
            record.setOutputRef(outputId);
            record.setCostCents(cost);
            record.setFinishedAt(Instant.now(clock));
            runRecords.save(record);
            health.report("behaviour:" + spec.name(), FailureState.NONE, "ok");
            return new BrainResult(spec.name(), record.getId(), envelope, outputId, grounding.problems(), structured,
                    cost);
        } catch (RuntimeException e) {
            log.warn("Behaviour {} failed: {}", spec.name(), e.toString());
            record.setState(AgentRunRecord.State.FAILED);
            record.setFailure(e.getClass().getSimpleName() + ": " + e.getMessage());
            record.setFinishedAt(Instant.now(clock));
            runRecords.save(record);
            health.report("behaviour:" + spec.name(), FailureState.FAILED, e.getMessage());
            Route route = SufficiencyRouter.route(null, null, consequence, FailureState.FAILED);
            OutputEnvelope failed = new OutputEnvelope(outputType,
                    "I couldn't complete this: the work failed before it produced a result.", List.of(),
                    List.of("The run failed: " + e.getMessage()), FailureState.FAILED, List.of(), route,
                    "run failed", List.of());
            return new BrainResult(spec.name(), record.getId(), failed, null, List.of(), null, 0);
        } finally {
            runs.close(run.runId());
        }
    }

    private String persist(OutputEnvelope envelope, BehaviourSpec spec, Primitive primitive, ContextEnvelope context,
            GroundingCheck.Result grounding) {
        OutputType type = SpecCode.parse(OutputType.class, envelope.outputType()).orElse(OutputType.OUT_08);
        if (type.persistence() == OutputType.Persistence.RUNTIME_ONLY) {
            return null;
        }
        OutputRecord o = new OutputRecord();
        o.setTenantId(context.actor().tenantId());
        Object scope = context.activeContext().get("scope");
        o.setScopeRef(scope == null ? null : String.valueOf(scope));
        if (context.conversationId() != null && "HALIE".equals(context.channel())) {
            o.setScopeRef("user:" + context.actor().id());
            o.setClassification(ai.halience.aql.spec.Vocabulary.Classification.PRIVATE);
        }
        o.setOutputType(type.code());
        o.setKind(kindOf(type));
        o.setBehaviour(spec.name());
        o.setBehaviourRevision(library.revision(primitive.key()));
        o.setPromptClasses(new ArrayList<>(spec.promptClasses()));
        o.setSummary(envelope.summary());
        o.setClaims(new ArrayList<>(envelope.claims().stream().map(OutputEnvelope.Claim::toMap).toList()));
        o.setEvidenceRefs(new ArrayList<>(envelope.evidenceRefs()));
        o.setKnownUnknowns(new ArrayList<>(envelope.knownUnknowns()));
        o.setConfidence(envelope.minConfidence());
        o.setSufficiency(envelope.overallSufficiency());
        o.setFailureState(envelope.failureState());
        o.setRoute(envelope.route());
        List<String> versions = new ArrayList<>(envelope.methodVersions());
        versions.add("behaviour:" + spec.name() + "@" + library.revision(primitive.key()));
        versions.add("thinking:" + spec.thinking().wireValue());
        o.setMethodVersions(versions);
        o.setConversationId(context.conversationId());
        o.setData(Map.of("grounding_problems", grounding.problems(),
                "proposed_actions", envelope.proposedActions().stream().map(a -> Map.of(
                        "action_type", a.actionType(), "target", a.target(), "authority", a.authority(),
                        "approval_required", a.approvalRequired())).toList()));
        return outputs.save(o).getId();
    }

    static OutputRecord.Kind kindOf(OutputType type) {
        return switch (type) {
            case OUT_01 -> OutputRecord.Kind.EVIDENCE_SET;
            case OUT_02 -> OutputRecord.Kind.CLASSIFICATION;
            case OUT_03 -> OutputRecord.Kind.INTELLIGENCE;
            case OUT_04 -> OutputRecord.Kind.JUDGEMENT;
            case OUT_05 -> OutputRecord.Kind.SCORE;
            case OUT_06 -> OutputRecord.Kind.PERSONALISATION;
            case OUT_07 -> OutputRecord.Kind.COMPARISON;
            case OUT_08 -> OutputRecord.Kind.ANSWER;
            case OUT_09 -> OutputRecord.Kind.RECOMMENDATION;
            case OUT_10 -> OutputRecord.Kind.DRAFT;
            case OUT_11 -> OutputRecord.Kind.ARTEFACT;
            case OUT_12 -> OutputRecord.Kind.CHANGE_SIGNAL;
            case OUT_13 -> OutputRecord.Kind.PLAN;
            case OUT_14 -> OutputRecord.Kind.COMMUNICATION;
            case OUT_15 -> OutputRecord.Kind.ACTION_RESULT;
            case OUT_16 -> OutputRecord.Kind.EVALUATION;
            case OUT_17 -> OutputRecord.Kind.ROUTE;
            case OUT_18 -> OutputRecord.Kind.OUTCOME;
            case OUT_19 -> OutputRecord.Kind.CALCULATION;
            case OUT_20 -> OutputRecord.Kind.CANVAS;
            case OUT_21 -> OutputRecord.Kind.INTERACTION_STATE;
            case OUT_22 -> OutputRecord.Kind.NAVIGATION;
        };
    }

    /** Execution-level behaviours are high consequence; recommending, drafting and planning medium; the rest low. */
    static Consequence consequenceOf(BehaviourSpec spec) {
        Authority a = spec.authority();
        if (a.isExecution()) {
            return Consequence.HIGH;
        }
        if (a.covers(Authority.RECOMMEND)) {
            return Consequence.MEDIUM;
        }
        return Consequence.LOW;
    }

    private static List<String> append(List<String> list, String item) {
        List<String> next = new ArrayList<>(list);
        next.add(item);
        return next;
    }

    private static String truncate(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }
}
