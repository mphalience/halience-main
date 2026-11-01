---
name: outcome_window_check
description: Daily check for outcome windows that have closed or outcome events that have arrived, linking them to frozen prior outputs.
phase: 1
cadence: daily
behaviour: outcome_recorder
trigger: TR-06
loops: LP-06
scope: system
signals: SG-019, SG-020, SG-032
---
Checks predictions, recommendations and actions whose observation window has closed, and new outcome events.
Hands outcome_recorder the frozen prior snapshot and any observed result with its source and time.
Material means an observed outcome that can be linked to a prior output with known attribution limits.
No feedback is not a label: a closed window without an observed outcome is recorded as unobserved, not as success or
failure. Never recompute the prior output in hindsight. Learning writes stay purpose-bound and privacy-safe.
