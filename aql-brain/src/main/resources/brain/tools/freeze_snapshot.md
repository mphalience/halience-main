---
name: freeze_snapshot
description: Freezes an immutable, versioned snapshot of current evidence, intelligence or state for later comparison, audit or outcome evaluation.
phase: 1
kind: brain
authority: compute
consequence: low
approval: none
reversible: false
---
## When to use
Before a consequential output, decision or action is relied on, and whenever monitoring needs a prior
state to compare against later. Snapshots make hindsight-safe evaluation possible.

## What it returns
The snapshot ID, scope, captured time and the refs it contains.

## What it never does
A snapshot is never edited after it is frozen. It never copies data the caller may not retain; it records
references where the source must remain authoritative.
