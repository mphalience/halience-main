package ai.halience.aql.governance;

import java.util.LinkedHashMap;
import java.util.Map;

import ai.halience.aql.domain.AuditEvent;
import ai.halience.aql.domain.Repositories.AuditRepository;
import org.springframework.stereotype.Component;

/** Writes audit events: who/what did what to which subject, with what outcome (GV-10, GV-18). */
@Component
public class AuditLog {

    private final AuditRepository audits;

    public AuditLog(AuditRepository audits) {
        this.audits = audits;
    }

    public AuditEvent record(Actor actor, String action, String subjectRef, String outcome, Map<String, Object> detail) {
        AuditEvent event = new AuditEvent();
        event.setTenantId(actor.tenantId());
        event.setActorId(actor.id());
        event.setActorType(actor.type().name());
        event.setAction(action);
        event.setSubjectRef(subjectRef);
        event.setOutcome(outcome);
        event.setDetail(detail == null ? new LinkedHashMap<>() : new LinkedHashMap<>(detail));
        return audits.save(event);
    }
}
