---
name: propose_action
description: Registers a proposed action with payload, target, consequence, reversibility and required authority and approval, without executing it.
phase: 1
kind: brain
authority: recommend
consequence: low
approval: none
reversible: true
---
## When to use
When a recommendation or plan step should become a governed proposed Action that can be reviewed,
approved and later executed.

## What it returns
The proposed action ID, its consequence class and the approval it requires under policy.

## What it never does
It never executes, schedules or approves the action, and the proposal grants no authority.
