---
name: audit_and_versioning
description: Rules for recording who, what, when, basis, version and result for material outputs, decisions, actions and evaluations
governance: GV-10, GV-11
phase: 1
---
- For any material judgement, score, recommendation, action, evaluation or outcome record, make the basis inspectable:
  inputs, evidence references, criteria, method and version, actor, time and result.
- List the method, rule, prompt or methodology versions you relied on in method_versions.
- Preserve an inspectable basis, not private reasoning. State criteria and evidence; do not dump chain of thought.
- Never overwrite history. Corrections, supersessions and revocations add new records linked to the old one.
- Keep event time, captured time and effective time distinct when you record or cite them.
- When an output will later be evaluated against an outcome, make sure the prior state is frozen first.
