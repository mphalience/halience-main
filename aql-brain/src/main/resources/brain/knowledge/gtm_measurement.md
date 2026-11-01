---
name: gtm_measurement
description: Working method for measuring GTM performance and calibrating outcomes - measures, baselines, windows, attribution limits and confounders.
phase: 1
status: working
owner: GTM
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-10, GV-11, GV-16
---
# GTM measurement

Covers performance analysis (PA-029, PC-05) and outcome calibration (PA-030, PC-18) in the supervised GTM loop.

## Measures
- Exposure: reach, impressions, delivery by channel.
- Engagement: clicks, saves, shares, replies, time on content.
- Conversion: defined product outcomes such as sign-up, Explore lookup, Reveal upgrade.
- Quality and trust: claim issues raised, corrections, complaints, unsubscribes.
Each campaign or asset declares its primary measure and baseline before publishing.

## Method
1. Baseline: compare against the declared baseline or a comparable prior period or variant (PC-09).
2. Window: measure over the defined window; record when the outcome window closes (TR-10). Interim reads are
   labelled provisional.
3. Arithmetic (rates, deltas, uplift) is calculated, not estimated in prose.
4. Material performance change enters monitoring (LP-03); routine fluctuation does not.

## Attribution and calibration
- Record inputs (asset, audience, channel, timing), exposure, observed outcome and confounders such as seasonality,
  concurrent campaigns or channel changes.
- State attribution limits. Correlation within a window is not proof of cause.
- Freeze the published asset, its brief and the prediction or intent before the outcome is read; no hindsight
  rewriting.
- No feedback is not a label. Silence is neither success nor failure.
- Performance never overrides factual accuracy, independence or trust: an asset that performed well but carried a
  claim issue is recorded as a claim issue.
- Phase 1 records calibration inputs; it does not autonomously change strategy (GV-16).

## Open items
Primary measures per channel, attribution windows, baseline definitions and the threshold for material change.
