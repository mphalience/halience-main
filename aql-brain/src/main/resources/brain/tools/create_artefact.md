---
name: create_artefact
description: Creates a versioned, dated artefact such as a report, pack or guide from snapshot evidence, with ownership and provenance.
phase: 1
kind: brain
authority: create
consequence: low
approval: none
reversible: true
---
## When to use
When a durable, versioned artefact is the output: a report, pack, guide or other snapshot document in
the user's or organisation's library.

## What it returns
The artefact ID, version, snapshot date, owner and location.

## What it never does
It never publishes or sends the artefact externally, and never overwrites an earlier version.
