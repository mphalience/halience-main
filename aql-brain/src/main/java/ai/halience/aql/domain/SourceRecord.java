package ai.halience.aql.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Source: originating material or system record from which governed Evidence can be derived. Accessible source is not
 * Evidence and not organisational truth; the external system may remain authoritative ({@link #authoritative}).
 */
@Getter
@Setter
@Entity
@Table(name = "aql_source")
public class SourceRecord extends AqlRecord {

    @Column(length = 64)
    private String sourceType;

    @Column(length = 128)
    private String provider;

    /** The system of record, when the source lives outside AQL. */
    @Column(length = 128)
    private String system;

    @Column(length = 1024)
    private String uri;

    /** Whether this source is authoritative for its data type in its scope (source hierarchy is per domain). */
    private boolean authoritative;

    @Column(length = 64)
    private String sourceVersion;

    private Instant lastRefreshed;

    @Column(length = 256)
    private String licence;

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> entityRefs = new ArrayList<>();

    @Override
    protected String idPrefix() {
        return "src";
    }
}
