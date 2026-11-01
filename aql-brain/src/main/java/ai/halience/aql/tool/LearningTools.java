package ai.halience.aql.tool;

import static ai.halience.aql.tool.BrainTool.bool;
import static ai.halience.aql.tool.BrainTool.num;
import static ai.halience.aql.tool.BrainTool.obj;
import static ai.halience.aql.tool.BrainTool.str;
import static ai.halience.aql.tool.BrainTool.strs;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.halience.aql.domain.Json;
import ai.halience.aql.domain.OutcomeRecord;
import ai.halience.aql.domain.OutputRecord;
import ai.halience.aql.domain.Repositories.OutputRepository;
import ai.halience.aql.domain.SignalEvent;
import ai.halience.aql.governance.GovernanceException;
import ai.halience.aql.ops.HealthRegistry;
import ai.halience.aql.outcome.OutcomeService;
import ai.halience.aql.signal.SignalService;
import ai.halience.aql.spec.ObservedInferred;
import ai.halience.aql.spec.OutcomeFamily;
import ai.halience.aql.spec.OutputType;
import ai.halience.aql.spec.Vocabulary.FailureState;
import ai.halience.aql.spec.Vocabulary.Initiator;
import ai.halience.aql.spec.Vocabulary.OutcomeSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Monitoring, evaluation and outcome tools (PC-13, PC-16, PC-18) and health telemetry for AQL Operations. */
@Configuration
public class LearningTools {

    @Bean
    BrainTool emitSignal(SignalService signals) {
        return BrainTool.of("emit_signal", Map.ofEntries(
                        Map.entry("signal_class", str("SG code, e.g. SG-006")),
                        Map.entry("event_family", str("e.g. watch, delivery, gtm, control")),
                        Map.entry("event_type", str("Versioned subtype, e.g. material_risk_change.v1")),
                        Map.entry("entity_refs", strs("The entities affected")),
                        Map.entry("observed_inferred", str("observed or inferred")),
                        Map.entry("confidence", num("0-1; required for inferred")),
                        Map.entry("old_value_ref", str("Prior value ref, for a change")),
                        Map.entry("new_value_ref", str("New value ref, for a change")),
                        Map.entry("cause_ref", str("The causal event or source")),
                        Map.entry("relative_change", num("Size of change as a fraction; 1 for categorical")),
                        Map.entry("caused_by_user_edit", bool("True if the user's own edit caused it")),
                        Map.entry("entity_watched", bool("True if the user watches or owns the entity")),
                        Map.entry("model_rule_version", str("Method/rule version that derived it")),
                        Map.entry("event_time", str("ISO time it happened"))),
                List.of("signal_class", "event_family", "event_type", "entity_refs", "observed_inferred"),
                (args, call) -> {
                    SignalEvent e = new SignalEvent();
                    e.setSignalClass(Args.required(args, "signal_class"));
                    e.setEventFamily(Args.required(args, "event_family"));
                    e.setEventType(Args.required(args, "event_type"));
                    e.setEntityRefs(new ArrayList<>(Args.list(args, "entity_refs")));
                    e.setInitiator(Initiator.AQL);
                    e.setObservedInferred(Args.enumOf(ObservedInferred.class, args, "observed_inferred", null));
                    e.setConfidence(Args.number(args, "confidence"));
                    e.setOldValueRef(Args.str(args, "old_value_ref"));
                    e.setNewValueRef(Args.str(args, "new_value_ref"));
                    e.setCauseRef(Args.str(args, "cause_ref"));
                    e.setModelRuleVersion(Args.str(args, "model_rule_version") == null ? "behaviour:" + call.behaviour()
                            : Args.str(args, "model_rule_version"));
                    String time = Args.str(args, "event_time");
                    try {
                        e.setEventTime(time == null ? Instant.now() : Instant.parse(time));
                    } catch (RuntimeException ex) {
                        throw BrainTool.input("event_time must be an ISO-8601 instant");
                    }
                    try {
                        Double change = Args.number(args, "relative_change");
                        SignalService.Captured c = signals.capture(e, change == null ? 0 : change,
                                Args.bool(args, "caused_by_user_edit"), Args.bool(args, "entity_watched"),
                                call.agent());
                        call.saw(c.event().getId());
                        return "Captured " + c.event().getId() + (c.verdict() == null ? "" : ": "
                                + c.verdict().materiality() + ", " + c.verdict().reason())
                                + (c.materialChange() == null ? "" : ". Material change signal "
                                + c.materialChange().getId());
                    } catch (GovernanceException ex) {
                        throw BrainTool.input(ex.getMessage());
                    }
                });
    }

