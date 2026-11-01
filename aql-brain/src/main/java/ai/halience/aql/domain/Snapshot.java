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

/**
 * A frozen copy of prior state (AR-02 "Freeze"): what AQL knew, predicted or recommended at a time, preserved so a later
 * outcome or correction cannot rewrite it. Erasure obligations still apply via the correction state.
 */
@Getter
@Setter
@Entity
@Table(name = "aql_snapshot")
public class Snapshot extends AqlRecord {

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> subjectRefs = new ArrayList<>();

    @Convert(converter = Json.MapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> content = new LinkedHashMap<>();

    @Column(length = 64)
    private String contentHash;

    @Column(length = 512)
    private String reason;

    @Override
    protected String idPrefix() {
        return "snp";
    }
}
