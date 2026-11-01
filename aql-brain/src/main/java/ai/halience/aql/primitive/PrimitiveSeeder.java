package ai.halience.aql.primitive;

import java.util.List;

import ai.halience.aql.config.BrainProperties;
import ai.halience.aql.domain.PrimitiveRecord;
import ai.halience.aql.domain.PrimitiveRevision;
import ai.halience.aql.domain.Repositories.PrimitiveRepository;
import ai.halience.aql.domain.Repositories.PrimitiveRevisionRepository;
import ai.halience.aql.governance.PayloadHasher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the store from the starter primitives at startup, then loads the library:
 * <ul>
 *   <li>a starter with no stored copy is added;</li>
 *   <li>a stored copy nobody has edited follows its starter when the starter changes in a release;</li>
 *   <li>a stored copy edited through the one door keeps its text: the edit owns it.</li>
 * </ul>
 * The starters must validate cleanly; a release with broken primitives fails to start rather than running a brain
 * that half works.
 */
@Slf4j
@Component
@Order(0)
public class PrimitiveSeeder implements ApplicationRunner {

    private final BrainProperties properties;
    private final PrimitiveRepository records;
    private final PrimitiveRevisionRepository revisions;
    private final PrimitiveLibrary library;

    public PrimitiveSeeder(BrainProperties properties, PrimitiveRepository records,
            PrimitiveRevisionRepository revisions, PrimitiveLibrary library) {
        this.properties = properties;
        this.records = records;
        this.revisions = revisions;
        this.library = library;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (properties.brain().seedOnStartup()) {
            seed(StarterPrimitives.load(properties.brain().primitives()));
        }
        List<ValidationIssue> issues = library.reload();
        if (issues.stream().anyMatch(ValidationIssue::isError)) {
            throw new IllegalStateException("The primitive set has errors; see the log");
        }
    }

    void seed(List<StarterPrimitives.Source> sources) {
        PrimitiveSet starters = StarterPrimitives.parse(sources);
        List<ValidationIssue> errors = PrimitiveValidator.errors(starters);
        if (!errors.isEmpty()) {
            errors.forEach(e -> log.error("Starter {}", e));
            throw new IllegalStateException(errors.size() + " starter primitive error(s); first: " + errors.getFirst());
        }
        int added = 0;
        int refreshed = 0;
        for (Primitive starter : starters.all()) {
            String hash = PayloadHasher.sha256(starter.markdown());
            PrimitiveRecord record = records.findByKey(starter.key()).orElse(null);
            if (record == null) {
                record = new PrimitiveRecord();
                record.setKey(starter.key());
                record.setType(starter.type().name());
                record.setMarkdown(starter.markdown());
                record.setStarterHash(hash);
                record.setUpdatedBy("starter");
                records.save(record);
                revision(record, "Seeded from starter");
                added++;
            } else if (!record.isEdited() && !hash.equals(record.getStarterHash())) {
                record.setMarkdown(starter.markdown());
                record.setStarterHash(hash);
                record.setRevision(record.getRevision() + 1);
                record.setUpdatedBy("starter");
                records.save(record);
                revision(record, "Refreshed from a newer starter");
                refreshed++;
            }
        }
        log.info("Primitives seeded: {} added, {} refreshed from starters, {} starters in all", added, refreshed,
                starters.size());
    }

    private void revision(PrimitiveRecord record, String reason) {
        PrimitiveRevision revision = new PrimitiveRevision();
        revision.setPrimitiveKey(record.getKey());
        revision.setRevision(record.getRevision());
        revision.setMarkdown(record.getMarkdown());
        revision.setCreatedBy("starter");
        revision.setReason(reason);
        revisions.save(revision);
    }
}
