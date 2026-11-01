package ai.halience.aql.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Who/what did what, when, on what basis, with what result (GV-10). Denied attempts are audited too (GV-18). */
@Getter
@Setter
@Entity
@Table(name = "aql_audit")
public class AuditEvent extends AqlRecord {

    @Column(length = 128)
    private String actorId;

    @Column(length = 16)
    private String actorType;

    @Column(length = 64, nullable = false)
    private String action;

    @Column(length = 512)
    private String subjectRef;

    /** allowed, denied, succeeded, failed... */
    @Column(length = 32)
    private String outcome;

    @Convert(converter = Json.MapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> detail = new LinkedHashMap<>();

    @Override
    protected String idPrefix() {
        return "aud";
    }
}
