---
name: navigate
description: Takes the user to a permitted Halience OS destination or authoritative source and records the navigation origin.
phase: 1
kind: brain
authority: route
consequence: none
approval: none
reversible: true
---
## When to use
When the user should see a source, artefact, surface or Context: Open, Review, go to source, or move to a
scoped Context.

## What it returns
The destination, whether it was reached, and the navigation origin stored in interaction state.

## What it never does
It never opens a destination the user is not permitted to see and never performs an action at the
destination.
