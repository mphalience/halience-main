package ai.halience.aql.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** When each routine last ran and how it went. */
@Getter
@Setter
@Entity
@Table(name = "aql_routine_state")
public class RoutineState extends AqlRecord {

    @Column(length = 64, nullable = false, unique = true)
    private String routine;

    private Instant lastRunAt;

    @Column(length = 32)
    private String lastStatus;

    @Column(length = 64)
    private String lastOutputRef;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @Override
    protected String idPrefix() {
        return "rts";
    }
}
