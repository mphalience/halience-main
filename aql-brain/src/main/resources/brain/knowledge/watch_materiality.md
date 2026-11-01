---
name: watch_materiality
description: When Settle Watch alerts - the signal layers, external-change rule, user-edit exclusion, score-as-consequence and the material change contract.
phase: 1
status: canonical
owner: Settle
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-01, GV-03, GV-13, GV-14
---
# Watch materiality

Settle Watch monitors Properties and Areas a user watches and alerts only on external, material, user-relevant
intelligence change (PA-009, LP-03). Materiality mechanics are AQL-owned; thresholds per signal are implementation
decisions.

## Signal layers
Raw source event (SG-001) -> normalised evidence change (SG-003) -> derived intelligence change (SG-006 to SG-008)
-> material user-relevant signal (OUT-12) -> trigger (TR-07) -> observed outcome. Each step is a gate.

## Rules
1. Never notify on raw source mutations. A source update is not a change in intelligence.
2. Recompute first, then compare current intelligence with the frozen prior snapshot.
3. Material means the change would plausibly alter what this user understands or does about the Property or Area:
   a new or changed material risk, a price/value change (asking price, valuation, Price Verdict band), a material
   cost change, or a change in confidence or applicability that matters.
4. User relevance is assessed against what the user watches and the Process or Relationship they are in.
5. User edits are excluded. Preference and must-have changes (SG-011), user preference or edit events (SG-030) and
   affordability input changes (SG-033) may recompute fit and affordability but are never Watch change events.
   A Settle Score change alerts only when an external intelligence change caused it (SG-012).
6. Score movement is a consequence, not a signal. Property, Area and Settle Score movement (SG-009, SG-010, SG-012)
   is carried on the causal material-change event, never emitted on its own.
7. Governed rate or assumption updates (TR-03) may recompute affordability but need materiality and user relevance
   before any notification.
8. Source health problems (SG-002) affect confidence and fallback; they are operational, not user alerts.
9. Suppress noise: deduplicate, and hold a change that is not material while monitoring continues.

## Material change contract (OUT-12)
Each signal carries: old and new value refs, cause ref (the source or evidence change), lineage refs, materiality
(Not material, Material, Critical), user relevance, confidence, and attribution of any score movement. The prior
snapshot ref is retained so later evaluation can judge whether the alert was right (Watch outcome family).

## Evaluation
Watch outcomes record alerts issued, changes confirmed, noisy or duplicate alerts and missed material changes,
to calibrate thresholds without hindsight.

## Open items
Per-signal thresholds, alert frequency limits and the user-relevance rules per Process.
