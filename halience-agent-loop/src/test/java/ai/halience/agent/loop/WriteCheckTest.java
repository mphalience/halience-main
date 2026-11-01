package ai.halience.agent.loop;

import ai.halience.agent.loop.memory.MemoryStore;
import ai.halience.agent.loop.testing.MockModel;
import ai.halience.agent.loop.testing.TestRuns;
import ai.halience.agent.loop.tool.RegisteredTool;
import ai.halience.agent.loop.tool.ToolContext;
import ai.halience.agent.loop.tool.ToolRegistry;
import ai.halience.agent.loop.tool.UntrustedContentGuard;
import ai.halience.agent.loop.workspace.InMemoryWorkspace;
import ai.halience.agent.loop.workspace.WorkspaceLimits;
import ai.halience.llm.router.model.ToolCall;
import ai.halience.llm.router.model.ToolDefinition;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ai.halience.agent.loop.testing.MockModel.lastToolResult;
import static org.assertj.core.api.Assertions.assertThat;

class WriteCheckTest {

    @Test
    void theGuardHoldsBackAnyCallItsToolSaysWritesOnceOutsideContentWasRead() {
        MockModel model = new MockModel()
                .callTool("read_page", Map.of())
                .callTool("ledger", Map.of("action", "view"))
                .callTool("ledger", Map.of("action", "post"))
                .callTool("broken", Map.of())
                .reply("done");
        ToolRegistry tools = new ToolRegistry()
                .register(new RegisteredTool(definition("read_page"), (args, context) -> "page text").withUntrustedOutput())
                .register(new RegisteredTool(definition("ledger"), (args, context) -> "ran " + args.get("action"))
                        .withWriteCheck((args, context) -> "post".equals(args.get("action"))))
                .register(new RegisteredTool(definition("broken"), (args, context) -> "ran")
                        .withWriteCheck((args, context) -> {
                            throw new IllegalStateException("store down");
                        }));

        TestRuns.run(model.loopBuilder().tools(tools).toolInterceptor(UntrustedContentGuard.block()).build(),
                LoopRequest.builder().prompt("go").agentProfile(MockModel.STEP_BY_STEP));

        assertThat(lastToolResult(model.requests().get(2))).contains("ran view");
        assertThat(lastToolResult(model.requests().get(3))).contains("This action wasn't approved");
        assertThat(lastToolResult(model.requests().get(4))).contains("This action wasn't approved");  // a failing check counts as a write
    }

    @Test
    void withersKeepTheWriteCheckAndAContextWithoutARunSaysNothingWrites() {
        RegisteredTool tool = new RegisteredTool(definition("t"), (args, context) -> "")
                .withWriteCheck((args, context) -> true)
                .withTimeout(Duration.ofSeconds(5))
                .withUntrustedOutput()
                .withRequiresWorkspace();
        Scope scope = Scope.forUser("u", "s");
        ToolContext detached = new ToolContext(UUID.randomUUID(), 0, scope, MemoryStore.NONE.scopedTo(scope),
                new InMemoryWorkspace().scopedTo(scope, WorkspaceLimits.DEFAULT));

        assertThat(tool.writes(Map.of(), detached)).isTrue();
        assertThat(new RegisteredTool(definition("t"), (args, context) -> "").writes(Map.of(), detached)).isFalse();
        assertThat(detached.writes(ToolCall.builder().id("1").name("t").arguments(Map.of()).build())).isFalse();
    }

    private static ToolDefinition definition(String name) {
        return ToolDefinition.builder().name(name).description(name)
                .parameters(Map.of("type", "object", "properties", Map.of(), "required", List.of())).build();
    }
}
