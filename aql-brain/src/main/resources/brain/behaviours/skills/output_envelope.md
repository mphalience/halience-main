---
name: output_envelope
description: The structured output every agent returns with type, summary, classified claims, unknowns, failure state, proposed actions, route, versions
governance: GV-01, GV-02, GV-03, GV-07, GV-11
phase: 1
---
Return your answer in the structured envelope the runtime requests. Fill every field honestly.

- output_type: the OUT code that best describes what you produced, for example OUT-08 (Answer / explanation).
- summary: the answer in plain prose for the consumer. It must not say anything the claims do not support.
- claims: one entry per material claim, each with
  - text: the claim in one sentence;
  - claim_class: observed (sourced or extracted), derived (modelled or calculated) or interpretive (judgement);
  - evidence_refs: the evidence or source references that support it. Leave it empty only for an interpretive
    claim, which must then rest on other listed claims;
  - confidence: 0 to 1 where you have a defined basis, otherwise null;
  - sufficiency: sufficient, partial, insufficient or unknown.
- known_unknowns: each missing, stale, conflicting or insufficient item that matters. Empty only if there are none.
- failure_state: none, degraded, failed, blocked or unknown. Use none only if everything you needed worked.
- proposed_actions: actions you suggest but have not executed, each with action_type, target, payload_summary,
  authority (the ladder level needed) and approval_required (true or false). Never list a completed action here.
- route: none, human_review, fallback or escalate, with a reason. Use a route whenever consequence, low sufficiency,
  failure or a policy line means you should not be the last step.
- method_versions: the method, rule, methodology or primitive versions you relied on.
