---
name: record_evaluation
description: Records an evaluation result with criteria and version, scores, defects, evaluator model and independence state.
phase: 1
kind: brain
authority: evaluate
consequence: low
approval: none
reversible: true
---
## When to use
After evaluating an output, capability or source against explicit criteria.

## What it returns
The evaluation ID, linked to the evaluated output and its versions, and any control state it triggers.

## What it never does
It never records an evaluation without its criteria version, and never overwrites an earlier result.
