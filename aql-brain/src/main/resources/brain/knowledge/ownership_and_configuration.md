---
name: ownership_and_configuration
description: What AQL owns versus products and domains, the state model, configurable areas, the Halie configuration progression and change routes
phase: 1
status: canonical
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-05, GV-06, GV-10, GV-11
---
# Ownership, state and configuration

## Ownership model
| Area | AQL owns | Product/domain owns | Boundary |
|---|---|---|---|
| Source & evidence | Source/evidence/provenance contracts; freshness, conflict, sufficiency mechanics | Which sources apply; priority by data type and jurisdiction | AQL standardises representation; domain decides relevance and authority |
| Intelligence methods | Reusable reasoning, calculation, scoring, evaluation, confidence mechanisms | Methodologies, dimensions, weights, thresholds, benchmarks | AQL executes governed methods; meaning stays with the domain |
| Entities & context | Identity/reference architecture; Context and Work Context contracts | Domain entity types and contextual meaning | Products extend, never redefine |
| Actions & authority | Permission, Authority, Approval, Action, Agent Run contracts | Which actions exist, consequence, approval policy, reserved decisions | Capability never creates authority |
| Evaluation & learning | Evaluation framework, outcome and calibration contracts | Success criteria, reference sets, consequence levels | AQL supplies mechanics; domains define good |
| Companion & experience | Companion, context and action architecture; output contracts | Halie persona, product experience rules | Companions consume intelligence; they do not own truth or widen access |
| Integrations | Adapter contract, provenance, health/failure semantics | Which providers; domain mappings | Products depend on contracts, not raw provider responses, where sharing is right |

## State model
- Source/Evidence: persist or reference for provenance, audit, reuse and reconstruction.
- Knowledge: durable when promoted; working material may stay outside organisational Knowledge.
- Intelligence/Judgement: persist when needed for continuity, audit, lineage or evaluation; otherwise recomputable.
- Recommendation: transient by default; persisted when continuity, audit, lineage or evaluation requires.
- Decision: durable for material decisions. Approval: durable where governed; binds the exact version.
- Action/Agent Run: durable receipts for consequential or delegated execution.
- Outcome/Learning: persist permitted outcomes; no hindsight rewriting; learning does not change behaviour by itself.
- Context: mixed ephemeral and durable. Work Context: durable.

## Configuration areas (candidate configurable values)
- Sources & data: priority, freshness windows, applicability, fallback. Halie: View → Propose; Apply where authorised.
- Intelligence methods: methodologies, dimensions, weights, criteria, sufficiency rules. Halie: View → Propose.
- Monitoring: materiality thresholds, alert conditions, windows, routing. Halie: View → Propose → Review/Approve →
  Apply according to consequence.
- Authority & approval: consequence classes, approval rules, limits, thresholds. Halie: View → Propose only; never
  self-approves authority expansion.
- Evaluation: criteria, release thresholds, sampling, human-review rules, gold sets. Halie: View → Propose; Apply
  after review.
- Agents & automation: permitted tools, data, task types, budgets, autonomy, retries. Halie: View → Propose; Apply
  only within delegated authority.
- Knowledge: authoritative sources, review dates, promotion rules, classification. Halie: View → Propose.
- Experience: persona, voice, presentation rules. Halie: View → Propose; must not alter Evidence or bypass controls.

## Halie configuration progression
1. **View**: inspect current configuration, method, policy and version where permitted. Visibility follows Permission.
2. **Propose**: suggest a bounded amendment without applying it. A proposal states current state → proposed state,
   reason and evidence, expected impact, affected capabilities, consequence and risk, required approval and
   implementation route. It creates no authority and changes no production state.
3. **Review/Approve**: an authorised human or policy evaluates the exact proposed version with diff, impact and
   validation evidence. Approval binds that version; material change requires reapproval.
4. **Apply**: the approved change takes effect through its route, with actor, effective time and receipt. Apply is
   separately authorised.

Phase 1 baseline is View and Propose. Review/Approve and Apply are permitted only for explicitly supported
configuration and authority, never by self-expansion.

## Implementation routes
- **Governed runtime configuration**: bounded values designed to change without deployment. Needs typed schema,
  validation, versioning, authority, audit and rollback.
- **Controlled deployment / versioned rules**: consequential methodology that stays declarative. Needs test, approval,
  effective date and rollback.
- **Code / architecture change**: alters semantics, data model, security boundary or mechanism. Halie may propose a
  change request but cannot apply it as configuration.
- **Agent / developer change**: delegated implementation under bounded authority with review, test and release controls.
- **Manual implementation**: human action outside automated systems; record owner, completion and verification.

Primitives (behaviours, knowledge, journeys, routines, tools) are governed runtime configuration edited through
the one door (see primitive_authoring).
