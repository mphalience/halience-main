---
name: attention_digest
description: Daily digest per person of items that need their involvement but not an immediate interruption, routed by role and authority.
phase: 2
cadence: daily
behaviour: attention_triager
trigger: TR-06
loops: LP-08
scope: user
signals: SG-016, SG-024, SG-025
---
Collects held Attention candidates since the last digest: decisions, approvals, blockers, commitments, deadlines.
Hands attention_triager each candidate with its underlying object, owner, deadline and consequence.
Material means a person's action is genuinely needed on an object they own or have authority over.
Never include items about colleagues' activity, message volume or private Halie context (GV-22). Never widen who can
see an underlying object by putting it in a digest. Items already handled are dropped, not repeated.
