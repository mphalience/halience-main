---
name: prompt_classes
description: The 19 reusable AQL brain jobs PC-01 to PC-19 with each job, primary output, whether it may trigger action, and its core guardrail
phase: 1
status: canonical
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
---
# Prompt Classes

A Prompt Class is the job the brain performs, not the product surface or a fixed workflow step. User, system, AQL,
automation and external-event initiators all share these classes.

| Code | Class | Job | Primary output | May trigger action | Core guardrail |
|---|---|---|---|---|---|
| `PC-01` | Retrieve & Ground | Find governed information and assemble grounded context, no interpretation beyond retrieval | Evidence/context set | No | Source hierarchy, permissions, freshness, provenance; return unknown rather than guess |
| `PC-02` | Extract & Structure | Turn unstructured content into typed facts, entities, claims, events or fields | Structured evidence | Indirect | Extraction is evidence, not truth; keep source location and confidence |
| `PC-03` | Classify & Resolve | Assign canonical type/state; resolve entities and context; route evidential conflict to reconciliation | Classification / resolved entity | Indirect | Never unsafe-merge entities; expose ambiguity |
| `PC-04` | Reconcile & Validate Evidence | Compare evidence across sources; find conflicts, sufficiency, freshness, known unknowns | Reconciled evidence state | Indirect | Source hierarchy; private/user evidence not canonical by default |
| `PC-05` | Analyse & Derive | Synthesise or infer derived intelligence that is not a defined calculation | Derived analysis | Indirect | Keep provenance; separate modelled from sourced; expose assumptions |
| `PC-06` | Evaluate & Judge | Evidence-based judgement against explicit criteria (risk, price position, quality, readiness) | Verdict / judgement | Indirect | Expose basis, confidence and limits; no hidden criteria |
| `PC-07` | Score & Rank | Transparent score or ordering using defined methodology | Score / ranked set | Indirect | Missing evidence lowers confidence, never silently negative; affordability never changes Property or Area Score |
| `PC-08` | Personalise & Prioritise | Re-weight intelligence for the user, goals, Process or situation | Personalised priority / fit | Indirect | Do not alter evidence; preference is not quality; never fabricate financial context |
| `PC-09` | Compare | Compare entities, options, states, periods or evidence sets | Comparison / delta | Indirect | Compatible dimensions and baselines; explain missing or incomparable evidence |
| `PC-10` | Explain & Answer | Translate evidence, analysis or judgement into a contextual answer | Answer / explanation | No | Strongest available context; keep provenance and entitlement boundaries |
| `PC-11` | Recommend & Next-Best-Action | Decide what should be considered or done next, and why | Recommendation | Yes | Recommendation is not execution; consequence, authority and regulated perimeter explicit |
| `PC-12` | Generate & Transform | Create or transform content and artefacts from grounded context | Draft / artefact | Yes | Generated claims stay grounded; persona, privacy, perimeter preserved |
| `PC-13` | Monitor & Detect Change | Compare current to prior state or rules; decide if meaningful change occurred | Change assessment | Yes | Source update is not material change; suppress noise |
| `PC-14` | Plan & Orchestrate | Decompose a goal into ordered steps, dependencies, tools and checkpoints | Plan / workflow | Yes | Respect Process state, tool permissions, approval gates |
| `PC-15` | Execute & Communicate | Perform an authorised action through a tool, API or channel | Action result | Yes | Explicit authority; approval where configured; log basis, actor, tool, result |
| `PC-16` | Evaluate System Quality | Assess an output, model, source or workflow against quality, safety and release criteria | Evaluation result | Yes | Explicit criteria/version; escalate failure; no circular self-certification |
| `PC-17` | Route & Escalate | Decide which capability, source, human, workflow or failure path takes the task next | Routing / escalation | Yes | Use permission, confidence, consequence, availability; never silently degrade consequential tasks |
| `PC-18` | Learn & Calibrate | Link predictions and actions to observed outcomes; update calibration within boundaries | Outcome label / calibration input | Indirect | No hindsight contamination; consent and privacy; no broad autonomous adaptation in Phase 1 |
| `PC-19` | Calculate | Run a defined quantitative calculation from explicit inputs and governed assumptions | Calculated value / scenarios | Indirect | Inputs, formula version and assumptions explicit; estimate not fact; no implied lender decision or advice |

## Boundaries worth remembering
- `PC-19` Calculate is distinct from `PC-05` Derive and `PC-07` Score. Arithmetic belongs to Calculate; interpretation
  to Derive; methodology-laden evaluation to Score.
- `PC-16` evaluates AQL itself (capability and output quality); `PC-06` judges a subject in the world.
- `PC-13` detects change; materiality and attention are decided downstream (`LP-03`, `LP-08`).
- "May trigger action" means the output can lead to an Action; it never confers the authority to take it.
