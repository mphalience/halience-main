package ai.halience.agent.loop;

import ai.halience.agent.loop.execution.TerminationReason;
import ai.halience.agent.loop.search.Embedder;
import ai.halience.agent.loop.search.SemanticSearch;
import ai.halience.agent.loop.testing.MockModel;
import ai.halience.agent.loop.testing.TestRuns;
import ai.halience.agent.loop.testing.TestRuns.TestRun;
import ai.halience.agent.loop.tool.ToolRegistry;
import ai.halience.agent.loop.tool.builtin.WorkspaceTools;
import ai.halience.agent.loop.usage.UsagePurpose;
import ai.halience.agent.loop.usage.UsageRecord;
import ai.halience.agent.loop.usage.UsageTotals;
import ai.halience.agent.loop.workspace.InMemoryWorkspace;
import ai.halience.llm.router.LlmRouter;
import ai.halience.llm.router.capability.EmbeddingModelEntry;
import ai.halience.llm.router.capability.ModelCapabilityTable;
import ai.halience.llm.router.capability.ModelEntry;
import ai.halience.llm.router.config.RouteEntry;
import ai.halience.llm.router.config.RouterConfig;
import ai.halience.llm.router.model.EmbeddingResponse;
import ai.halience.llm.router.model.Request;
import ai.halience.llm.router.model.Response;
import ai.halience.llm.router.model.ToolCall;
import ai.halience.llm.router.model.ToolDefinition;
import ai.halience.llm.router.model.Usage;
import ai.halience.llm.router.provider.Provider;
import ai.halience.llm.router.provider.ProviderAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/** Costs in micro-dollars: priced embeddings, and many sub-cent calls adding up. */
class UsageCostTest {

    private static final String CHAT = "test-priced-chat";
    private static final String EMBEDDING = "test-priced-embedding";
    private static final Scope ALICE = Scope.of("acme", "alice", "s1");

    @BeforeEach
    void registerPricedModels() {
        // $1 per million input tokens: a 3,000-token call costs $0.003, a third of a cent.
        ModelCapabilityTable.registerModel(ModelEntry.builder().provider(Provider.ANTHROPIC).model(CHAT)
                .contextWindowTokens(100_000).maxOutputTokens(4_000).supportsTools(true)
                .inputCostPerMillionTokens(1.0).outputCostPerMillionTokens(0).build());
        // $0.02 per million tokens, like text-embedding-3-small.
        ModelCapabilityTable.registerEmbeddingModel(EmbeddingModelEntry.builder().provider(Provider.OPENAI).model(EMBEDDING)
                .inputCostPerMillionTokens(0.02).dimensions(2).maxInputTokens(8_000).lastUpdated(Instant.now()).build());
    }

    @AfterEach
    void removePricedModels() {
        ModelCapabilityTable.removeModel(Provider.ANTHROPIC, CHAT);
        ModelCapabilityTable.removeEmbeddingModel(Provider.OPENAI, EMBEDDING);
    }

    /** An embeddings provider reporting {@code tokensPerCall} input tokens for each call. */
    private static ProviderAdapter embeddings(int tokensPerCall) {
        return new ProviderAdapter() {
            @Override
            public Provider id() {
                return Provider.OPENAI;
            }

            @Override
            public boolean isAvailable() {
                return true;
            }

            @Override
            public Response send(String model, Request adaptedRequest) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String defaultEmbeddingModel() {
                return EMBEDDING;
            }

            @Override
            public EmbeddingResponse embed(String model, List<String> texts, Integer dimensions) {
                return EmbeddingResponse.builder()
                        .vectors(texts.stream().map(text -> new float[]{text.length(), 1}).toList())
                        .usage(Usage.builder().inputTokens(tokensPerCall).build()).build();
            }
        };
    }

    @Test
    void routerEmbeddingsCarryLlmRoutersPriceInMicroDollars() {
        Embedder.Embeddings result = Embedder.router(new LlmRouter(List.of(embeddings(50_000)))).embed(List.of("a"));

        // 50,000 tokens at $0.02 per million = $0.001.
        assertThat(result.costUsdMicros()).isEqualTo(1_000);
        assertThat(result.model()).endsWith("/" + EMBEDDING);
    }

    @Test
    void embeddingCostsAreMeteredInMicroDollarsAndCountTowardTheRun() {
        List<UsageRecord> records = new CopyOnWriteArrayList<>();
        MockModel model = new MockModel()
                .callTool(WorkspaceTools.WRITE, Map.of("path", "notes.md", "content", "Quarterly numbers."))
                .reply("Saved.");
        AgentLoop loop = model.loopBuilder()
                .tools(new ToolRegistry().registerAll(WorkspaceTools.all()))
                .workspace(new InMemoryWorkspace())
                .usageMeter(records::add)
                .semanticSearch(SemanticSearch.builder()
                        .embedder(Embedder.router(new LlmRouter(List.of(embeddings(50_000))))).build())
                .build();

        TestRun run = TestRuns.run(loop, LoopRequest.builder().prompt("Save my notes").agentProfile(MockModel.STEP_BY_STEP).scope(ALICE));

        UsageRecord embedding = records.stream().filter(r -> r.purpose() == UsagePurpose.EMBEDDING).findFirst().orElseThrow();
        assertThat(embedding.costUsdMicros()).isEqualTo(1_000);
        assertThat(embedding.costUsdCents()).isZero(); // a tenth of a cent rounds to nothing
        assertThat(embedding.executionId()).isEqualTo(run.result().execution().id());
        assertThat(run.result().usage().costUsdMicros()).isGreaterThanOrEqualTo(1_000);
    }

