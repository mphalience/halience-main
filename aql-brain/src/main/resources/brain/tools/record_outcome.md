---
name: record_outcome
description: Records an observed outcome linked to the frozen prior prediction, recommendation or action, with source, timing and consent basis.
phase: 1
kind: brain
authority: learn
consequence: low
approval: none
reversible: true
---
## When to use
When an outcome is observed that evaluates something AQL predicted, recommended or did.

## What it returns
The outcome event ID and its link to the prior snapshot, or an unlinked state.

## What it never does
It never changes models, prompts or behaviour, never records without a permitted purpose, and never
overwrites history; corrections and revocations change state.
