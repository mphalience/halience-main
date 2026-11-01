package ai.halience.aql.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import ai.halience.aql.spec.Authority;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Agent Run: one bounded execution of a behaviour, with its permitted tools, authority and budget fixed at start. An
 * agent cannot widen any of them during the run. Every behaviour run is recorded, so runs can be reviewed and audited.
 */
@Getter
@Setter
@Entity
@Table(name = "aql_agent_run")
public class AgentRunRecord extends AqlRecord {

    public enum State { RUNNING, COMPLETED, WRAPPED_UP, FAILED, CANCELLED, ESCALATED }

    @Column(length = 64, nullable = false)
    private String behaviour;

    private Integer behaviourRevision;

    @Column(length = 128)
    private String initiatedBy;

    @Column(length = 16)
    private String trigger;

    @Column(columnDefinition = "TEXT")
    private String objective;

    @Column(length = 64)
    private String workContextRef;

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> permittedTools = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 24)
    private Authority authority;

    @Column(length = 16)
    private String thinking;

    private Integer budgetCents;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private State state = State.RUNNING;

    @Column(length = 64)
    private String outputRef;

    @Column(columnDefinition = "TEXT")
    private String failure;

    private Instant finishedAt;

    private Double costCents;

    @Override
    protected String idPrefix() {
        return "run";
    }
}
