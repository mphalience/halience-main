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
 * Work Context: the durable governed boundary binding objective, actors, work, artefacts, evidence, decisions,
 * permissions and authority. Membership never makes restricted linked objects visible by association (GV-18).
 * Phase 1 establishes the contract; Phase 2 adds depth.
 */
@Getter
@Setter
@Entity
@Table(name = "aql_work_context")
public class WorkContextRecord extends AqlRecord {

    @Column(columnDefinition = "TEXT")
    private String objective;

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> entityRefs = new ArrayList<>();

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> actorRefs = new ArrayList<>();

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> linkedRefs = new ArrayList<>();

    /** Actor id to granted permission/authority classes within this context. */
    @Convert(converter = Json.MapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> grants = new LinkedHashMap<>();

    @Column(length = 32)
    private String state = "active";

    @Override
    protected String idPrefix() {
        return "wct";
    }
}
