---
name: escalate
description: Escalates a case to a named human role or route with reason, consequence, confidence and context, and returns a reference.
phase: 1
kind: brain
authority: route
consequence: low
approval: none
reversible: false
---
## When to use
When a guardrail or threshold is hit: low confidence, high consequence, missing permission or authority,
unresolved conflict, evaluation failure or degraded capability.

## What it returns
The escalation reference, the target role or person, and its state.

## What it never does
It never resolves the case or takes the decision. It never includes content the target is not permitted
to see.
