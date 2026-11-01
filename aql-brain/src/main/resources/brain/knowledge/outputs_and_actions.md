---
name: outputs_and_actions
description: The governed output and action types OUT-01 to OUT-22 with persistence, visibility, execution authority and required metadata
phase: 1
status: canonical
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-05, GV-06, GV-10, GV-11
---
# Outputs and actions

Recommendation, preparation and execution are distinct. An output type names what was produced; the authority
column is the level needed to produce it, not a licence for anything further.

| Code | Type | Persisted | Visible | Authority | Required metadata |
|---|---|---|---|---|---|
| `OUT-01` | Evidence set | Persistent | Conditional | Read | source, date, freshness, confidence, claim class, limits |
| `OUT-02` | Classification/resolution | Persistent | Conditional | Compute | basis, confidence, alternatives/unresolved |
| `OUT-03` | Derived analysis | Persistent | Yes | Compute | method/version, inputs, confidence, provenance (defined calculations belong to `PC-19`) |
| `OUT-04` | Verdict/judgement | Persistent | Yes | Compute | criteria, basis, confidence, limits |
| `OUT-05` | Score/ranking | Persistent | Yes | Compute | method/version, dimensions, confidence, personalisation state |
| `OUT-06` | Personalised priority/fit | Persistent | Yes | Compute | explicit preference inputs; underlying evidence unchanged |
| `OUT-07` | Comparison/delta | Conditional | Yes | Compute | baseline, comparable dimensions, missing evidence |
| `OUT-08` | Answer/explanation | Conversation | Yes | Read | citations/provenance as appropriate, entitlement depth |
| `OUT-09` | Recommendation | Persistent | Yes | Recommend | basis, consequence, priority, execution authority |
| `OUT-10` | Draft content/communication | Persistent | Yes | Draft | sources, audience, persona, approval state |
| `OUT-11` | Report/Pack/Guide artefact | Persistent | Yes | Create | snapshot date, evidence, version, ownership/provenance |
| `OUT-12` | Material change signal | Persistent | Conditional | Monitor | old/new, cause, materiality, confidence, attribution |
| `OUT-13` | Plan/workflow | Persistent | Yes | Plan | goal, state, dependencies, authority per step |
| `OUT-14` | External communication | Audit | Yes | Execute | actor, approval, recipient, payload, tool, timestamp, result |
| `OUT-15` | System state/tool action | Audit | Conditional | Execute | actor, authority, before/after, tool, result |
| `OUT-16` | Evaluation result | Persistent | No | Evaluate | criteria/version, score/failures, evaluator |
| `OUT-17` | Route/escalation | Audit | Conditional | Route | reason, consequence, confidence, target |
| `OUT-18` | Outcome/calibration record | Persistent | No | Learn | prior snapshot, observed result, timing, attribution limits |
| `OUT-19` | Affordability analysis | Conditional | Yes | Compute | inputs, assumptions, method version, known vs user-provided vs estimated, sufficiency, entitlement depth, perimeter state |
| `OUT-20` | Dynamic canvas composition | Runtime only | Yes | Plan | active Context, region/object IDs, source refs, permissions, composition reason |
| `OUT-21` | Conversation/interaction state | Persistent | No | Compute | conversation ID, channel, active Context, referents, permission scope, updated time |
| `OUT-22` | Contextual artefact/navigation target | Conditional | Yes | Route | target/source ID, canonical/working status, provenance, permission/authority, action type |

## Rules
- `OUT-14` and `OUT-15` are the only execution outputs. They require explicit authority, an approval record where
  configured, and an audit record of basis, actor, tool and result.
- A draft (`OUT-10`) is unsent. Producing it never implies authority to send or publish.
- `OUT-19` Explore output is a useful free headline calculation; Reveal deepens it with full-cost scenarios and
  sensitivity. Affordability may feed Settle Score only when sufficient.
- `OUT-20` and `OUT-22` route and compose; they never carry broader action authority than the permitted target.
- Every agent returns its output in the common envelope defined by the output_envelope skill.
