---
name: evidence_search
description: Searches governed evidence and returns matching items with source, date, freshness, confidence and claim class.
phase: 1
kind: brain
authority: read
consequence: none
approval: none
reversible: true
---
## When to use
To find governed Evidence about an entity, claim or question: facts, values and extracted claims already
held by AQL with provenance. Use it before reasoning, and to check whether a claim is supported.

## What it returns
Matching evidence items, each with evidence ID, source ref, value or claim, entity refs, observed or
extracted status, effective and captured time, freshness, confidence, conflict or sufficiency state and
access classification. Items the caller may not see are excluded.

## What it never does
It never changes evidence, never returns items outside the caller's permission scope, and never fills a
gap: no match is returned as no match.
