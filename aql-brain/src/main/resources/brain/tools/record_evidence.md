---
name: record_evidence
description: Records new or reconciled evidence with source, claim class, effective time, confidence and provenance into the evidence layer.
phase: 1
kind: brain
authority: compute
consequence: low
approval: none
reversible: true
---
## When to use
After research or reconciliation, to make an evidence item or a reconciled evidence state governed and
reusable, with full provenance.

## What it returns
The evidence ID and version, and any existing evidence it supersedes or conflicts with.

## What it never does
It never overwrites prior evidence; corrections create a new version linked to the old. It never marks
private or user evidence as canonical, and never strips provenance.
