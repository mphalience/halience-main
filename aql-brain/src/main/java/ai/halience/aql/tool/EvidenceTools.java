package ai.halience.aql.tool;

import static ai.halience.aql.tool.BrainTool.bool;
import static ai.halience.aql.tool.BrainTool.num;
import static ai.halience.aql.tool.BrainTool.obj;
import static ai.halience.aql.tool.BrainTool.str;
import static ai.halience.aql.tool.BrainTool.strs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ai.halience.aql.domain.Evidence;
import ai.halience.aql.domain.Json;
import ai.halience.aql.domain.KnowledgeItem;
import ai.halience.aql.domain.OutputRecord;
import ai.halience.aql.domain.Repositories.EvidenceRepository;
import ai.halience.aql.domain.Repositories.KnowledgeRepository;
import ai.halience.aql.domain.Repositories.OutputRepository;
import ai.halience.aql.domain.Snapshot;
import ai.halience.aql.governance.PermissionService;
import ai.halience.aql.outcome.OutcomeService;
import ai.halience.aql.primitive.PrimitiveLibrary;
import ai.halience.aql.spec.ObservedInferred;
import ai.halience.aql.spec.OutputType;
import ai.halience.aql.spec.SpecCode;
import ai.halience.aql.spec.Vocabulary.ClaimClass;
import ai.halience.aql.spec.Vocabulary.KnowledgeStatus;
import ai.halience.aql.spec.Vocabulary.Sufficiency;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Retrieval and recording tools over governed evidence, knowledge and outputs (PC-01, PC-02, PC-05). Retrieval checks
 * permission on every record before it reaches the model (GV-18), and labels knowledge with its status so working
 * material is never passed off as canonical (GV-19).
 */
@Configuration
public class EvidenceTools {

    @Bean
    BrainTool evidenceSearch(EvidenceRepository evidence, PermissionService permissions) {
        return BrainTool.of("evidence_search", Map.of(
                        "query", str("Words to find in the claim or claim type"),
                        "limit", num("Most results to return (default 20)")), List.of("query"),
                (args, call) -> {
                    int limit = Args.integer(args, "limit") == null ? 20 : Math.min(50, Args.integer(args, "limit"));
                    List<Map<String, Object>> out = new ArrayList<>();
                    for (Evidence e : evidence.searchCurrent(call.tenantId(), Args.required(args, "query"))) {
                        if (out.size() >= limit) {
                            break;
                        }
                        if (!permissions.canSee(call.agent(), e)) {
                            continue;
                        }
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("evidence_id", e.getId());
                        row.put("claim", e.getClaim());
                        row.put("claim_type", e.getClaimType());
                        row.put("claim_class", e.getClaimClass());
                        row.put("value", e.getValue());
                        row.put("source_refs", e.getSourceRefs());
                        row.put("confidence", e.getConfidence());
                        row.put("fresh_as_of", String.valueOf(e.getFreshAsOf()));
                        row.put("conflict_state", e.getConflictState());
                        row.put("effective_time", String.valueOf(e.getEffectiveTime()));
                        out.add(row);
                        call.saw(e.getId());
                    }
                    return out.isEmpty() ? "No current evidence matches. Treat this as a known unknown, not as a "
                            + "negative finding." : Json.write(out);
                });
    }

    @Bean
    BrainTool knowledgeSearch(KnowledgeRepository knowledge, PermissionService permissions) {
        return BrainTool.of("knowledge_search", Map.of(
                        "query", str("Words to find in the title or content"),
                        "include_working", bool("Also return working and proposed material (labelled as such)")),
                List.of("query"),
                (args, call) -> {
                    List<KnowledgeStatus> statuses = new ArrayList<>(List.of(KnowledgeStatus.CANONICAL,
                            KnowledgeStatus.APPROVED));
                    if (Args.bool(args, "include_working")) {
                        statuses.addAll(List.of(KnowledgeStatus.WORKING, KnowledgeStatus.PROPOSED));
                    }
                    List<Map<String, Object>> out = new ArrayList<>();
                    for (KnowledgeItem k : knowledge.search(call.tenantId(), Args.required(args, "query"), statuses)) {
                        if (!permissions.canSee(call.agent(), k) || !k.isCurrent()) {
                            continue;
                        }
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("knowledge_id", k.getId());
                        row.put("title", k.getTitle());
                        row.put("status", k.getStatus().name().toLowerCase());
                        row.put("authoritative_source", k.getAuthoritativeSource());
                        row.put("owner", k.getOwner());
                        row.put("effective_time", String.valueOf(k.getEffectiveTime()));
                        row.put("review_by", String.valueOf(k.getReviewBy()));
                        String content = k.getContent() == null ? "" : k.getContent();
                        row.put("content", content.length() > 4000 ? content.substring(0, 4000) + " [truncated]"
                                : content);
                        out.add(row);
                        call.saw(k.getId());
                        if (out.size() >= 15) {
                            break;
                        }
                    }
                    return out.isEmpty() ? "No knowledge matches." : Json.write(out);
                });
    }

