---
name: reclassification_remediation
description: How LP-16 propagates redaction, reclassification, correction or recall through already-used information with targeted remediation and preserved auditability.
phase: 2
status: working
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-08, GV-09, GV-10, GV-11, GV-19
---
# Information reclassification and remediation (LP-16)

## Purpose
`LP-16` propagates governed changes to sensitivity, access or validity through affected downstream state without
losing auditability. Cycle: Reclassify, trace impact, remediate, propagate, verify. It handles information already
in use that changes state; `LP-07` handles promotion into organisational Knowledge.

## Triggers
Redaction, reclassification, correction, supersession, revocation, recall or access-policy change (`TR-03`, `TR-08`).

## Impact tracing
Uses the common identity and reference architecture and temporal version history to find every Evidence, Knowledge,
Intelligence, Judgement, artefact, approved output, Decision and Action that referenced the changed item.

## Remediation classes
- No downstream use: update state only.
- Recompute: hand corrected state to the owning loop to recompute or resume.
- Restrict or redact: without disclosing the restricted content.
- Supersede: mark prior outputs as superseded.
- Reapprove: an approved output or action relied on it (`LP-15`).
- Reconcile Knowledge: organisational Knowledge affected (`LP-07`).

## Principles (chain CH-12)
- Targeted resume, not cascade: `LP-16` identifies impact and hands state to the owning loop; it does not rerun every
  loop blindly.
- History is not rewritten. Decision-time records are marked as relying on information later changed.
- Remediation never reveals restricted content to unauthorised actors.
- Lawful erasure and minimisation obligations still apply (`GV-09`); where history is minimised, that is recorded.
- Untraceable impact is a known unknown and is escalated.

## Verification
Each remediation item closes only when the owning loop confirms the result. Open items remain visible.

## Open items
Lineage depth required in phase 2, notification rules for affected owners, erasure versus audit precedence by data
class.
