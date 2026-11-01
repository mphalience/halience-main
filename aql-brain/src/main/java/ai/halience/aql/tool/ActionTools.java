package ai.halience.aql.tool;

import static ai.halience.aql.tool.BrainTool.obj;
import static ai.halience.aql.tool.BrainTool.str;
import static ai.halience.aql.tool.BrainTool.strs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ai.halience.aql.action.ActionService;
import ai.halience.aql.domain.ActionRecord;
import ai.halience.aql.domain.ApprovalRecord;
import ai.halience.aql.domain.Json;
import ai.halience.aql.domain.OutputRecord;
import ai.halience.aql.domain.Repositories.ApprovalRepository;
import ai.halience.aql.domain.Repositories.OutputRepository;
import ai.halience.aql.governance.ApprovalService;
import ai.halience.aql.governance.AuditLog;
import ai.halience.aql.governance.GovernanceException;
import ai.halience.aql.spec.OutputType;
import ai.halience.aql.spec.Vocabulary.ApprovalMode;
import ai.halience.aql.spec.Vocabulary.Consequence;
import ai.halience.aql.spec.Vocabulary.Route;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Drafting, proposing, approving, executing and escalating (PC-11, PC-12, PC-15, PC-17). Recommendation, preparation,
 * approval and execution stay separate: the execute tools take an action id and run it only if that exact action is
 * authorised. A refused or failed execution is reported as such.
 */
@Configuration
public class ActionTools {

    @Bean
    BrainTool proposeAction(ActionService actions) {
        return BrainTool.of("propose_action", Map.of(
                        "action_type", str("send_message, publish_content, update_record, api_call, transact..."),
                        "channel", str("Where it would be carried out: email, publish, api, crm..."),
                        "target", str("Recipient, channel, resource or record it applies to"),
                        "payload", obj("Exactly what would be sent or changed"),
                        "consequence", str("none, low, medium, high or critical"),
                        "basis", str("Why: the recommendation, decision or request it carries out")),
                List.of("action_type", "channel", "target", "payload", "basis"),
                (args, call) -> {
                    Consequence consequence = Args.enumOf(Consequence.class, args, "consequence", Consequence.MEDIUM);
                    ActionRecord action = actions.propose(Args.required(args, "action_type"),
                            Args.required(args, "channel"), Args.required(args, "target"), Args.map(args, "payload"),
                            consequence, ApprovalMode.POLICY, Args.required(args, "basis"), call.agent());
                    call.saw(action.getId());
                    return "Proposed action " + action.getId() + ". "
                            + (actions.needsApproval(action) ? "It needs approval before it can run: use "
                            + "request_approval." : "Its consequence is below the approval threshold.");
                });
    }

    @Bean
    BrainTool requestApproval(ActionService actions, ApprovalService approvals) {
        return BrainTool.of("request_approval", Map.of(
                        "action_id", str("The proposed action to approve"),
                        "required_role", str("A role the approver must hold, if the policy names one"),
                        "reason", str("What the approver needs to know")),
                List.of("action_id", "reason"),
                (args, call) -> {
                    ActionRecord action = actions.get(Args.required(args, "action_id"));
                    ApprovalRecord approval = approvals.request(action, call.agent(), Args.str(args, "required_role"),
                            Args.required(args, "reason"));
                    call.saw(approval.getId());
                    return "Approval " + approval.getId() + " requested for exactly this version of " + action.getId()
                            + ". Nothing has been executed. A person must decide it.";
                });
    }

    @Bean
    BrainTool actionStatus(ActionService actions, ApprovalRepository approvalRecords, ApprovalService approvals) {
        return BrainTool.of("action_status", Map.of("action_id", str("The action to check")), List.of("action_id"),
                (args, call) -> {
                    ActionRecord action = actions.get(Args.required(args, "action_id"));
                    if (!action.getTenantId().equals(call.tenantId())) {
                        return "Not permitted.";
                    }
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("action_id", action.getId());
                    out.put("type", action.getActionType());
                    out.put("target", action.getTarget());
                    out.put("status", action.getStatus());
                    out.put("needs_approval", actions.needsApproval(action));
                    out.put("authorised_now", approvals.isAuthorised(action));
                    out.put("approvals", approvalRecords.findBySubjectRef(action.getId()).stream()
                            .map(a -> Map.of("id", a.getId(), "state", a.getState().name(),
                                    "decided_by", String.valueOf(a.getDecidedBy())))
                            .toList());
                    out.put("result", action.getResult());
                    call.saw(action.getId());
                    return Json.write(out);
                });
    }

    @Bean
    BrainTool executeAction(ActionService actions) {
        return executor("execute_action", "the action's own channel", actions, null);
    }

    @Bean
    BrainTool sendMessage(ActionService actions) {
        return executor("send_message", "email", actions, "send_message");
    }