    @Bean
    BrainTool recordEvaluation(OutputRepository outputs, HealthRegistry health) {
        return BrainTool.of("record_evaluation", Map.of(
                        "subject_ref", str("The output, capability or model evaluated"),
                        "criteria_version", str("The evaluation criteria and their version"),
                        "result", str("pass, degrade or block"),
                        "score", num("Optional score 0-1"),
                        "defects", strs("What failed and why"),
                        "evaluator", str("Who or what evaluated (model, rule, person)"),
                        "independent", bool("Whether the evaluator was independent of the generator")),
                List.of("subject_ref", "criteria_version", "result", "evaluator", "independent"),
                (args, call) -> {
                    String result = Args.required(args, "result").toLowerCase();
                    if (!List.of("pass", "degrade", "block").contains(result)) {
                        throw BrainTool.input("result must be pass, degrade or block");
                    }
                    OutputRecord o = ActionTools.output(call, OutputType.OUT_16, OutputRecord.Kind.EVALUATION,
                            result + ": " + String.join("; ", Args.list(args, "defects")),
                            List.of(Args.required(args, "subject_ref")));
                    o.setConfidence(Args.number(args, "score"));
                    o.setMethodVersions(new ArrayList<>(List.of(Args.required(args, "criteria_version"))));
                    o.setFailureState(switch (result) {
                        case "block" -> FailureState.BLOCKED;
                        case "degrade" -> FailureState.DEGRADED;
                        default -> FailureState.NONE;
                    });
                    o.setData(Map.of("result", result, "evaluator", Args.required(args, "evaluator"),
                            "independent", Args.bool(args, "independent"), "defects", Args.list(args, "defects")));
                    OutputRecord saved = outputs.save(o);
                    if (!result.equals("pass")) {
                        health.report(Args.required(args, "subject_ref"), o.getFailureState(), "evaluation " + result);
                    }
                    return "Recorded evaluation " + saved.getId() + (Args.bool(args, "independent") ? ""
                            : " (marked not independent)");
                });
    }

    @Bean
    BrainTool recordOutcome(OutcomeService outcomes) {
        return BrainTool.of("record_outcome", Map.of(
                        "family", str("One of the 19 outcome families, e.g. offer, valuation, recommendation, gtm"),
                        "event_type", str("e.g. offer_accepted"),
                        "entity_refs", strs("Entities involved"),
                        "observed_inferred", str("observed or inferred"),
                        "outcome_source", str("user_confirmed, public_record, system_event, professional_document, "
                                + "aql_output or inference"),
                        "prior_ref", str("The prior output/recommendation/action it evaluates"),
                        "snapshot_ref", str("The frozen snapshot of that prior state"),
                        "result", obj("What happened"),
                        "event_time", str("ISO time it happened"),
                        "attribution_limits", str("What this outcome can't tell us")),
                List.of("family", "event_type", "observed_inferred", "outcome_source", "result"),
                (args, call) -> {
                    OutcomeRecord o = new OutcomeRecord();
                    o.setFamily(Args.enumOf(OutcomeFamily.class, args, "family", null));
                    o.setEventType(Args.required(args, "event_type"));
                    o.setEntityRefs(new ArrayList<>(Args.list(args, "entity_refs")));
                    o.setObservedInferred(Args.enumOf(ObservedInferred.class, args, "observed_inferred", null));
                    o.setOutcomeSource(Args.enumOf(OutcomeSource.class, args, "outcome_source", null));
                    o.setPriorRef(Args.str(args, "prior_ref"));
                    o.setSnapshotRef(Args.str(args, "snapshot_ref"));
                    o.setResult(Args.map(args, "result"));
                    o.setAttributionLimits(Args.str(args, "attribution_limits"));
                    String time = Args.str(args, "event_time");
                    o.setEventTime(time == null ? Instant.now() : Instant.parse(time));
                    try {
                        OutcomeRecord saved = outcomes.record(o, call.agent());
                        return "Recorded outcome " + saved.getId();
                    } catch (GovernanceException e) {
                        throw BrainTool.input(e.getMessage());
                    }
                });
    }

    @Bean
    BrainTool healthRead(HealthRegistry health) {
        return BrainTool.of("health_read", Map.of("capability", str("A capability, source, connector or model; omit "
                        + "for all")), List.of(),
                (args, call) -> {
                    String capability = Args.str(args, "capability");
                    var rows = health.all().stream()
                            .filter(h -> capability == null || h.capability().equals(capability))
                            .map(h -> Map.of("capability", h.capability(), "state", h.state().name(),
                                    "detail", String.valueOf(h.detail()), "since", String.valueOf(h.since()),
                                    "last_seen", String.valueOf(h.lastSeen())))
                            .toList();
                    return rows.isEmpty() ? (capability == null ? "No health telemetry recorded yet."
                            : "No telemetry for " + capability + ": its health is unknown, not healthy.")
                            : Json.write(rows);
                });
    }
}
