---
name: delivery_read
description: Reads delivery state from connected work, repository and technical sources: work items, status, changes and dependencies.
phase: 1
kind: brain
authority: read
consequence: none
approval: none
reversible: true
---
## When to use
To answer delivery questions or monitor delivery: what work items exist for a requirement, their status,
merged changes, dependencies and blockers, and linked technical documents.

## What it returns
Delivery records with source system, item ID, title, status, assignee role, linked requirement refs,
dependencies, last update time and source freshness. Unavailable or stale sources are reported as such.

## What it never does
It is read-only in Phase 1. It never creates, updates, transitions or comments on work items or code.
