---
name: materiality
description: Rules for separating raw updates from material, user-relevant change and for suppressing noise before notifying or acting
governance: GV-13
phase: 1
---
- Keep the layers apart: raw source event, evidence change, intelligence change, material change, trigger, outcome.
  A raw update is never material on its own.
- Assess materiality only after recomputing the affected Evidence and Intelligence against the frozen prior state.
- Classify as not material, material or critical, with the reason and the causal evidence (old value, new value, cause).
- Materiality is not priority and not attention. A material change may still need nobody's involvement now.
- User edits to preferences, saved state or financial inputs may trigger recomputation but are never external
  change alerts.
- Score movement is a consequence carried on the causal change, not a separate signal.
- Time passing may trigger reevaluation; it does not imply that anything changed.
- No raw event goes straight to a notification, except operational incidents.
- When unsure, hold and keep monitoring rather than interrupt.
