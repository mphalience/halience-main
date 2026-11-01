package ai.halience.aql.api;

import java.util.List;
import java.util.Map;

import ai.halience.aql.domain.ApprovalRecord;
import ai.halience.aql.domain.OutcomeRecord;
import ai.halience.aql.domain.PrimitiveRecord;
import ai.halience.aql.domain.SignalEvent;
import ai.halience.aql.domain.Snapshot;
import ai.halience.aql.governance.Actor;
import ai.halience.aql.governance.ApprovalService;
import ai.halience.aql.journey.JourneyService;
import ai.halience.aql.outcome.OutcomeService;
import ai.halience.aql.primitive.PrimitiveEditService;
import ai.halience.aql.primitive.PrimitiveLibrary;
import ai.halience.aql.signal.SignalService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The control surface: primitives through the one door (propose, approve, apply, roll back), approvals, signals,
 * outcomes and journeys. Every endpoint resolves the actor and leaves the decision to the governed services.
 */
@RestController
public class GovernanceController {

    private final PrimitiveLibrary library;
    private final PrimitiveEditService edits;
    private final ApprovalService approvals;
    private final SignalService signals;
    private final OutcomeService outcomes;
    private final JourneyService journeys;
    private final ActorResolver actors;

    public GovernanceController(PrimitiveLibrary library, PrimitiveEditService edits, ApprovalService approvals,
            SignalService signals, OutcomeService outcomes, JourneyService journeys, ActorResolver actors) {
        this.library = library;
        this.edits = edits;
        this.approvals = approvals;
        this.signals = signals;
        this.outcomes = outcomes;
        this.journeys = journeys;
        this.actors = actors;
    }

    // --- primitives -------------------------------------------------------------------------------------------

    @GetMapping("/api/primitives")
    List<Map<String, Object>> primitives(@RequestParam(required = false) String type) {
        return library.current().all().stream().filter(p -> type == null || p.type().folder().equals(type))
                .map(p -> Map.<String, Object>of("key", p.key(), "type", p.type().folder(), "phase", p.phase(),
                        "enabled", library.isEnabled(p), "revision", library.revision(p.key()),
                        "description", p.description()))
                .toList();
    }

    @GetMapping("/api/primitives/view")
    Map<String, Object> primitive(@RequestParam String key) {
        var p = library.current().byKey(key).orElseThrow(() -> new IllegalArgumentException("No primitive " + key));
        return Map.of("key", key, "revision", library.revision(key), "markdown", p.markdown(),
                "history", edits.history(key).stream().map(r -> Map.of("revision", r.getRevision(),
                        "by", String.valueOf(r.getCreatedBy()), "reason", String.valueOf(r.getReason()))).toList());
    }

    public record ProposalRequest(String key, String markdown, String reason) {
    }

    @PostMapping("/api/primitives/proposals")
    Map<String, Object> propose(@RequestBody ProposalRequest body, HttpServletRequest request) {
        var p = edits.propose(body.key(), body.markdown(), body.reason(), actors.resolve(request));
        return Map.of("actionId", p.action().getId(), "approvalId", p.approval().getId(),
                "warnings", p.warnings().stream().map(Object::toString).toList());
    }

    @PostMapping("/api/primitives/actions/{actionId}/apply")
    Map<String, Object> apply(@PathVariable String actionId, HttpServletRequest request) {
        PrimitiveRecord r = edits.apply(actionId, actors.resolve(request));
        return Map.of("key", r.getKey(), "revision", r.getRevision());
    }

    public record RollbackRequest(String key, int revision, String reason) {
    }

    @PostMapping("/api/primitives/rollback")
    Map<String, Object> rollback(@RequestBody RollbackRequest body, HttpServletRequest request) {
        PrimitiveRecord r = edits.rollback(body.key(), body.revision(), actors.resolve(request), body.reason());
        return Map.of("key", r.getKey(), "revision", r.getRevision());
    }

    // --- approvals --------------------------------------------------------------------------------------------

    public record Decision(boolean approve, String reason) {
    }

    @PostMapping("/api/approvals/{approvalId}/decision")
    Map<String, Object> decide(@PathVariable String approvalId, @RequestBody Decision body,
            HttpServletRequest request) {
        ApprovalRecord a = approvals.decide(approvalId, actors.resolve(request), body.approve(), body.reason());
        return Map.of("approvalId", a.getId(), "state", a.getState().name(), "subject", a.getSubjectRef());
    }

    // --- signals and outcomes ---------------------------------------------------------------------------------

    public record SignalRequest(SignalEvent event, double relativeChange, boolean causedByUserEdit,
                                boolean entityWatched) {
    }

    @PostMapping("/api/signals")
    Map<String, Object> signal(@RequestBody SignalRequest body, HttpServletRequest request) {
        var c = signals.capture(body.event(), body.relativeChange(), body.causedByUserEdit(), body.entityWatched(),
                actors.resolve(request));
        return Map.of("eventId", c.event().getId(),
                "materiality", c.verdict() == null ? "n/a" : c.verdict().materiality().name(),
                "notifyUser", c.verdict() != null && c.verdict().notifyUser(),
                "reason", c.verdict() == null ? "recorded" : c.verdict().reason(),
                "materialChange", c.materialChange() == null ? "" : c.materialChange().getId());
    }

    public record SnapshotRequest(List<String> refs, String reason) {
    }

    @PostMapping("/api/snapshots")
    Map<String, Object> freeze(@RequestBody SnapshotRequest body, HttpServletRequest request) {
        Snapshot s = outcomes.freeze(body.refs(), body.reason(), actors.resolve(request));
        return Map.of("snapshotId", s.getId(), "hash", s.getContentHash());
    }

    @PostMapping("/api/outcomes")
    Map<String, Object> outcome(@RequestBody OutcomeRecord body, HttpServletRequest request) {
        OutcomeRecord o = outcomes.record(body, actors.resolve(request));
        return Map.of("outcomeId", o.getId());
    }

    // --- journeys ---------------------------------------------------------------------------------------------

    public record StartJourney(String subjectRef) {
    }

    public record CompleteMilestone(String evidenceRef, String approvalId) {
    }

    @PostMapping("/api/journeys/{journey}/runs")
    Map<String, Object> startJourney(@PathVariable String journey, @RequestBody StartJourney body,
            HttpServletRequest request) {
        return describe(journeys.start(journey, body.subjectRef(), actors.resolve(request)));
    }

    @GetMapping("/api/journeys/runs/{runId}")
    Map<String, Object> journeyStatus(@PathVariable String runId) {
        return describe(journeys.status(runId));
    }

    @PostMapping("/api/journeys/runs/{runId}/milestones/{milestone}")
    Map<String, Object> complete(@PathVariable String runId, @PathVariable String milestone,
            @RequestBody CompleteMilestone body, HttpServletRequest request) {
        Actor actor = actors.resolve(request);
        return describe(journeys.complete(runId, milestone, body.evidenceRef(), body.approvalId(), actor));
    }

    private static Map<String, Object> describe(JourneyService.Status s) {
        return Map.of("runId", s.run().getId(), "journey", s.journey().name(), "completed", s.run().getCompleted(),
                "available", s.available().stream().map(m -> Map.of("key", m.key(), "title", m.title(),
                        "behaviour", String.valueOf(m.behaviour()), "approval", m.approval().name())).toList(),
                "complete", s.complete(), "status", s.run().getStatus().name());
    }
}
