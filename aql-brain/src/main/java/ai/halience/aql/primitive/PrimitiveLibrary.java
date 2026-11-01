package ai.halience.aql.primitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import ai.halience.aql.config.BrainProperties;
import ai.halience.aql.domain.PrimitiveRecord;
import ai.halience.aql.domain.Repositories.PrimitiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * The live primitive set, loaded from the store. Every reload bumps {@link #generation()} so built agents can be
 * dropped and rebuilt. A set with validation errors is never made live: the previous set stays in force.
 * <p>
 * Phase gating: a primitive whose phase is above {@code halience.brain.max-phase} is loaded and validated but
 * {@link #isEnabled disabled}, so it can't run, be delegated to or be offered as a tool.
 */
@Slf4j
@Component
public class PrimitiveLibrary {

    private final PrimitiveRepository records;
    private final int maxPhase;
    private final AtomicInteger generation = new AtomicInteger();
    private volatile PrimitiveSet current = new PrimitiveSet(List.of());
    private volatile Map<String, Integer> revisions = Map.of();

    public PrimitiveLibrary(PrimitiveRepository records, BrainProperties properties) {
        this.records = records;
        this.maxPhase = properties.brain().maxPhase();
    }

    public PrimitiveSet current() {
        return current;
    }

    public int generation() {
        return generation.get();
    }

    public int maxPhase() {
        return maxPhase;
    }

    public boolean isEnabled(Primitive primitive) {
        return primitive.phase() <= maxPhase;
    }

    /** The stored revision of a primitive, for output versioning (GV-11). */
    public int revision(String key) {
        return revisions.getOrDefault(key, 0);
    }

    /** Reloads from the store. Returns the issues found; the set is only installed if there are no errors. */
    public synchronized List<ValidationIssue> reload() {
        List<Primitive> primitives = new ArrayList<>();
        Map<String, Integer> revs = new HashMap<>();
        List<ValidationIssue> issues = new ArrayList<>();
        for (PrimitiveRecord record : records.findAll()) {
            try {
                primitives.add(Primitive.parse(record.getKey() + ".md", record.getMarkdown()));
                revs.put(record.getKey(), record.getRevision());
            } catch (IllegalArgumentException e) {
                issues.add(ValidationIssue.error(record.getKey(), e.getMessage()));
            }
        }
        PrimitiveSet set = new PrimitiveSet(primitives);
        issues.addAll(PrimitiveValidator.validate(set));
        if (issues.stream().anyMatch(ValidationIssue::isError)) {
            issues.stream().filter(ValidationIssue::isError).forEach(i -> log.error("Primitive {}", i));
            log.error("Primitive set has errors; keeping the previous set ({} primitives)", current.size());
            return issues;
        }
        issues.forEach(i -> log.warn("Primitive {}", i));
        install(set, revs);
        return issues;
    }

    /** Installs an already-validated set (also used by tests). */
    public synchronized void install(PrimitiveSet set, Map<String, Integer> revs) {
        this.current = set;
        this.revisions = Map.copyOf(revs);
        generation.incrementAndGet();
        log.info("Primitive set installed: {} primitives, generation {}, max phase {}", set.size(), generation.get(),
                maxPhase);
    }
}
