package ai.halience.aql.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** One journey being followed for one subject (a venture, a company, a Situation, a Settle Process...). */
@Getter
@Setter
@Entity
@Table(name = "aql_journey_run")
public class JourneyRun extends AqlRecord {

    public enum Status { ACTIVE, COMPLETED, STOPPED }

    @Column(length = 64, nullable = false)
    private String journey;

    @Column(length = 128)
    private String subjectRef;

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> completed = new ArrayList<>();

    /** Milestone key to the output/approval/outcome ref that completed it. */
    @Convert(converter = Json.MapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> evidence = new LinkedHashMap<>();

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(length = 16, nullable = false)
    private Status status = Status.ACTIVE;

    @Column(length = 128)
    private String startedBy;

    @Override
    protected String idPrefix() {
        return "jrn";
    }
}
