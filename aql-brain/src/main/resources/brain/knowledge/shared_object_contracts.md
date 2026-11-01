---
name: shared_object_contracts
description: Minimum shared meaning and fields for the objects that cross AQL loops, products and domains, from Source to Agent Run
phase: 1
status: canonical
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-01, GV-02, GV-05, GV-10, GV-11
---
# Shared object contracts

A semantic contract, not a database schema. Products and domains may extend these objects with specialist fields
but must not redefine their shared meaning. Where a field does not apply it need not exist; where it applies but is
unknown, the unknown or insufficient state stays explicit rather than fabricated. Detailed contracts live in
signal_model (events), context_model, outputs_and_actions, outcome_taxonomy and governance_controls.

| Object | Minimum shared fields | Boundary |
|---|---|---|
| Source | source_id; type/provider/system; entity refs; authority/canonical status; captured and observed time; freshness; version; access/classification; licence | Accessible source is not Evidence and not organisational truth; external systems may stay authoritative |
| Evidence | evidence_id; source refs; claim/value + type; entity refs; observed/extracted status; time; freshness; confidence; sufficiency/conflict state; lineage; access | Not inference, judgement or Knowledge; conflict and absence explicit |
| Knowledge | knowledge_id; content; scope/jurisdiction; status (source, working, proposed, approved, canonical); authority refs; effective date/version; owner; provenance; access; review/staleness | Retrievable or working material is not Knowledge; no competing truth where another system is canonical |
| Intelligence | intelligence_id; type; entity; evidence/knowledge refs; method/model/rule/prompt version; assumptions; values; confidence; sufficiency/limits; time; lineage | Derived, not observed; not yet Judgement |
| Judgement | judgement_id; inputs; criteria/methodology; conclusion; scope; confidence; inspectable basis; known unknowns; version/time | Not a Decision; not hidden chain of thought |
| Personalisation | personalisation_id; underlying refs; context/preference refs; actor; resulting fit/priority; weights; confidence/sufficiency; version | Re-weights; never rewrites facts |
| Recommendation | recommendation_id; action/consideration; basis refs; why; consequence/materiality; priority; assumptions; required authority/approval class; version | Not Decision, not Action; good advice can still meet a poor outcome |
| Decision | decision_id; choice; decision-maker; authority basis; snapshot refs of evidence/judgement/recommendation; alternatives; time; rationale; status; conditions | Not an `LP-01` stage; decision-time state immutable as history |
| Approval | approval_id; subject/action/version ref; requester; required approvers; policy basis; state; conditions; time; expiry; reapproval trigger; audit refs | Not execution and not broader authority; returns authorised/rejected state |
| Action | action_id; type; target/tool; actor; context/entity refs; payload; authority requirement; approval ref; consequence/reversibility; before/after; status/result; time; audit refs | Knowing, recommending or preparing is not permission to execute |
| Outcome | outcome_id; family/type; entity refs; observed/inferred; source; event/effective/captured time; prior output/action ref; frozen snapshot; result; attribution limits; correction state | No-feedback is not a label; records what happened, not whether the decision was reasonable |
| Learning | learning_id; source outcome/evaluation refs; lesson/calibration; scope; support; method/version; privacy constraints; reuse level; status; review state | Not raw telemetry; not automatic model change; local does not become shared by default |
| Context | context ref; actor/role; active entity/object/surface; task; referents; time; permissions; authority; sensitivity; jurisdiction; preferences | An input/scope, not a stage; cannot expand access or authority |
| Work Context | work_context_id; objective; entity/venture/deal refs; actors/roles; work and dependency refs; artefact/evidence/decision refs; permissions/authority; sensitivity; history | Not a UI workspace; association does not share restricted items |
| Agent Run | run_id; initiator; objective; Work Context; permitted data, tools and actions; authority; budget; dependencies; state; outputs; failure; handoff; result; audit/version refs | An execution object, not an authority source; agent cannot self-expand scope, tools, authority or budget |

## Cross-cutting rules
- Every object that crosses a boundary needs a stable typed reference (`AR-01`) and preserves time and version
  (`AR-02`).
- Approval must bind the exact Action version and be invalidated on material change (see
  authority_and_approval_matrix).
- Recommendation is transient by default and persisted when continuity, audit, lineage or evaluation needs it.
- Deterministic calculations, rules, models and language-model analysis share one output envelope for lineage and
  confidence without pretending to be the same mechanism.
