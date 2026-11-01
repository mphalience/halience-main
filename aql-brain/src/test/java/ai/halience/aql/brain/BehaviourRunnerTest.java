package ai.halience.aql.brain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.halience.agent.loop.testing.MockModel;
import ai.halience.aql.Fakes;
import ai.halience.aql.config.BrainProperties;
import ai.halience.aql.context.ContextEnvelope;
import ai.halience.aql.domain.AgentRunRecord;
import ai.halience.aql.domain.OutputRecord;
import ai.halience.aql.domain.Repositories.AgentRunRepository;
import ai.halience.aql.domain.Repositories.AuditRepository;
import ai.halience.aql.domain.Repositories.OutputRepository;
import ai.halience.aql.domain.Repositories.PrimitiveRepository;
import ai.halience.aql.governance.AuditLog;
import ai.halience.aql.governance.AuthorityPolicy;
import ai.halience.aql.llm.ThinkingRoutes;
import ai.halience.aql.ops.HealthRegistry;
import ai.halience.aql.primitive.PrimitiveLibrary;
import ai.halience.aql.primitive.PrimitiveSet;
import ai.halience.aql.primitive.PrimitiveType;
import ai.halience.aql.primitive.StarterPrimitives;
import ai.halience.aql.primitive.ToolManifest;
import ai.halience.aql.spec.Trigger;
import ai.halience.aql.spec.Vocabulary.FailureState;
import ai.halience.aql.spec.Vocabulary.Route;
import ai.halience.aql.spec.Vocabulary.Sufficiency;
import ai.halience.aql.tool.BrainTool;
import ai.halience.aql.tool.ToolCatalogue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Runs real shipped behaviours through the real agent loop against a scripted model: no network, no keys. Checks the
 * wiring from primitive to agent to tools to governed, persisted output.
 */
class BehaviourRunnerTest {

    static final PrimitiveSet SHIPPED = StarterPrimitives.parse(StarterPrimitives.load("classpath*:brain/**/*.md"));

    MockModel model = new MockModel();
    AgentRunRepository runRepo = mock(AgentRunRepository.class);
    OutputRepository outputRepo = mock(OutputRepository.class);
    AuditRepository audits = mock(AuditRepository.class);
    Map<String, AgentRunRecord> runStore;
    Map<String, OutputRecord> outputStore;
    List<String> toolCalls = new ArrayList<>();
    BehaviourRunner runner;

    @BeforeEach
    void setUp() {
        runStore = Fakes.backed(runRepo);
        outputStore = Fakes.backed(outputRepo);
        Fakes.backed(audits);
        BrainProperties properties = BrainProperties.defaults();
        PrimitiveLibrary library = new PrimitiveLibrary(mock(PrimitiveRepository.class), properties);
        library.install(SHIPPED, Map.of());
        RunRegistry runs = new RunRegistry();
        List<BrainTool> tools = new ArrayList<>();
        for (var p : SHIPPED.ofType(PrimitiveType.TOOL)) {
            ToolManifest m = ToolManifest.of(p);
            if (m.kind() != ToolManifest.Kind.BRAIN) {
                continue;
            }
            tools.add(BrainTool.of(m.name(), Map.of("query", Map.of("type", "string")), List.of(), (args, call) -> {
                toolCalls.add(call.behaviour() + ":" + m.name());
                if (m.name().equals("knowledge_search")) {
                    call.saw("kno_watch");
                    return "[{\"knowledge_id\":\"kno_watch\",\"title\":\"Settle Watch requirement\","
                            + "\"status\":\"canonical\"}]";
                }
                return "stub";
            }));
        }
        AuditLog audit = new AuditLog(audits);
        ToolCatalogue catalogue = new ToolCatalogue(tools, library, runs, new AuthorityPolicy(properties), audit);
        ThinkingRoutes routes = new ThinkingRoutes(properties.llm(), provider -> false);
        BrainAgents agents = new BrainAgents(library, catalogue, routes, model.loopBuilder().build());
        runner = new BehaviourRunner(agents, library, runs, runRepo, outputRepo, new HealthRegistry(Fakes.CLOCK),
                properties, Fakes.CLOCK);
    }

