package ai.halience.aql.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import ai.halience.aql.config.BrainProperties;
import ai.halience.llm.router.config.RouteEntry;
import ai.halience.llm.router.config.RouterConfig;
import ai.halience.llm.router.config.ThinkingLevel;
import ai.halience.llm.router.provider.Provider;
import org.junit.jupiter.api.Test;

class ThinkingRoutesTest {

    @Test
    void eachLevelMapsToItsConfiguredModelsInOrder() {
        ThinkingRoutes routes = new ThinkingRoutes(BrainProperties.Llm.defaults(), p -> true);
        assertThat(routes.models(Thinking.LOW).getFirst()).isEqualTo(RouteEntry.of(Provider.ANTHROPIC, "claude-haiku-5-5"));
        assertThat(routes.models(Thinking.HIGH).getFirst()).isEqualTo(RouteEntry.of(Provider.ANTHROPIC, "claude-opus-5-5"));
        RouterConfig config = routes.routerConfig(Thinking.MEDIUM);
        assertThat(config.getThinkingLevel()).isEqualTo(ThinkingLevel.MEDIUM);
        assertThat(config.getRoute()).extracting(RouteEntry::getModel)
                .containsExactly("claude-sonnet-5-5", "gpt-6.1-sol", "google/gemini-4-argon");
    }

    @Test
    void providersWithoutCredentialsAreDropped() {
        ThinkingRoutes routes = new ThinkingRoutes(BrainProperties.Llm.defaults(), p -> p == Provider.OPENAI);
        assertThat(routes.models(Thinking.LOW)).containsExactly(RouteEntry.of(Provider.OPENAI, "gpt-6-luna"));
    }

    @Test
    void withNoAvailableModelsTheRouterChoosesAtTheSameDepth() {
        ThinkingRoutes routes = new ThinkingRoutes(BrainProperties.Llm.defaults(), p -> false);
        RouterConfig config = routes.routerConfig(Thinking.HIGH);
        assertThat(config.getRoute()).isNull();
        assertThat(config.getThinkingLevel()).isEqualTo(ThinkingLevel.HIGH);
    }

    @Test
    void configuredRoutesOverrideTheDefaultsPerLevel() {
        BrainProperties.Llm llm = new BrainProperties.Llm(Map.of(Thinking.LOW, List.of("openai")));
        ThinkingRoutes routes = new ThinkingRoutes(llm, p -> true);
        assertThat(routes.models(Thinking.LOW)).containsExactly(RouteEntry.of(Provider.OPENAI));
        assertThat(routes.models(Thinking.HIGH)).isNotEmpty();
    }

    @Test
    void parsesProviderAndModelIncludingSlashedModelIds() {
        assertThat(ThinkingRoutes.parse("openrouter/google/gemini-3.8-flash"))
                .isEqualTo(RouteEntry.of(Provider.OPENROUTER, "google/gemini-3.8-flash"));
        assertThatThrownBy(() -> ThinkingRoutes.parse("bedrock/claude")).hasMessageContaining("Unknown provider");
    }

    @Test
    void thinkingParsesFromFrontMatter() {
        assertThat(Thinking.fromWire("High")).isEqualTo(Thinking.HIGH);
        assertThat(Thinking.fromWire(null)).isEqualTo(Thinking.MEDIUM);
        assertThatThrownBy(() -> Thinking.fromWire("max")).hasMessageContaining("low, medium or high");
    }
}
