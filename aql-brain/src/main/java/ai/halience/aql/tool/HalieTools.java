package ai.halience.aql.tool;

import static ai.halience.aql.tool.BrainTool.obj;
import static ai.halience.aql.tool.BrainTool.str;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ai.halience.aql.domain.Conversation;
import ai.halience.aql.domain.Json;
import ai.halience.aql.governance.GovernanceException;
import ai.halience.aql.governance.PermissionService;
import ai.halience.aql.halie.CanvasComposer;
import ai.halience.aql.halie.ConversationService;
import ai.halience.aql.halie.DeliverySource;
import ai.halience.aql.halie.NavigationService;
import ai.halience.aql.journey.JourneyService;
import ai.halience.aql.spec.Vocabulary.Classification;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Interaction-state, canvas, navigation, delivery and journey tools for Halie (LP-02, PA-039 to PA-043, PA-021/022). */
@Configuration
public class HalieTools {

    @Bean
    BrainTool conversationState(ConversationService conversations) {
        return BrainTool.of("conversation_state", Map.of(
                        "operation", str("get (default), refer (add a referent) or set_context"),
                        "ref", str("For refer: the typed ref of the object discussed"),
                        "label", str("For refer: a short label"),
                        "type", str("For refer: requirement, artefact, venture, decision..."),
                        "active_context", obj("For set_context: the new active context, or {} for All Halience")),
                List.of(),
                (args, call) -> {
                    String id = call.conversationId();
                    if (id == null) {
                        return "This run has no conversation, so there is no interaction state.";
                    }
                    Conversation c;
                    try {
                        c = conversations.requireOwned(call.agent(), id);
                    } catch (GovernanceException e) {
                        return "Refused: " + e.getMessage();
                    }
                    String op = Args.str(args, "operation") == null ? "get" : Args.str(args, "operation");
                    switch (op) {
                        case "refer" -> c = conversations.refer(c, Args.required(args, "ref"),
                                Args.str(args, "label"), Args.str(args, "type"));
                        case "set_context" -> c = conversations.setActiveContext(c, Args.map(args, "active_context"));
                        case "get" -> {
                        }
                        default -> throw BrainTool.input("operation must be get, refer or set_context");
                    }
                    c.getReferents().forEach(r -> call.saw(String.valueOf(r.get("ref"))));
                    return Json.write(Map.of("conversation", c.getId(), "channel", c.getChannel().name(),
                            "active_context", c.getActiveContext(), "referents", c.getReferents()));
                });
    }

    @Bean
    BrainTool composeCanvas(PermissionService permissions) {
        return BrainTool.of("compose_canvas", Map.of(
                        "active_scope", str("The scoped Context, e.g. venture:settle; omit for All Halience"),
                        "items", Map.of("type", "array", "description",
                                "Candidates: {ref, label, region (attention|work|pulse|team), scope_ref, "
                                        + "classification (public|internal|restricted|private), status, reason}",
                                "items", Map.of("type", "object"))),
                List.of("items"),
                (args, call) -> {
                    List<CanvasComposer.Item> items = new ArrayList<>();
                    for (Map<String, Object> i : Args.maps(args, "items")) {
                        items.add(new CanvasComposer.Item(String.valueOf(i.get("ref")), String.valueOf(i.get("label")),
                                Args.enumOf(CanvasComposer.Region.class, i, "region", CanvasComposer.Region.WORK),
                                Args.str(i, "scope_ref"),
                                Args.enumOf(Classification.class, i, "classification", Classification.INTERNAL),
                                Args.str(i, "status"), Args.str(i, "reason")));
                    }
                    CanvasComposer.Canvas canvas = CanvasComposer.compose(call.agent(),
                            call.context().activeContext(), Args.str(args, "active_scope"), items, permissions, 8);
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("output_type", "OUT-20");
                    out.put("active_context", canvas.activeContext());
                    canvas.regions().forEach((region, placed) -> out.put(region.name().toLowerCase(),
                            placed.stream().map(p -> Map.of("ref", p.ref(), "label", p.label(),
                                    "status", String.valueOf(p.status()), "reason", String.valueOf(p.reason())))
                                    .toList()));
                    return Json.write(out);
                });
    }

    @Bean
    BrainTool navigate(NavigationService navigation) {
        return BrainTool.of("navigate", Map.of(
                        "ref", str("The target object"),
                        "action", str("open, review, ask_halie, approve or navigate"),
                        "classification", str("public, internal, restricted or private"),
                        "scope_ref", str("The scope the target belongs to"),
                        "status", str("canonical or working"),
                        "required_role", str("For approve: the role the approval needs")),
                List.of("ref", "action"),
                (args, call) -> {
                    NavigationService.Target t = navigation.route(call.agent(), Args.required(args, "ref"),
                            Args.enumOf(NavigationService.OsAction.class, args, "action", null),
                            Args.enumOf(Classification.class, args, "classification", Classification.INTERNAL),
                            Args.str(args, "scope_ref"), Args.str(args, "status"), Args.str(args, "required_role"));
                    if (t.permitted()) {
                        call.saw(t.ref());
                    }
                    return Json.write(Map.of("output_type", "OUT-22", "ref", t.ref(), "action", t.action().name(),
                            "permitted", t.permitted(), "destination", String.valueOf(t.destination()),
                            "reason", t.reason()));
                });
    }

    @Bean
    BrainTool deliveryRead(List<DeliverySource> sources) {
        return BrainTool.of("delivery_read", Map.of(
                        "kind", str("requirements, issues or repository"),
                        "query", str("What to look for")),
                List.of("kind", "query"),
                (args, call) -> {
                    String kind = Args.required(args, "kind");
                    DeliverySource source = sources.stream().filter(s -> s.kind().equals(kind)).findFirst()
                            .orElse(null);
                    if (source == null) {
                        return "Unavailable: no " + kind + " connector is configured. Report the source as "
                                + "unavailable, not as empty.";
                    }
                    try {
                        List<Map<String, Object>> items = source.search(Args.required(args, "query"),
                                call.principal());
                        items.forEach(i -> call.saw(String.valueOf(i.get("ref"))));
                        return items.isEmpty() ? "No matching " + kind + " items." : Json.write(items);
                    } catch (Exception e) {
                        return "Failed: the " + kind + " source couldn't be read (" + e.getMessage() + ").";
                    }
                });
    }

    @Bean
    BrainTool journeyStatus(JourneyService journeys) {
        return BrainTool.of("journey_status", Map.of(
                        "run_id", str("A journey run id, for progress on one subject"),
                        "journey", str("A journey name, to see its milestones")),
                List.of(),
                (args, call) -> {
                    try {
                        String runId = Args.str(args, "run_id");
                        if (runId != null) {
                            JourneyService.Status s = journeys.status(runId);
                            return Json.write(Map.of("journey", s.journey().name(), "subject",
                                    String.valueOf(s.run().getSubjectRef()), "completed", s.run().getCompleted(),
                                    "available", s.available().stream().map(m -> m.key() + ": " + m.title()).toList(),
                                    "complete", s.complete()));
                        }
                        var j = journeys.definition(Args.required(args, "journey"));
                        return Json.write(Map.of("journey", j.name(), "goal", j.goal(), "milestones",
                                j.milestones().stream().map(m -> Map.of("key", m.key(), "title", m.title(),
                                        "after", m.after(), "approval", m.approval().name(),
                                        "phase", m.phase())).toList()));
                    } catch (GovernanceException e) {
                        return "Refused: " + e.getMessage();
                    }
                });
    }
}
