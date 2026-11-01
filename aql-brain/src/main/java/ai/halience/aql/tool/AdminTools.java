package ai.halience.aql.tool;

import static ai.halience.aql.tool.BrainTool.bool;
import static ai.halience.aql.tool.BrainTool.str;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ai.halience.aql.config.BrainProperties;
import ai.halience.aql.domain.Json;
import ai.halience.aql.governance.GovernanceException;
import ai.halience.aql.primitive.Primitive;
import ai.halience.aql.primitive.PrimitiveEditService;
import ai.halience.aql.primitive.PrimitiveLibrary;
import ai.halience.aql.primitive.PrimitiveType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Halie viewing and proposing changes to its own primitives (View → Propose). Both need the principal to be a Halience
 * admin. Proposing goes through the one door: it creates an action awaiting an admin's approval and changes nothing.
 */
@Configuration
public class AdminTools {

    @Bean
    BrainTool primitiveView(PrimitiveLibrary library, PrimitiveEditService edits, BrainProperties properties) {
        return BrainTool.of("primitive_view", Map.of(
                        "key", str("A primitive key, e.g. behaviours/halie/halie; omit to list"),
                        "type", str("When listing: behaviours, knowledge, journeys, routines or tools"),
                        "history", bool("With key: include revision history")),
                List.of(),
                (args, call) -> {
                    if (!call.principal().hasRole(properties.brain().adminRole())) {
                        return "Refused: viewing primitives is for Halience admins.";
                    }
                    String key = Args.str(args, "key");
                    if (key == null) {
                        String type = Args.str(args, "type");
                        var rows = library.current().all().stream()
                                .filter(p -> type == null || p.type().folder().equals(type))
                                .map(p -> Map.of("key", p.key(), "phase", p.phase(), "enabled", library.isEnabled(p),
                                        "description", p.description()))
                                .toList();
                        return Json.write(rows);
                    }
                    Primitive p = library.current().byKey(key).orElse(null);
                    if (p == null) {
                        return "No primitive " + key;
                    }
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("key", key);
                    out.put("revision", library.revision(key));
                    out.put("enabled", library.isEnabled(p));
                    out.put("markdown", p.markdown());
                    if (Args.bool(args, "history")) {
                        out.put("history", edits.history(key).stream().map(r -> Map.of("revision", r.getRevision(),
                                "by", String.valueOf(r.getCreatedBy()), "reason", String.valueOf(r.getReason()),
                                "action", String.valueOf(r.getActionRef()))).toList());
                    }
                    return Json.write(out);
                });
    }

    @Bean
    BrainTool primitivePropose(PrimitiveEditService edits, BrainProperties properties) {
        return BrainTool.of("primitive_propose", Map.of(
                        "key", str("The primitive key, e.g. behaviours/core/researcher (new or existing). The first "
                                + "folder is the type: " + String.join(", ", java.util.Arrays.stream(
                                PrimitiveType.values()).map(PrimitiveType::folder).toList())),
                        "markdown", str("The complete new file: front matter and body"),
                        "reason", str("Current → proposed, why, expected impact, affected primitives, risk")),
                List.of("key", "markdown", "reason"),
                (args, call) -> {
                    if (!call.principal().hasRole(properties.brain().adminRole())) {
                        return "Refused: only Halience admins can ask for primitive changes.";
                    }
                    try {
                        PrimitiveEditService.Proposal p = edits.propose(Args.required(args, "key"),
                                Args.required(args, "markdown"), Args.required(args, "reason"), call.agent());
                        call.saw(p.action().getId());
                        return "Proposed as action " + p.action().getId() + ", awaiting approval " + p.approval().getId()
                                + " by a Halience admin. Nothing has changed yet."
                                + (p.warnings().isEmpty() ? "" : " Warnings: " + p.warnings());
                    } catch (GovernanceException e) {
                        throw BrainTool.input(e.getMessage());
                    }
                });
    }
}
