---
name: signal_model
description: AQL signal layers, the catalogue of signals SG-001 to SG-038 with status and materiality gate, and the common signal contract fields
phase: 1
status: canonical
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-02, GV-08, GV-13
---
# Signal model

## Layers
Raw source event → normalised evidence change → derived intelligence change → material user-relevant signal →
prompt/action trigger → observed outcome. Each layer is distinct. Nothing is notified or acted on from a raw mutation
by default; operational incidents are the only direct path.

## Signals
Status: O = observed, I = inferred. Gate: whether materiality must be assessed (Yes, No, Conditional, Operational).

| Code | Layer | Signal | O/I | Gate | Key rule |
|---|---|---|---|---|---|
| `SG-001` | Raw | Source record update | O | No | Never notify merely because a source mutated |
| `SG-002` | Raw | Source freshness/health | O | Operational | May affect confidence/fallback before user intelligence |
| `SG-003` | Evidence | Normalised fact change | O | Conditional | Keep old/new, source, date, confidence, attribution |
| `SG-004` | Evidence | Evidence conflict | O | Yes | Do not silently choose unless hierarchy supports it |
| `SG-005` | Evidence | Known unknown / evidence gap | I | Yes | First-class state, not blank or fabricated |
| `SG-006` | Intelligence | Risk finding/change | I | Yes | Material risk shown at overview depth; never paywalled |
| `SG-007` | Intelligence | Price/value finding/change | I | Yes | Price Verdict stays within Property Score price/value dimension |
| `SG-008` | Intelligence | Cost finding/change | I | Yes | Separate factual cost from personal affordability |
| `SG-009` | Intelligence | Property Score change | I | Yes | Consequence carried on the causal change; not a source signal |
| `SG-010` | Intelligence | Area Score change | I | Yes | Same consequence rule as `SG-009` |
| `SG-011` | Personalisation | Preference/must-have change | O | No | Not presented as external Watch change |
| `SG-012` | Personalisation | Settle Score change | I | Conditional | Recompute on edits; alert only for external causes |
| `SG-013` | Document | Document uploaded/received/versioned | O | No | Raw private documents stay private |
| `SG-014` | Document | Document finding/conflict | I | Yes | Document evidence is not canonical truth |
| `SG-015` | Process | Process state transition | O | Conditional | Typed event with actor, time, basis |
| `SG-016` | Process | Deadline/time window | I | Yes | Time may trigger reevaluation without external change |
| `SG-017` | Relationship | Home-management signal | I | Yes | Relationship is Settle-owned; artefacts stay in Library |
| `SG-018` | Behaviour | User intent/interaction | O | No | Capture only with defensible purpose |
| `SG-019` | Outcome | Property/process outcome | O | Yes | Snapshot prediction before outcome |
| `SG-020` | Outcome | Action outcome | O | Yes | Separate recommendation quality from tool failure |
| `SG-021` | GTM | Research/market signal | O | Yes | Phase 1 sensing may be assisted/manual |
| `SG-022` | GTM | Content/campaign event | O | Operational | Consequential publishing human-approved |
| `SG-023` | GTM | Performance outcome | O | Yes | Never overrides accuracy, independence, trust |
| `SG-024` | Delivery | Requirement/delivery state change | O | Yes | Read-only Phase 1 delivery intelligence |
| `SG-025` | Control | Evaluation result | I | Yes | Criteria/version attached |
| `SG-026` | Control | Model/rule/version change | O | Operational | Traceable to outputs and regression state |
| `SG-027` | Control | Permission/authority change | O | Yes | Hard deterministic edge |
| `SG-028` | Control | Failure/degradation/incident | O | Yes | Honest degradation, explicit fallback |
| `SG-029` | Behaviour | User correction/challenge | O | Yes | Preserve original snapshot and correction |
| `SG-030` | Behaviour | User preference/edit event | O | No | May recompute; never an external Watch event |
| `SG-031` | Knowledge | Governed knowledge change | O | Conditional | High-consequence changes may need a human gate before release |
| `SG-032` | Outcome | Outcome-family event | I | No | One of 19 families; never label no-feedback; corrections propagate |
| `SG-033` | Personalisation | Affordability input/change | O | No | Sensitive; user edits are not Watch changes |
| `SG-034` | Intelligence | Affordability finding/change | I | Conditional | Estimate, not lender decision; keep assumptions/version |
| `SG-035` | Relationship | Budget and cost-optimisation signal | I | Yes | Financial management distinct from affordability calculation |
| `SG-036` | Interaction | Active context/referent change | O | No | Halie/OS runtime state |
| `SG-037` | Interaction | Conversation/channel state change | O | No | Keep channels distinct |
| `SG-038` | Interface | Canvas/context recomposition request | O | No | Explicit user/system request |

## Signal contract (common event fields)
Always: `event_id` (unique, immutable); `event_family` (controlled taxonomy); `event_type` (versioned subtype);
`entity_refs` (no unsafe merge; unresolved identity explicit); `initiator` (User, System, AQL, Automation, External
event; not a prompt class); `observed_inferred`; `event_time` (distinct from processing time); `captured_time`;
`retention_class`; `correction_revocation_state` (Current, Corrected, Revoked, Erased/Anonymised).

Conditional: `outcome_source` (User-confirmed, Public record, System event, Professional document, AQL output,
Inference) for outcomes; `evidence_snapshot_ref` for evaluation or learning; `model_rule_version` for AQL-derived
events; `prediction_recommendation_ref` when evaluating a prior output; `confidence` for inferred values (not
sufficiency); `materiality` (Not material, Material, Critical) for changes; `user_relevance` (Relevant, Not relevant)
for Watch and attention; `old_value_ref`, `new_value_ref`, `cause_ref`, `lineage_refs` for changes;
`permission_consent_basis` for personal, behavioural or private data.

## Fixed rules
- Observed vs inferred is never blurred; no-feedback never becomes a label.
- Prior state is frozen before evaluation; predictions are never reconstructed after outcomes.
- User edits and preference changes are excluded from external-change Watch events.
- Score movement is a consequence, not a source signal.
- There is no absolute append-only rule for personal data; erasure and correction propagate.
