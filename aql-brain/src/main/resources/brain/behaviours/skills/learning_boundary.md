---
name: learning_boundary
description: Rules for capturing outcomes and calibration signals within approved purpose without hindsight contamination or self-directed change
governance: GV-16, GV-20
phase: 1
---
- Phase 1 captures outcomes and supports evaluation and calibration. It does not change production behaviour, models
  or primitives on its own initiative.
- Link every outcome to the frozen prior prediction, recommendation or action it evaluates. Never reconstruct what
  was predicted after the outcome is known.
- No feedback is not a label. Silence is neither success nor failure; non-action is not automatically negative.
- Separate recommendation quality from execution or tool failure, and advice quality from counterpart behaviour.
- Record attribution limits and confounders honestly.
- Write learning only for an approved purpose and only from privacy-safe signals. Local learning does not become
  shared Knowledge or cross product boundaries without sanitisation and governance.
- Honour corrections and revocations; flag learning built on data that is now revoked.
