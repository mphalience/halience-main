---
name: governance_controls
description: The 22 non-negotiable AQL controls GV-01 to GV-22 with the required rule, scope, Phase 1 position and engineering check
phase: 1
status: canonical
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
---
# Governance controls

Positions: Mandatory (always applies), Default (applies until a capability is explicitly graduated), Bounded,
Interface ready (contract preserved now, workflow later).

| Code | Control | Rule | Applies to | Position | Check |
|---|---|---|---|---|---|
| `GV-01` | Grounding | Material outputs trace to governed evidence and expose known unknowns | `PC-01`–`PC-12` | Mandatory | Evidence IDs, source, date, freshness, confidence available to output and eval |
| `GV-02` | Claim honesty | Distinguish observed, derived and interpretive claims | All intelligence and generation | Mandatory | Claim class retained through transformations |
| `GV-03` | Confidence & sufficiency | Low confidence or insufficiency changes behaviour, not just display | All | Mandatory | Thresholds route to degrade, unknown or escalation by consequence |
| `GV-04` | Permissions & entitlement | Depth, data access and actions obey deterministic gates | All | Mandatory | Model cannot override permission, plan or consent rules |
| `GV-05` | Authority | Recommendation, draft and execution are separate authority levels | `PC-11`–`PC-15` | Mandatory | Every executable action declares authority and approval need |
| `GV-06` | Human approval | Consequential actions stay human-approved until explicitly graduated | `PC-15`, GTM publish | Default | Approval record before execution where configured |
| `GV-07` | Failure honesty | Expose unknown, unavailable, degraded or failed states | All | Mandatory | Typed failure states and fallback paths |
| `GV-08` | Privacy boundaries | Data is scoped; raw private documents are not shared learning data by default | All | Mandatory | Purpose, permission and retention tags |
| `GV-09` | Retention & erasure | Retain where lawful and useful; erase, anonymise or minimise where required | Memory, events | Mandatory | No absolute append-only personal-data rule |
| `GV-10` | Auditability | Material decisions, actions and evals record who, what, when, basis, version, result | `PC-06`, `PC-07`, `PC-11`, `PC-15`–`PC-18` | Mandatory | Action/evaluation/audit event contract |
| `GV-11` | Versioning | Outputs trace to source, model, prompt/rule and methodology versions | All | Mandatory | Version IDs in runtime and eval telemetry |
| `GV-12` | Evaluation | Pre-release criteria and production monitoring proportionate to risk | All | Mandatory | Eval suite, release gate, escalation path |
| `GV-13` | Materiality | Separate raw update, evidence change, intelligence change and material change | `PC-13` | Mandatory | No raw-event-to-notification path except incidents |
| `GV-14` | Personalisation honesty | Re-weight without rewriting facts; affordability affects Settle Score only | `PC-07`, `PC-08`, `PC-11` | Mandatory | Evidence stored separately from weights and output |
| `GV-15` | Regulated perimeter | Stay within the Phase 1 legal and regulatory boundary | Settle, GTM | Mandatory | Tool allow-list and jurisdiction/effective-date rules |
| `GV-16` | Learning boundary | Capture outcomes and calibrate; no broad self-directed adaptation | `PC-18` | Bounded | Learning writes need approved purpose and privacy-safe signal |
| `GV-17` | Financial calculation boundary | Affordability is indicative with assumptions; never implies lender eligibility, approval or advice | Affordability, Settle Score finance dimension | Mandatory | Versioned calculation, input provenance, estimate labels, route if perimeter crossed |
| `GV-18` | Work/context permission boundary | Knowing or displaying never grants permission to retain, share or act | Halie, Halience OS, connected systems | Mandatory | Access checked before model/tool use; authority checked separately; audit decisions |
| `GV-19` | Organisational knowledge boundary | Retrievable material is not canonical knowledge | Halie knowledge, artefacts | Mandatory | Keep status and provenance; authoritative source stays authoritative |
| `GV-20` | Privacy-safe organisational learning | Private context informs broader learning only via sanitised, permitted signals | Halie, OS activity, learning | Mandatory | Minimisation and anonymisation before reuse |
| `GV-21` | Capture & recording | Recordings are governed source material, subject to consent, notice, jurisdiction, retention | Capture interfaces | Interface ready | Preserve source and restricted status; promotion follows governed lifecycle |
| `GV-22` | Non-surveillance & psychological safety | Visibility supports coordination, never employee surveillance | Halie, OS, learning, reporting | Mandatory | Private context private by default; report work state, not personal monitoring |

## How controls are applied
- Skills carry most controls into agents (for example grounding for `GV-01`/`GV-02`, human_approval for `GV-06`).
- Deterministic controls (`GV-04`, `GV-05`, `GV-06`, `GV-18`) are enforced by the runtime, not left to model judgement.
- When controls appear to conflict, the more protective control wins and the conflict is routed for review.
