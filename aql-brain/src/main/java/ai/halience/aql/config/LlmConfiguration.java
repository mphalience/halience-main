package ai.halience.aql.config;

import java.time.Clock;

import ai.halience.agent.loop.AgentLoop;
import ai.halience.agent.loop.conversation.InMemoryConversationStore;
import ai.halience.agent.loop.workspace.InMemoryWorkspace;
import ai.halience.aql.llm.ThinkingRoutes;
import ai.halience.llm.router.LlmRouter;
import ai.halience.llm.router.provider.CredentialResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the LLM layer: the router (credentials from {@code HALIENCE_<PROVIDER>_API_KEY}), the thinking-level routes,
 * and the base agent loop every behaviour runs on. The application starts without any keys; a behaviour run then fails
 * honestly with "no providers configured" rather than the app refusing to boot.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(BrainProperties.class)
public class LlmConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    CredentialResolver credentialResolver() {
        return new CredentialResolver();
    }

    @Bean
    LlmRouter llmRouter(CredentialResolver credentials) {
        if (credentials.availableProviders().isEmpty()) {
            log.warn("No LLM provider keys found (HALIENCE_ANTHROPIC_API_KEY, HALIENCE_OPENAI_API_KEY, ...); "
                    + "behaviour runs will fail until one is set");
        } else {
            log.info("LLM providers available: {}", credentials.availableProviders());
        }
        return new LlmRouter();
    }

    @Bean
    ThinkingRoutes thinkingRoutes(BrainProperties properties, CredentialResolver credentials) {
        return new ThinkingRoutes(properties.llm(), credentials::isAvailable);
    }

    /**
     * The base loop. Conversation history for Halie is kept by AQL's own conversation store and passed per run, so the
     * loop's store is only a per-process cache for other sessions. The workspace is in memory for the first cut (used
     * by the Phase 2 coding tools).
     */
    @Bean
    AgentLoop agentLoop(LlmRouter router) {
        return AgentLoop.builder()
                .router(router)
                .conversations(new InMemoryConversationStore())
                .workspace(new InMemoryWorkspace())
                .build();
    }
}
