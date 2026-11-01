---
name: score
description: Computes a transparent score or ranking using a governed, versioned methodology and returns the breakdown and confidence.
phase: 1
kind: brain
authority: compute
consequence: none
approval: none
reversible: true
---
## When to use
When a judgement or ranking uses a defined scoring methodology with dimensions and weights.

## What it returns
The score or ordering, per-dimension contributions, methodology and version, confidence, and the
dimensions that could not be scored for lack of evidence.

## What it never does
It never treats missing evidence as a negative value, and never applies personal preferences to a score
whose methodology excludes them.
