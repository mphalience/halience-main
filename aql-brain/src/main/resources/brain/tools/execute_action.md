---
name: execute_action
description: Executes an approved system state change or tool operation exactly as approved and returns an auditable receipt.
phase: 1
kind: brain
authority: execute
consequence: high
approval: required
reversible: false
---
## When to use
Only to carry out a proposed action whose approval is recorded, current and bound to this exact payload
and version.

## What it returns
A receipt with actor, authority, approval reference, tool, before and after state, timestamp and result
(success, failed or unknown).

## What it never does
It never runs without a recorded approval, never changes the approved payload, and never retries after an
unknown result without checking state first.
