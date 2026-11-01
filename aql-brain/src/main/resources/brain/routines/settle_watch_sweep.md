---
name: settle_watch_sweep
description: Daily Settle Watch sweep comparing watched Properties and Areas with frozen prior state to find material, attributable changes.
phase: 1
cadence: daily
behaviour: watch_change_detector
trigger: TR-06
loops: LP-03
scope: product
signals: SG-001, SG-003, SG-006, SG-007, SG-008, SG-009, SG-010, SG-012
---
Checks every watched Property and Area for source and evidence changes since the last frozen snapshot.
Hands watch_change_detector the watch list, the prior snapshot and the new evidence for each subject.
Material means a change in risk, price position, cost or score that passes watch_materiality rules, with its cause
attributed to evidence. Score movement is carried on the causal change, not reported as a separate signal.
Never notify on raw source mutations. Never treat a user's own preference or Process edits as a Watch event (SG-030).
The schedule firing does not itself mean anything changed; an empty sweep is a valid result.
