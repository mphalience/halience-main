package ai.halience.aql.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * The live copy of one primitive. The Markdown shipped with the application (the starter) only seeds an empty store or
 * updates a primitive nobody has edited; once edited through the one door, the stored copy owns the text.
 */
@Getter
@Setter
@Entity
@Table(name = "aql_primitive")
public class PrimitiveRecord extends AqlRecord {

    /** e.g. {@code behaviours/halie/halie}. */
    @Column(name = "primitive_key", length = 256, nullable = false, unique = true)
    private String key;

    @Column(length = 16, nullable = false)
    private String type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String markdown;

    private int revision = 1;

    /** Hash of the starter text this record was last seeded from. */
    @Column(length = 64)
    private String starterHash;

    /** Whether it has been changed through the one door (and so no longer follows its starter). */
    private boolean edited;

    @Column(length = 128)
    private String updatedBy;

    @Override
    protected String idPrefix() {
        return "prm";
    }
}