    static ContextEnvelope context() {
        return ContextEnvelope.of(Fakes.person("ana"), Trigger.TR_01, Fakes.NOW);
    }

    static String envelope(String summary, String ref) {
        return "{\"output_type\":\"OUT-08\",\"summary\":\"" + summary + "\",\"claims\":[{\"text\":\"Watch alerts "
                + "only on external material change\",\"claim_class\":\"observed\",\"evidence_refs\":[\"" + ref
                + "\"],\"confidence\":0.9,\"sufficiency\":\"sufficient\"}],\"known_unknowns\":[],"
                + "\"failure_state\":\"none\",\"route\":\"none\"}";
    }

    @Test
    void aBehaviourRunsWithItsToolsAndProducesAGroundedPersistedOutput() {
        model.planCheck(false)
                .callTool("knowledge_search", Map.of("query", "watch"))
                .reply("Watch alerts only on external material change (Settle Watch requirement).")
                .reply(envelope("Watch alerts only on external material change.", "kno_watch"));

        BrainResult result = runner.run(BrainRequest.of("org_knowledge_retriever",
                "What does the Watch requirement say about alerts?", context()));

        assertThat(toolCalls).containsExactly("org_knowledge_retriever:knowledge_search");
        assertThat(result.groundingProblems()).isEmpty();
        assertThat(result.envelope().route()).isEqualTo(Route.NONE);
        assertThat(result.envelope().overallSufficiency()).isEqualTo(Sufficiency.SUFFICIENT);
        OutputRecord stored = outputStore.get(result.outputId());
        assertThat(stored.getBehaviour()).isEqualTo("org_knowledge_retriever");
        assertThat(stored.getEvidenceRefs()).containsExactly("kno_watch");
        assertThat(stored.getMethodVersions()).contains("thinking:medium");
        AgentRunRecord run = runStore.get(result.runId());
        assertThat(run.getState()).isEqualTo(AgentRunRecord.State.COMPLETED);
        assertThat(run.getPermittedTools()).containsExactly("knowledge_search", "artefact_open", "evidence_search");
        // The model saw the behaviour's instructions, its skills and the tools it is allowed.
        var firstWorkingCall = model.requests().get(1);
        assertThat(MockModel.toolsOffered(firstWorkingCall)).contains("knowledge_search", "artefact_open",
                "evidence_search", "load_skill").doesNotContain("execute_action", "primitive_propose");
    }

    @Test
    void inventedProvenanceIsCaughtAndRouted() {
        model.planCheck(false)
                .reply("It says so.")
                .reply(envelope("It says so.", "kno_invented"));

        BrainResult result = runner.run(new BrainRequest("org_knowledge_retriever", "What does it say?", context(),
                ai.halience.aql.spec.Vocabulary.Consequence.MEDIUM, null));

        assertThat(result.groundingProblems()).hasSize(1);
        assertThat(result.envelope().claims().getFirst().sufficiency()).isEqualTo(Sufficiency.INSUFFICIENT);
        assertThat(result.envelope().route()).isEqualTo(Route.ESCALATE);
        assertThat(runStore.get(result.runId()).getState()).isEqualTo(AgentRunRecord.State.ESCALATED);
    }

    @Test
    void aFailedRunIsReportedAsAFailureNeverAsSuccess() {
        model.otherwise(request -> {
            throw new IllegalStateException("provider down");
        });

        BrainResult result = runner.run(BrainRequest.of("org_knowledge_retriever", "Anything?", context()));

        assertThat(result.envelope().failureState()).isEqualTo(FailureState.FAILED);
        assertThat(result.outputId()).isNull();
        assertThat(runStore.get(result.runId()).getState()).isEqualTo(AgentRunRecord.State.FAILED);
    }

    @Test
    void laterPhaseBehavioursDontRun() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> runner.run(BrainRequest.of("valuation_analyst",
                "Value this", context()))).hasMessageContaining("not enabled");
    }
}
