package ai.halience.llm.router;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import ai.halience.llm.router.config.ThinkingLevel;
import ai.halience.llm.router.model.AttemptOutcome;
import ai.halience.llm.router.model.AttemptRecord;
import ai.halience.llm.router.model.AttemptUsage;
import ai.halience.llm.router.model.CallUsage;
import ai.halience.llm.router.model.Usage;
import ai.halience.llm.router.provider.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One call's attempts: the {@link AttemptRecord}s that go on its response or exception, and the
 * events its {@link UsageListener} hears (§5.6). A call's attempts run one at a time, so this
 * isn't thread-safe; an async call hands it from one attempt's thread to the next through its
 * futures.
 */
final class CallTracker {

    private static final Logger log = LoggerFactory.getLogger(CallTracker.class);

    private final UsageListener listener;
    private final boolean embedding;
    private final long callStart = System.nanoTime();
    private final List<AttemptRecord> attempts = new ArrayList<>();
    private final List<AttemptUsage> events = new ArrayList<>();
    private long attemptStart = callStart;

    CallTracker(UsageListener listener, boolean embedding) {
        this.listener = listener;
        this.embedding = embedding;
    }

    /** The candidates skipped or failed so far, for {@code Response.attempts} and {@code RouterExhaustedException}. */
    List<AttemptRecord> attempts() {
        return attempts;
    }

    /** Marks the start of the next attempt's latency. */
    void attemptStarted() {
        attemptStart = System.nanoTime();
    }

    void skipped(AttemptRecord record) {
        attempts.add(record);
        attempt(record, Duration.ZERO);
    }

    void failed(AttemptRecord record) {
        attempts.add(record);
        attempt(record, sinceAttemptStart());
    }

    /** Reports the answering attempt and then the call. */
    void succeeded(Provider provider, String model, ThinkingLevel level, int retries, Usage usage) {
        AttemptUsage attempt = AttemptUsage.builder()
                .provider(provider)
                .model(model)
                .outcome(AttemptOutcome.SUCCESS)
                .thinkingLevel(level)
                .retries(retries)
                .latency(sinceAttemptStart())
                .usage(usage)
                .embedding(embedding)
                .build();
        events.add(attempt);
        notifyAttempt(attempt);
        notifyCall(CallUsage.builder()
                .succeeded(true)
                .providerUsed(provider)
                .modelUsed(model)
                .latency(Duration.ofNanos(System.nanoTime() - callStart))
                .usage(usage)
                .attempts(List.copyOf(events))
                .embedding(embedding)
                .build());
    }

    void callFailed(Throwable failure) {
        notifyCall(CallUsage.builder()
                .succeeded(false)
                .latency(Duration.ofNanos(System.nanoTime() - callStart))
                .attempts(List.copyOf(events))
                .failure(failure)
                .embedding(embedding)
                .build());
    }

    private void attempt(AttemptRecord record, Duration latency) {
        AttemptUsage attempt = AttemptUsage.builder()
                .provider(record.getProvider())
                .model(record.getModel())
                .outcome(record.getOutcome())
                .reason(record.getReason())
                .thinkingLevel(record.getThinkingLevel())
                .retries(record.getRetries())
                .latency(latency)
                .embedding(embedding)
                .build();
        events.add(attempt);
        notifyAttempt(attempt);
    }

    private Duration sinceAttemptStart() {
        return Duration.ofNanos(System.nanoTime() - attemptStart);
    }

    private void notifyAttempt(AttemptUsage attempt) {
        try {
            listener.onAttempt(attempt);
        } catch (RuntimeException e) {
            log.warn("UsageListener.onAttempt threw; ignoring it", e);
        }
    }

    private void notifyCall(CallUsage call) {
        try {
            listener.onCall(call);
        } catch (RuntimeException e) {
            log.warn("UsageListener.onCall threw; ignoring it", e);
        }
    }
}