    @Bean
    BrainTool artefactOpen(OutputRepository outputs, KnowledgeRepository knowledge, EvidenceRepository evidence,
            PermissionService permissions) {
        return BrainTool.of("artefact_open", Map.of("ref", str("The id of an output, knowledge item or evidence")),
                List.of("ref"), (args, call) -> {
                    String ref = Args.required(args, "ref");
                    Object found = outputs.findById(ref).map(Object.class::cast)
                            .or(() -> knowledge.findById(ref).map(Object.class::cast))
                            .or(() -> evidence.findById(ref).map(Object.class::cast)).orElse(null);
                    if (found == null) {
                        return "Not found: " + ref + ". It may not exist or may live in a source system that isn't "
                                + "connected.";
                    }
                    ai.halience.aql.domain.AqlRecord record = (ai.halience.aql.domain.AqlRecord) found;
                    if (!permissions.canSee(call.agent(), record)) {
                        return "Not permitted: you can't open " + ref + ".";
                    }
                    call.saw(ref);
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("ref", ref);
                    out.put("current", record.isCurrent());
                    out.put("correction_state", record.getCorrectionState());
                    out.put("superseded_by", record.getSupersededBy());
                    out.put("captured", String.valueOf(record.getCapturedTime()));
                    switch (found) {
                        case OutputRecord o -> {
                            out.put("type", o.getOutputType());
                            out.put("status", "agent-produced");
                            out.put("produced_by", o.getBehaviour() + "@" + o.getBehaviourRevision());
                            out.put("summary", o.getSummary());
                            out.put("claims", o.getClaims());
                            out.put("data", o.getData());
                        }
                        case KnowledgeItem k -> {
                            out.put("type", "knowledge");
                            out.put("status", k.getStatus().name().toLowerCase());
                            out.put("authoritative_source", k.getAuthoritativeSource());
                            out.put("title", k.getTitle());
                            out.put("content", k.getContent());
                        }
                        case Evidence e -> {
                            out.put("type", "evidence");
                            out.put("claim", e.getClaim());
                            out.put("source_refs", e.getSourceRefs());
                            out.put("source_location", e.getSourceLocation());
                        }
                        default -> {
                        }
                    }
                    return Json.write(out);
                });
    }

    @Bean
    BrainTool recordEvidence(EvidenceRepository evidence) {
        return BrainTool.of("record_evidence", Map.of(
                        "claim", str("The claim, fact or value as stated by the source"),
                        "claim_type", str("e.g. lease_term, asking_price, requirement_status"),
                        "source_refs", strs("Where it came from: source ids, document ids, URLs"),
                        "source_location", str("Page, clause or field in the source"),
                        "entity_refs", strs("The entities it is about"),
                        "value", obj("Structured value, if any"),
                        "confidence", num("Extraction confidence 0-1"),
                        "observed_inferred", str("observed (default) or inferred")),
                List.of("claim", "claim_type", "source_refs"),
                (args, call) -> {
                    List<String> sources = Args.list(args, "source_refs");
                    if (sources.isEmpty()) {
                        throw BrainTool.input("evidence needs at least one source ref (GV-01)");
                    }
                    Evidence e = new Evidence();
                    e.setTenantId(call.tenantId());
                    e.setScopeRef(scopeOf(call));
                    e.setClaim(Args.required(args, "claim"));
                    e.setClaimType(Args.required(args, "claim_type"));
                    e.setSourceRefs(new ArrayList<>(sources));
                    e.setSourceLocation(Args.str(args, "source_location"));
                    e.setEntityRefs(new ArrayList<>(Args.list(args, "entity_refs")));
                    e.setValue(Args.map(args, "value"));
                    e.setConfidence(Args.number(args, "confidence"));
                    e.setObservedInferred(Args.enumOf(ObservedInferred.class, args, "observed_inferred",
                            ObservedInferred.OBSERVED));
                    e.setClaimClass(e.getObservedInferred() == ObservedInferred.OBSERVED ? ClaimClass.OBSERVED
                            : ClaimClass.DERIVED);
                    e.setLineageRefs(new ArrayList<>(sources));
                    Evidence saved = evidence.save(e);
                    call.saw(saved.getId());
                    return "Recorded evidence " + saved.getId();
                });
    }

