---
name: primitive_propose
description: Submits an exact proposed edit to an AQL primitive, with current and proposed text, reason, impact, risk and required approval.
phase: 1
kind: brain
authority: recommend
consequence: low
approval: none
reversible: true
---
## When to use
When primitive_editor has a concrete change for a Halience admin to review: a new or changed behaviour,
skill, knowledge item, journey, routine or tool.

## What it returns
The proposal ID, the validation result (front matter, names, catalogue references, authority limits) and
the approval route.

## What it never does
It never applies or approves the change. A human admin approves; the runtime applies it through the one
door with an Action record and a new revision that can be rolled back. The runtime refuses it for anyone
who is not a Halience staff admin.
