package ai.halience.aql.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Every saved version of a primitive, so any change can be inspected and rolled back. */
@Getter
@Setter
@Entity
@Table(name = "aql_primitive_revision", indexes = @Index(columnList = "primitiveKey"))
public class PrimitiveRevision extends AqlRecord {

    @Column(length = 256, nullable = false)
    private String primitiveKey;

    private int revision;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String markdown;

    /** The action that produced this revision (absent for a starter seed). */
    @Column(length = 64)
    private String actionRef;

    @Column(length = 128)
    private String createdBy;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Override
    protected String idPrefix() {
        return "prv";
    }
}