    @Bean
    BrainTool recordIntelligence(OutputRepository outputs, PrimitiveLibrary library) {
        return BrainTool.of("record_intelligence", Map.of(
                        "output_type", str("OUT code, e.g. OUT-03 (derived analysis), OUT-04 (verdict), OUT-05 (score)"),
                        "kind", str("intelligence, judgement, score, personalisation, comparison, recommendation, "
                                + "calculation, plan"),
                        "summary", str("The result in words, with its basis"),
                        "evidence_refs", strs("Evidence/knowledge ids it rests on"),
                        "method_version", str("The method, rule or model version, e.g. affordability-v0.1"),
                        "confidence", num("0-1"),
                        "sufficiency", str("sufficient, partial, insufficient or unknown"),
                        "known_unknowns", strs("What is missing, stale or conflicting"),
                        "data", obj("Structured result: breakdown, values, criteria")),
                List.of("output_type", "kind", "summary", "evidence_refs"),
                (args, call) -> {
                    OutputType type = SpecCode.parse(OutputType.class, Args.required(args, "output_type"))
                            .orElseThrow(() -> BrainTool.input("output_type must be an OUT code"));
                    OutputRecord o = new OutputRecord();
                    o.setTenantId(call.tenantId());
                    o.setScopeRef(scopeOf(call));
                    o.setOutputType(type.code());
                    o.setKind(Args.enumOf(OutputRecord.Kind.class, args, "kind", null));
                    o.setBehaviour(call.behaviour());
                    o.setBehaviourRevision(library.current().agent(call.behaviour())
                            .map(p -> library.revision(p.key())).orElse(null));
                    o.setSummary(Args.required(args, "summary"));
                    o.setEvidenceRefs(new ArrayList<>(Args.list(args, "evidence_refs")));
                    String method = Args.str(args, "method_version");
                    o.setMethodVersions(method == null ? new ArrayList<>() : new ArrayList<>(List.of(method)));
                    o.setConfidence(Args.number(args, "confidence"));
                    o.setSufficiency(Args.enumOf(Sufficiency.class, args, "sufficiency", Sufficiency.UNKNOWN));
                    o.setKnownUnknowns(new ArrayList<>(Args.list(args, "known_unknowns")));
                    o.setData(Args.map(args, "data"));
                    o.setConversationId(call.conversationId());
                    OutputRecord saved = outputs.save(o);
                    call.saw(saved.getId());
                    return "Recorded " + type.code() + " " + saved.getId();
                });
    }

    @Bean
    BrainTool freezeSnapshot(OutcomeService outcomes) {
        return BrainTool.of("freeze_snapshot", Map.of(
                        "refs", strs("Output, recommendation, decision or action ids to freeze"),
                        "reason", str("Why: e.g. before awaiting the outcome of this recommendation")),
                List.of("refs", "reason"),
                (args, call) -> {
                    Snapshot s = outcomes.freeze(Args.list(args, "refs"), Args.required(args, "reason"), call.agent());
                    call.saw(s.getId());
                    return "Frozen as " + s.getId() + " (hash " + s.getContentHash().substring(0, 12) + ")";
                });
    }

    static String scopeOf(BrainCall call) {
        Object scope = call.context().activeContext().get("scope");
        return scope == null ? null : String.valueOf(scope);
    }
}
