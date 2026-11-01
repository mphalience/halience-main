package ai.halience.aql.signal;

import java.util.List;
import java.util.Map;

import ai.halience.aql.domain.OutputRecord;
import ai.halience.aql.domain.Repositories.OutputRepository;
import ai.halience.aql.domain.Repositories.SignalEventRepository;
import ai.halience.aql.domain.SignalEvent;
import ai.halience.aql.governance.Actor;
import ai.halience.aql.governance.AuditLog;
import ai.halience.aql.governance.GovernanceException;
import ai.halience.aql.ops.HealthRegistry;
import ai.halience.aql.signal.MaterialityAssessor.Change;
import ai.halience.aql.signal.MaterialityAssessor.Verdict;
import ai.halience.aql.spec.OutputType;
import ai.halience.aql.spec.SignalClass;
import ai.halience.aql.spec.SpecCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Captures signals in the Signal Contract shape and runs the materiality gate (LP-03: observe → recompute → assess →
 * route/hold). Only a material change relevant to a user becomes a user-facing material change signal (OUT-12);
 * operational signals go to AQL Operations; everything else is recorded and held. Nothing goes straight from a raw
 * event to a notification.
 */
@Service
public class SignalService {

    private final SignalEventRepository events;
    private final OutputRepository outputs;
    private final HealthRegistry health;
    private final AuditLog audit;

    public SignalService(SignalEventRepository events, OutputRepository outputs, HealthRegistry health,
            AuditLog audit) {
        this.events = events;
        this.outputs = outputs;
        this.health = health;
        this.audit = audit;
    }

    public record Captured(SignalEvent event, Verdict verdict, OutputRecord materialChange) {
    }

    @Transactional
    public Captured capture(SignalEvent event, double relativeChange, boolean causedByUserEdit, boolean entityWatched,
            Actor actor) {
        if (event.getTenantId() == null) {
            event.setTenantId(actor.tenantId());
        }
        // The materiality gate runs first: materiality and user relevance are AQL-owned fields it supplies.
        SignalClass signal = event.getSignalClass() == null ? null
                : SpecCode.parse(SignalClass.class, event.getSignalClass()).orElse(null);
        Verdict verdict = signal == null ? null
                : MaterialityAssessor.assess(new Change(signal, relativeChange, causedByUserEdit, entityWatched));
        if (verdict != null) {
            if (event.getMateriality() == null) {
                event.setMateriality(verdict.materiality());
            }
            event.setUserRelevance(verdict.userRelevance());
        }
        List<String> violations = SignalContractValidator.violations(event);
        if (!violations.isEmpty()) {
            throw new GovernanceException(GovernanceException.Reason.INVALID,
                    "Signal doesn't meet the Signal Contract: " + violations);
        }
        SignalEvent saved = events.save(event);
        OutputRecord change = null;
        if (verdict != null && verdict.routeToOps()) {
            health.record(saved);
        }
        if (verdict != null && verdict.notifyUser()) {
            change = new OutputRecord();
            change.setTenantId(saved.getTenantId());
            change.setScopeRef(saved.getScopeRef());
            change.setOutputType(OutputType.OUT_12.code());
            change.setKind(OutputRecord.Kind.CHANGE_SIGNAL);
            change.setSummary(saved.getEventType() + ": " + verdict.reason());
            change.setEntityRefs(saved.getEntityRefs());
            change.setEvidenceRefs(List.of(saved.getId()));
            change.setConfidence(saved.getConfidence());
            change.setData(Map.of("old", String.valueOf(saved.getOldValueRef()),
                    "new", String.valueOf(saved.getNewValueRef()), "cause", String.valueOf(saved.getCauseRef()),
                    "materiality", verdict.materiality().name()));
            change = outputs.save(change);
        }
        audit.record(actor, "signal_captured", saved.getId(),
                verdict == null ? "recorded" : verdict.materiality().name().toLowerCase(),
                Map.of("signal", String.valueOf(saved.getSignalClass()),
                        "reason", verdict == null ? "no signal class" : verdict.reason()));
        return new Captured(saved, verdict, change);
    }
}
