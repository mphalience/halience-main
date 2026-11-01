---
name: cross_product_learning
description: How LP-13 turns permitted local learning into validated, privacy-safe shared learning across products and companies without leaking restricted context.
phase: 2
status: working
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-08, GV-16, GV-19, GV-20
---
# Cross-product learning (LP-13)

## Purpose
`LP-13` turns permitted local outcomes and patterns into reusable portfolio learning without leaking private, product
or company context. Cycle: Learn locally, sanitise, compare, validate, share, observe. It is distinct from local
outcome calibration (`LP-06`).

## The Learning object
A validated lesson or calibration signal derived from outcomes, corrections or repeated governed patterns for a
permitted scope. Fields include source outcome or evaluation references, lesson, applicable scope, support and
confidence, method version, privacy and use constraints, permitted reuse level, status and review state. Learning is
not raw behaviour telemetry and not an automatic model change.

## Chain CH-10
`LP-06` supplies permitted local learning; `LP-13` sanitises and validates cross-context patterns; `LP-05` controls
quality; `LP-07` governs promotion where shared organisational Knowledge is warranted.
- Insufficient evidence: retain locally.
- Privacy or policy boundary fails: do not share.
- Validation fails: hold or reject.
- Promotion not required: retain as a governed learning artefact.

## Boundaries
- Only approved privacy-safe signals cross product or company boundaries (`GV-20`). Raw private documents, personal
  data and company-confidential figures do not.
- Shared learning does not expose the restricted source material behind it.
- Learning does not automatically change production behaviour (`GV-16`); that is a separately governed decision.
- Engagement or activity volume never stands in for outcome.

## Validation
Support from multiple independent contexts, consistency, effect size where measurable, known confounders, and an
explicit statement of where the pattern should not be applied. Validation is independent of the producer.

## Open items
Sanitisation and anonymisation methods, minimum support thresholds, reuse levels, who approves sharing.