    @Test
    void manySubCentCallsStillReachTheCostLimit() {
        AtomicInteger calls = new AtomicInteger();
        MockModel model = new MockModel().usage(3_000, 0).otherwise(request -> Response.builder().content("")
                .toolCalls(List.of(ToolCall.builder().id("n" + calls.incrementAndGet()).name("noop").arguments(Map.of()).build()))
                .build());
        AgentLoop loop = model.loopBuilder().tools(new ToolRegistry().register(ToolDefinition.builder().name("noop")
                .description("Does nothing").parameters(Map.of("type", "object")).build(), args -> "ok")).build();

        TestRun run = TestRuns.run(loop, LoopRequest.builder().prompt("Keep going").agentProfile(MockModel.STEP_BY_STEP)
                .routerConfig(RouterConfig.builder().route(List.of(RouteEntry.of(Provider.ANTHROPIC, CHAT))).build())
                .maxCostUsdCents(1));

        assertThat(run.error()).isNull();
        assertThat(run.result().execution().terminationReason()).isEqualTo(TerminationReason.COST_LIMIT_REACHED);
        // 0.3 cents a call: the 4th call takes the run past 1 cent, though each call alone rounds to 0 cents.
        // Then one metered wrap-up call.
        assertThat(model.requests()).hasSize(5);
        assertThat(model.requests().getLast().getTools()).isEmpty();
        UsageTotals usage = run.result().usage();
        assertThat(usage.costUsdMicros()).isEqualTo(15_000);
        assertThat(usage.costUsdCents()).isZero();
        assertThat(usage.costUsdCentsRounded()).isEqualTo(2);
    }

    @Test
    void aToolThatKeepsFailingStillStopsAtTheCostLimit() {
        AtomicInteger calls = new AtomicInteger();
        MockModel model = new MockModel().usage(3_000, 0).otherwise(request -> Response.builder().content("")
                .toolCalls(List.of(ToolCall.builder().id("f" + calls.incrementAndGet()).name("flaky").arguments(Map.of()).build()))
                .build());
        AgentLoop loop = model.loopBuilder().tools(new ToolRegistry().register(ToolDefinition.builder().name("flaky")
                .description("Always fails").parameters(Map.of("type", "object")).build(), args -> {
                    throw new IllegalStateException("upstream unavailable");
                })).build();

        TestRun run = TestRuns.run(loop, LoopRequest.builder().prompt("Keep going").agentProfile(MockModel.STEP_BY_STEP)
                .routerConfig(RouterConfig.builder().route(List.of(RouteEntry.of(Provider.ANTHROPIC, CHAT))).build())
                .maxCostUsdCents(1));

        assertThat(run.error()).isNull();
        assertThat(run.result().execution().terminationReason()).isEqualTo(TerminationReason.COST_LIMIT_REACHED);
        assertThat(model.requests()).hasSize(5);
        assertThat(MockModel.lastToolResult(model.requests().get(3)))
                .isEqualTo("Error: flaky failed: IllegalStateException: upstream unavailable");
    }

    @Test
    void nearTheCostLimitTheModelIsWarnedOnceThenWrapsUp() {
        AtomicInteger calls = new AtomicInteger();
        MockModel model = new MockModel().usage(3_000, 0).otherwise(request -> request.getTools().isEmpty()
                ? Response.builder().content("Best answer within budget.").build()
                : Response.builder().content("")
                        .toolCalls(List.of(ToolCall.builder().id("n" + calls.incrementAndGet()).name("noop").arguments(Map.of()).build()))
                        .build());
        List<UsageRecord> records = new CopyOnWriteArrayList<>();
        AgentLoop loop = model.loopBuilder().usageMeter(records::add).tools(new ToolRegistry().register(ToolDefinition.builder().name("noop")
                .description("Does nothing").parameters(Map.of("type", "object")).build(), args -> "ok")).build();

        TestRun run = TestRuns.run(loop, LoopRequest.builder().prompt("Keep going").agentProfile(MockModel.STEP_BY_STEP)
                .routerConfig(RouterConfig.builder().route(List.of(RouteEntry.of(Provider.ANTHROPIC, CHAT))).build())
                .maxCostUsdCents(1));

        // 0.3 cents a call: after 3 calls (0.9 cents) the 80% mark is passed, so the 4th call is warned;
        // it takes the run past 1 cent, and the 5th call is the tools-off wrap-up.
        assertThat(model.requests()).extracting(WindDownTest::limitWarnings).containsExactly(0L, 0L, 0L, 1L, 0L);
        assertThat(model.requests().get(3).getPrompt())
                .contains("about $0.00 of the $0.01 cost limit left");
        assertThat(model.requests().getLast().getTools()).isEmpty();
        assertThat(run.result().execution().terminationReason()).isEqualTo(TerminationReason.COST_LIMIT_REACHED);
        assertThat(run.result().finalResponse().getContent()).isEqualTo("Best answer within budget.");
        assertThat(records.stream().filter(r -> r.purpose() == UsagePurpose.WRAP_UP)).hasSize(1);
    }
}
