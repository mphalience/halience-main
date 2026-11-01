---
name: record_intelligence
description: Records derived intelligence or a judgement with inputs, method and version, assumptions, confidence and lineage.
phase: 1
kind: brain
authority: compute
consequence: low
approval: none
reversible: true
---
## When to use
After analysis, judgement or traceability work whose result will be reused, monitored, compared later or
referenced by a recommendation or decision.

## What it returns
The intelligence or judgement ID and version, with lineage refs.

## What it never does
It never records intelligence without its evidence refs and method version, and never presents derived
results as observed facts.
