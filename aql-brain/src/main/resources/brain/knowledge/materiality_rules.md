---
name: materiality_rules
description: First-cut method for deciding whether a change is not material, material or critical, and user-relevant, before routing or holding it
phase: 1
status: working
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-03, GV-13, GV-14
---
# Materiality rules (working)

Materiality is whether a change or finding is significant enough to alter behaviour, routing, attention or
user-facing treatment. It is a threshold assessment, not a ranking, and it is assessed after the relevant Evidence
and Intelligence change, never on a raw event. AQL owns materiality for intelligence; domains own delivery and
notification preferences. This file sets a sensible first-cut method; thresholds remain to be validated.

## Distinct concepts
- **Relevance**: does the change apply to this user, entity, task or Context at all?
- **Materiality**: is it significant enough to change treatment? (Not material, Material, Critical.)
- **Priority**: relative order among material items in a context.
- **Attention**: does someone need to be involved now? Decided in `LP-08`, not here.

## Method (`LP-03`: observe → recompute → assess → route/hold → observe)
1. **Observe**: take the triggering event (`TR-03` to `TR-08`) and the frozen prior snapshot.
2. **Normalise**: decide whether canonical Evidence actually changed (`SG-003`). If not, stop; record operational
   telemetry only.
3. **Recompute**: rerun only the affected Intelligence and Judgement (scores, verdicts, risk, cost, delivery state).
4. **Attribute**: record old value, new value, cause and lineage. Score movement is carried on the causal change.
5. **Assess materiality** against the criteria below.
6. **Assess relevance** to each subscriber or context (`user_relevance`).
7. **Route or hold**: Critical and relevant → route promptly with reason; Material and relevant → surface in the
   appropriate channel or digest; Not material or not relevant → update state silently and keep monitoring.

## First-cut criteria
A change is **material** when any of these hold for a relevant entity:
- a risk finding appears, clears, or changes severity band or applicability (`SG-006`);
- a categorical judgement changes (for example Price Verdict position, `SG-007`);
- a score or its confidence crosses a band boundary rather than moving within one (`SG-009`, `SG-010`, `SG-012`);
- a cost changes enough to alter an affordability or budget conclusion (`SG-008`, `SG-034`, `SG-035`);
- evidence sufficiency changes state (for example sufficient → insufficient) for a dimension in use (`SG-005`);
- a material conflict appears between relevant sources or documents (`SG-004`, `SG-014`);
- a deadline window is reached or missed (`SG-016`);
- a delivery item becomes blocked, stale beyond its expected window, or changes dependency state (`SG-024`).

A change is **critical** when it is material and also high consequence: it could cause financial or legal harm,
affects an imminent irreversible step (exchange, submission, publication), or invalidates an approved action or
output (reapproval needed).

## Exclusions (never material as external change)
- User edits to preferences, saved state, Process inputs or financial inputs (`SG-011`, `SG-030`, `SG-033`). They may
  recompute fit; they are not Watch alerts.
- Raw source mutations with no evidence change (`SG-001`).
- Time passing on its own (`TR-06`) unless it reaches a defined window.
- Duplicate signals for a change already surfaced.

## Guardrails
- When uncertain, hold and keep monitoring; do not interrupt on low-confidence change unless consequence is high,
  in which case route to review rather than notify as fact.
- Material risk honesty is never gated by plan.
- Operational incidents are the only direct path from raw event to notification.
- High-consequence governed knowledge changes (`SG-031`) may require a human gate before release.

## Open implementation items
- Band boundaries and numeric thresholds per score, cost and risk dimension.
- Observation windows, debounce and deduplication rules per signal.
- Per-domain consequence mapping that promotes material to critical.
- Notification and digest routing per product and user preference.
- Calibration against Watch outcomes (alert noisy, duplicate, missed) via `LP-06`.
