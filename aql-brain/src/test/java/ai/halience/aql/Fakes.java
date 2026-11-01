package ai.halience.aql;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ai.halience.aql.domain.AqlRecord;
import ai.halience.aql.governance.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

/** Test helpers: repositories backed by a map, fixed clocks and standard actors. */
public final class Fakes {

    public static final Instant NOW = Instant.parse("2026-11-01T09:00:00Z");
    public static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private Fakes() {
    }

    /** Makes a Mockito repository mock store and return records in memory, assigning ids on save. */
    public static <T extends AqlRecord, R extends JpaRepository<T, String>> Map<String, T> backed(R repository) {
        Map<String, T> store = new ConcurrentHashMap<>();
        lenient().when(repository.save(any())).thenAnswer(invocation -> {
            T record = invocation.getArgument(0);
            record.ensureId();
            if (record.getCapturedTime() == null) {
                record.setCapturedTime(NOW);
            }
            store.put(record.getId(), record);
            return record;
        });
        lenient().when(repository.findById(any())).thenAnswer(invocation ->
                Optional.ofNullable(store.get((String) invocation.getArgument(0))));
        lenient().when(repository.findAll()).thenAnswer(invocation -> java.util.List.copyOf(store.values()));
        return store;
    }

    public static Actor admin(String id) {
        return Actor.person(id, Set.of("admin"), Set.of(Actor.ALL_SCOPES));
    }

    public static Actor person(String id) {
        return Actor.person(id, Set.of(), Set.of(Actor.ALL_SCOPES));
    }
}
