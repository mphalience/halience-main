package ai.halience.aql.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import ai.halience.aql.spec.Vocabulary.KnowledgeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Knowledge: governed reusable information accepted for an authorised scope. Its {@link #status} keeps source, working,
 * proposed and canonical material apart (GV-19): retrievable is not the same as organisational truth.
 * <p>
 * This is the organisation's domain knowledge (requirements, decisions, guides...). The brain's own knowledge
 * primitives (methods and frameworks it reasons with) live with the other primitives.
 */
@Getter
@Setter
@Entity
@Table(name = "aql_knowledge")
public class KnowledgeItem extends AqlRecord {

    @Column(length = 256)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private KnowledgeStatus status = KnowledgeStatus.WORKING;

    @Column(length = 64)
    private String jurisdiction;

    @Column(length = 128)
    private String owner;

    /** The authoritative system or document this knowledge comes from, which remains authoritative. */
    @Column(length = 512)
    private String authoritativeSource;

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> sourceRefs = new ArrayList<>();

    @Convert(converter = Json.StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> entityRefs = new ArrayList<>();

    private Instant reviewBy;

    @Override
    protected String idPrefix() {
        return "kno";
    }
}
