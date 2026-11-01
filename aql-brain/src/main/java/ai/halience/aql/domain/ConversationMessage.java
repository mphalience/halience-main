package ai.halience.aql.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** One message in a conversation. A message stays communication; significant content becomes a linked object. */
@Getter
@Setter
@Entity
@Table(name = "aql_message", indexes = @Index(columnList = "conversationId"))
public class ConversationMessage extends AqlRecord {

    public enum Role { USER, HALIE, PERSON }

    @Column(length = 64, nullable = false)
    private String conversationId;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private Role role;

    @Column(length = 128)
    private String authorId;

    @Column(columnDefinition = "TEXT")
    private String content;

    /** The output record behind a Halie reply, for provenance. */
    @Column(length = 64)
    private String outputRef;

    @Override
    protected String idPrefix() {
        return "msg";
    }
}
