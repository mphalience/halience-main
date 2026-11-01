package ai.halience.aql.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Decision: an explicit choice by an authorised actor at a point in time. It references a frozen snapshot of what was
 * known and judged, so later outcomes cannot rewrite the decision's basis (LP-10). Decision is not approval.
 */
@Getter
@Setter
@Entity
@Table(name = "aql_decision")
public class DecisionRecord extends AqlRecord {

    @Column(columnDefinition = "TEXT")
    private String choice;

    @Column(length = 128)
    private String decisionMaker;

    @Column(length = 512)
    private String authorityBasis;

    /** Frozen evidence/intelligence/judgement/recommendation state at decision time. */
    @Column(length = 64)
    private String snapshotRef;

    @Column(length = 64)
    private String recommendationRef;

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> alternatives = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String rationale;

    @Column(length = 32)
    private String status = "made";

    @Column(columnDefinition = "TEXT")
    private String conditions;

    @Override
    protected String idPrefix() {
        return "dec";
    }
}
