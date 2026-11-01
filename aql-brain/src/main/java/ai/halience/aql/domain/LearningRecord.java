package ai.halience.aql.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Learning: a validated lesson or calibration signal derived from outcomes for a permitted scope. It never changes
 * production behaviour by itself (GV-16) and stays local unless explicitly sanitised and approved for wider reuse.
 */
@Getter
@Setter
@Entity
@Table(name = "aql_learning")
public class LearningRecord extends AqlRecord {

    public enum Reuse { LOCAL, PRODUCT, PORTFOLIO }

    public enum Status { PROPOSED, VALIDATED, REJECTED, SUPERSEDED }

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> sourceRefs = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String lesson;

    private Double confidence;

    @Column(length = 64)
    private String methodVersion;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Reuse permittedReuse = Reuse.LOCAL;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Status status = Status.PROPOSED;

    @Column(columnDefinition = "TEXT")
    private String privacyConstraints;

    @Override
    protected String idPrefix() {
        return "lrn";
    }
}
