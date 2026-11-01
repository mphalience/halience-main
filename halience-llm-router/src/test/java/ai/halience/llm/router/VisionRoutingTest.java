package ai.halience.llm.router;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import ai.halience.llm.router.capability.ModelCapabilityTable;
import ai.halience.llm.router.capability.ModelEntry;
import ai.halience.llm.router.config.FallbackPolicy;
import ai.halience.llm.router.config.Feature;
import ai.halience.llm.router.config.RouterConfig;
import ai.halience.llm.router.config.ThinkingLevel;
import ai.halience.llm.router.error.RouterExhaustedException;
import ai.halience.llm.router.model.Attachment;
import ai.halience.llm.router.model.AttemptOutcome;
import ai.halience.llm.router.model.AttemptRecord;
import ai.halience.llm.router.model.Request;
import ai.halience.llm.router.provider.Provider;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * The vision path: a deck page PDF or a PNG sent with {@code requiredFeatures(ATTACHMENTS)} must
 * only ever reach models whose capability-table row takes that media type — at every thinking
 * level, across every seeded provider, with and without cost-optimized routing, and through
 * {@code FallbackPolicy.FINISH}'s step-downs. Every adapter fails, so the router walks the whole
 * route and every candidate it would ever send to is exercised.
 */
class VisionRoutingTest {

    private static final List<Provider> SEEDED = List.of(
            Provider.ANTHROPIC, Provider.OPENAI, Provider.PERPLEXITY, Provider.NVIDIA, Provider.OPENROUTER);

    static Stream<Arguments> cases() {
        List<Arguments> cases = new ArrayList<>();
        for (Attachment attachment : List.of(TestMedia.deckPagePdf(), TestMedia.pngAttachment())) {
            for (ThinkingLevel level : ThinkingLevel.values()) {
                for (boolean costOptimized : new boolean[] {false, true}) {
                    for (FallbackPolicy fallback : FallbackPolicy.values()) {
                        cases.add(Arguments.of(attachment, level, costOptimized, fallback));
                    }
                }
            }
        }
        return cases.stream();
    }

    @ParameterizedTest(name = "{0} at {1}, costOptimized={2}, {3}")
    @MethodSource("cases")
    void onlyModelsThatTakeTheMediaTypeAreEverSentIt(
            Attachment attachment, ThinkingLevel level, boolean costOptimized, FallbackPolicy fallback) {
        List<RecordingAdapter> adapters = SEEDED.stream().map(RecordingAdapter::new).toList();
        LlmRouter router = new LlmRouter(List.copyOf(adapters));
        RouterConfig config = RouterConfig.builder() // default route: every seeded provider
                .thinkingLevel(level)
                .costOptimized(costOptimized)
                .fallback(fallback)
                .requiredFeatures(Set.of(Feature.ATTACHMENTS))
                .build();
        Request request = Request.builder().prompt("What does this slide say?").attachments(List.of(attachment)).config(config).build();

        Throwable failure = catchThrowable(() -> router.complete(request));

        assertThat(failure).isInstanceOf(RouterExhaustedException.class);
        int sent = 0;
        for (RecordingAdapter adapter : adapters) {
            for (RecordingAdapter.Call call : adapter.calls()) {
                ModelEntry model = ModelCapabilityTable.findModel(adapter.id(), call.model()).orElseThrow();
                assertThat(model.supportsMediaType(attachment.getMediaType()))
                        .as("%s/%s was sent %s", adapter.id(), call.model(), attachment.getMediaType())
                        .isTrue();
                assertThat(call.request().getAttachments()).containsExactly(attachment); // sent intact, never stripped
                sent++;
            }
        }
        assertThat(sent).as("some provider takes %s", attachment.getMediaType()).isPositive();

        List<AttemptRecord> attempts = ((RouterExhaustedException) failure).attempts();
        assertThat(attempts).filteredOn(attempt -> attempt.getProvider() == Provider.NVIDIA).singleElement()
                .satisfies(nvidia -> {
                    assertThat(nvidia.getOutcome()).isEqualTo(AttemptOutcome.SKIPPED);
                    assertThat(nvidia.getReason()).isEqualTo("no nvidia model supports required feature(s) [attachments]");
                });
        if (fallback == FallbackPolicy.SAME_DEPTH) {
            assertThat(attempts).allSatisfy(attempt -> assertThat(attempt.getThinkingLevel()).isEqualTo(level));
        } else {
            assertThat(attempts).allSatisfy(attempt -> assertThat(attempt.getThinkingLevel()).isLessThanOrEqualTo(level));
        }
    }
}
