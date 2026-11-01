---
name: decision_lineage
description: How LP-10 preserves the chain from evidence and judgement through a Decision to Action, Outcome and Learning, with frozen decision-time state.
phase: 2
status: working
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-02, GV-10, GV-11
---
# Decision lineage (LP-10)

## Purpose
`LP-10` preserves the chain Evidence, judgement, decision, action, outcome, learning. Decision is not an `LP-01`
stage; it is a governed object and lineage overlay across `LP-01` states. Underlying contracts begin in phase 1;
richer organisational decision management is phase 2.

## The Decision object
An explicit choice made by an authorised actor or governed process at a point in time. Minimum fields: decision;
decision-maker; authority basis; snapshot references to Evidence, Intelligence, Judgement and Recommendation;
material alternatives; decision time; rationale; status; conditions; version.

## Distinctions
- Recommendation is not Decision. A Recommendation creates no authority.
- Decision is not Approval. A Decision is a choice; an Approval authorises an exact action or version. Both may occur
  and are recorded separately.
- Decision is not Action. Execution is separately attributable; a good decision can have a poor outcome and a poor
  execution can spoil a good decision.

## Decision-time state
What was known and judged at decision time is frozen and immutable as historical basis. Later recomputation, new
evidence or correction does not edit it. A change of mind is a new Decision that supersedes the old one. Where
information used was later reclassified or corrected, the record is marked as such by `LP-16`, not rewritten.

## Linking outcomes
Actions taken under a Decision link to it. Outcomes link to the Action and Decision, with attribution limits.
`LP-06` evaluates effectiveness; `LP-10` preserves the lineage. Outcome records what happened, not whether the
decision was reasonable given what was knowable.

## Rationale
Store the decision-maker's inspectable rationale, not hidden model reasoning.

## Open items
Which decisions count as material in each domain, and how decision-time references are frozen without duplicating
all underlying data (CTO design question).
