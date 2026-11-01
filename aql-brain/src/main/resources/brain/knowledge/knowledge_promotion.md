---
name: knowledge_promotion
description: How source, captured and working material becomes governed organisational Knowledge through LP-07 - statuses, eligibility, review, approval and reconciliation.
phase: 2
status: working
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-08, GV-19, GV-20, GV-21
---
# Knowledge promotion and reconciliation (LP-07)

## Purpose
`LP-07` moves permitted source or working material into organisational Knowledge without treating capture, access or
retrieval as truth. Its cycle is Capture, process, propose, review, approve, write or reconcile. Phase 1 preserves the
status and provenance interfaces; the workflow itself is phase 2 (`GV-19`).

## Knowledge statuses
- Source: originating material or system record. May be authoritative in its own system.
- Working: material in use within a Work Context; not organisational truth.
- Proposed: a specific change to Knowledge awaiting review.
- Approved / canonical: accepted for an authorised scope, with owner, effective date and review date.
- Superseded / restricted: no longer current, or narrowed by reclassification (`LP-16`).

Every Knowledge item carries the shared contract fields: content, scope, status, authority and source references,
effective date and version, owner, provenance, classification and review state (see shared_object_contracts).

## Eligibility
- Private or restricted individual material is not eligible by default (`GV-20`). A sanitised derivative may be
  proposed only where policy explicitly permits it.
- Captured recordings are governed source material; consent, notice, jurisdiction and access rules apply before
  any use (`GV-21`, see capture_and_recording).
- Where another system is authoritative, Knowledge references it rather than creating a competing copy.

## Promotion criteria (first cut, to validate)
A proposal is ready when it has: sufficient, traceable evidence; a named owner of meaning; a defined scope and
jurisdiction where relevant; an effective date and review date; a classification; and a stated relationship to
existing Knowledge (new, consistent, supersedes, conflicts).

## Review, approval and write
- Proposals state current state, proposed state, reason and evidence, expected impact, affected consumers,
  consequence and required approver.
- Approval goes through `LP-15`. It binds the exact proposed version; material change needs reapproval. Approval
  does not itself write: the write is a separate governed step that leaves an Action record.
- Conflicts are reconciled, never silently overwritten. Unresolved conflict stays visible.

## Relationship to other loops
- `LP-16` handles information already in use that later changes classification or validity.
- `LP-13` may propose validated cross-product learning for promotion.
- `SG-031` signals governed Knowledge changes to consumers.

## Open items
Approver roles per Knowledge domain; exact promotion thresholds; which capture channels are in scope; how sanitised
derivatives are produced and checked.
