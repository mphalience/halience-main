package ai.halience.aql.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * A persistent conversation (OUT-21): its channel, active Halience context and recent referents, so a follow-up does
 * not need the user to restate the work. Halie conversations and People/Message threads are separate channels and
 * are never merged (CX-21).
 */
@Getter
@Setter
@Entity
@Table(name = "aql_conversation")
public class Conversation extends AqlRecord {

    public enum Channel { HALIE, PEOPLE }

    @Column(length = 128, nullable = false)
    private String ownerId;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private Channel channel = Channel.HALIE;

    /** For a People/Message thread, the thread's id in the messaging system. */
    @Column(length = 128)
    private String threadRef;

    @Column(length = 256)
    private String title;

    /** The active venture/object/surface/scoped Context (CX-20). */
    @Convert(converter = Json.MapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> activeContext = new LinkedHashMap<>();

    /** Recent referents, newest first: each {ref, label, type, at}. */
    @Convert(converter = Json.ObjectListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Map<String, Object>> referents = new ArrayList<>();

    private Instant lastActivity;

    @Override
    protected String idPrefix() {
        return "cnv";
    }
}