    @Bean
    BrainTool publishContent(ActionService actions) {
        return executor("publish_content", "publish", actions, "publish_content");
    }

    private static BrainTool executor(String name, String channelLabel, ActionService actions, String expectedType) {
        return BrainTool.of(name, Map.of("action_id", str("The approved action to carry out via " + channelLabel)),
                List.of("action_id"), (args, call) -> {
                    String id = Args.required(args, "action_id");
                    try {
                        ActionRecord current = actions.get(id);
                        String channel = expectedType == null ? current.getTool()
                                : expectedType.equals("send_message") ? "email" : "publish";
                        ActionRecord done = actions.execute(id, channel, expectedType, call.agent());
                        return switch (done.getStatus()) {
                            case EXECUTED -> "Executed " + id + ": " + done.getResult();
                            case FAILED -> "Failed: " + id + " did not complete (" + done.getResult()
                                    + "). Say so; don't report success.";
                            default -> "Not executed: " + id + " is " + done.getStatus() + ".";
                        };
                    } catch (GovernanceException e) {
                        return "Refused: " + e.getMessage();
                    }
                });
    }

    @Bean
    BrainTool saveDraft(OutputRepository outputs) {
        return BrainTool.of("save_draft", Map.of(
                        "title", str("Draft title"),
                        "content", str("The draft"),
                        "audience", str("Who it is for"),
                        "source_refs", strs("Evidence the draft's claims rest on")),
                List.of("title", "content"),
                (args, call) -> {
                    OutputRecord o = output(call, OutputType.OUT_10, OutputRecord.Kind.DRAFT, Args.required(args, "title"),
                            Args.list(args, "source_refs"));
                    o.setData(Map.of("content", Args.required(args, "content"),
                            "audience", String.valueOf(Args.str(args, "audience")), "approval_state", "unsent"));
                    OutputRecord saved = outputs.save(o);
                    call.saw(saved.getId());
                    return "Saved draft " + saved.getId() + ". It has not been sent.";
                });
    }

    @Bean
    BrainTool createArtefact(OutputRepository outputs) {
        return BrainTool.of("create_artefact", Map.of(
                        "title", str("Artefact title"),
                        "artefact_type", str("report, pack, guide..."),
                        "content", str("The artefact content (Markdown)"),
                        "snapshot_refs", strs("The evidence/snapshot it is dated to")),
                List.of("title", "artefact_type", "content"),
                (args, call) -> {
                    OutputRecord o = output(call, OutputType.OUT_11, OutputRecord.Kind.ARTEFACT,
                            Args.required(args, "title"), Args.list(args, "snapshot_refs"));
                    o.setData(Map.of("artefact_type", Args.required(args, "artefact_type"),
                            "content", Args.required(args, "content"), "snapshot_date", o.getCapturedTime() == null
                                    ? java.time.Instant.now().toString() : o.getCapturedTime().toString()));
                    OutputRecord saved = outputs.save(o);
                    call.saw(saved.getId());
                    return "Created artefact " + saved.getId();
                });
    }

    @Bean
    BrainTool escalate(OutputRepository outputs, AuditLog audit) {
        return BrainTool.of("escalate", Map.of(
                        "reason", str("Why: consequence, uncertainty, failure or policy"),
                        "route", str("human_review, fallback or escalate"),
                        "target_role", str("Who should receive it"),
                        "consequence", str("none, low, medium, high or critical"),
                        "subject_ref", str("What it is about")),
                List.of("reason", "route"),
                (args, call) -> {
                    Route route = Args.enumOf(Route.class, args, "route", Route.ESCALATE);
                    OutputRecord o = output(call, OutputType.OUT_17, OutputRecord.Kind.ROUTE,
                            Args.required(args, "reason"), Args.list(args, "subject_ref"));
                    o.setRoute(route);
                    o.setData(Map.of("target_role", String.valueOf(Args.str(args, "target_role")),
                            "consequence", String.valueOf(Args.str(args, "consequence"))));
                    OutputRecord saved = outputs.save(o);
                    audit.record(call.agent(), "escalated", String.valueOf(Args.str(args, "subject_ref")), route.name(),
                            Map.of("output", saved.getId()));
                    return "Escalated as " + saved.getId() + " (" + route.name().toLowerCase() + ").";
                });
    }

    static OutputRecord output(BrainCall call, OutputType type, OutputRecord.Kind kind, String summary,
            List<String> refs) {
        OutputRecord o = new OutputRecord();
        o.setTenantId(call.tenantId());
        o.setScopeRef(EvidenceTools.scopeOf(call));
        o.setOutputType(type.code());
        o.setKind(kind);
        o.setBehaviour(call.behaviour());
        o.setSummary(summary);
        o.setEvidenceRefs(new ArrayList<>(refs));
        o.setConversationId(call.conversationId());
        return o;
    }
}
