---
name: aql_taxonomy
description: The shared AQL vocabulary with each canonical term, its meaning and the near-neighbour terms it must not be confused with
phase: 1
status: canonical
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-01, GV-02, GV-05, GV-13
---
# AQL taxonomy

AQL terms have distinct meanings. Use them precisely; near-neighbours are not synonyms.

## Intelligence model
- **Source**: originating material, record or system from which governed Evidence can be derived. An accessible
  Source is not automatically Evidence or Knowledge, and an external system may remain authoritative.
- **Evidence**: a governed observed or extracted claim, fact or value, with provenance and uncertainty. Not
  inference, Judgement or organisational Knowledge (`OUT-01`, `CX-05`).
- **Knowledge**: governed reusable information accepted for an authorised scope. Retrievable, private or working
  material does not become Knowledge merely because AQL can access it.
- **Intelligence**: a derived analytical or modelled result from governed Evidence and/or Knowledge. Derived, never
  an observed fact (`OUT-03`).
- **Judgement**: an evidence-based conclusion against explicit criteria. Not a Decision; preserve an inspectable
  basis, not hidden model reasoning (`OUT-04`).
- **Personalisation**: contextual significance, fit or priority from applying permitted context to Intelligence or
  Judgement. Re-weights; never rewrites facts (`OUT-05`, `OUT-06`).
- **Recommendation**: a proposed consideration or next action. Grants no execution authority; not a loop stage (`OUT-09`).
- **Decision**: an explicit choice by an authorised actor at a point in time. Not a loop stage (`LP-10`).
- **Action**: a proposed, prepared or executed operation that changes state or communicates externally. A loop
  stage, but execution is separately governed (`OUT-14`, `OUT-15`).
- **Outcome**: an observed or inferred later result linked to what it evaluates. Records what happened, not whether
  the earlier decision was reasonable (`OUT-18`).
- **Learning**: a validated lesson or calibration signal for a permitted scope. Not raw telemetry; does not
  automatically become Knowledge or change models.

## Execution, context, event and control
- **Intelligence Loop** (`LP-01`): Sources → Evidence → Knowledge → Intelligence → Judgement → Personalisation →
  Action → Outcomes → Learning. Context scopes it; it is not a stage.
- **Specialist Loop**: a reusable process with its own purpose, state and controls that uses relevant `LP-01` stages
  and may invoke other loops, pause and resume.
- **Prompt Class**: a reusable brain job, independent of surface or initiator (`PC-01` to `PC-19`).
- **Prompt Activity**: a concrete Phase 1 use of a Prompt Class (`PA-001` to `PA-043`).
- **Agent Run**: a bounded execution instance for delegated agent work. Not a source of authority (Phase 2).
- **Context**: structured scope for interpretation, retrieval, reasoning and action. Cannot expand permission or authority.
- **Work Context**: a durable governed boundary for a body of work. Linking does not make restricted items visible.
- **Signal / Event**: a governed observed or inferred change with identity, time, source and lineage. An event does
  not imply relevance or Action.
- **Permission**: whether information or capability may be accessed, used, retained or shared.
- **Authority**: whether an actor may perform a class of Action in scope. Checked at the point of Action.
- **Approval**: authorisation or rejection of one exact proposed Action, change or version. Does not execute or broaden scope.
- **Materiality**: whether a change is significant enough to alter behaviour, routing or treatment. Assessed after
  the Evidence/Intelligence change, as a threshold, not a ranking.

## Key distinctions (controlled vocabulary)
- Retrieve (locate existing) vs Gather (accumulate in research) vs Capture (record incoming material; not truth).
- Extract (structure without adding meaning) vs Derive (infer new Intelligence) vs Calculate (defined formula, `PC-19`)
  vs Score (methodology adds meaning, `PC-07`).
- Classify (assign category) vs Resolve (establish canonical referent) vs Reconcile (cohere competing evidence
  without erasing conflict).
- Validate (condition check) vs Evaluate (criteria-based assessment) vs Verify (confirm a change took effect).
- Judge (conclude) vs Decide (choose) vs Approve (authorise exact subject) vs Confirm (affirm; not governed approval).
- Compare (differences) vs Rank (order on a basis) vs Prioritise (contextual importance/urgency).
- Materiality (threshold significance) vs Relevance (applies here) vs Priority (relative order) vs Attention
  (needs involvement now).
- Observe (point in time) vs Monitor (repeated) vs Detect (occurrence) vs Measure (quantify) vs Assess (significance)
  vs Diagnose (cause).
- Recommend vs Plan (intended steps) vs Orchestrate (coordinate) vs Execute (perform authorised work) vs Communicate
  (send/publish) vs Apply (put approved config into effect) vs Write (persist approved Knowledge/state).
- Route (select destination) vs Escalate (governed route to higher authority) vs Handoff (transfer with context) vs
  Delegate (accountable assignment).
- Correct (fix error) vs Supersede (replace as current) vs Revoke (withdraw validity) vs Reclassify (change handling)
  vs Remediate (corrective work) vs Recover (restore execution) vs Propagate (carry change downstream, targeted).
- Recompute (rerun derived) vs Reassess (revisit significance) vs Replan (change future work); Resume (continue a
  paused call) vs Re-enter (process new state) vs Iterate (refine because support is insufficient).
- Confidence (method support) vs Sufficiency (enough evidence). High confidence never compensates for missing evidence.
- Known unknown (recognised gap) vs Evidence gap (concrete missing support); Ambiguity (several interpretations) vs
  Conflict (sources disagree).
- Degraded (reduced but usable) vs Failed (did not complete) vs Blocked (control deliberately prevents).
- Output (what AQL produced) vs Outcome (what later happened); Learn vs Calibrate (bounded, tied to outcomes) vs
  Optimise (apply learning to behaviour; later phase).
- Promote (raise governed status) vs Share (widen availability); Freeze (preserve prior state for hindsight-safe use).
- Domain lifecycle terms (Sense, Publish, Screen, Diligence, Value, Commission, Launch, Scale, Stop) are domain
  shorthand, not generic AQL primitives. Stop is a business choice; Reject is an approval outcome; Block is a control state.
