---
name: artefact_open
description: Opens an authorised artefact by reference and returns its content with provenance, version, owner and canonical or working status.
phase: 1
kind: brain
authority: read
consequence: none
approval: none
reversible: true
---
## When to use
To read a specific artefact: a requirement, decision record, document, report, pack or other
organisational or product artefact, once its reference is known.

## What it returns
The artefact content (or the requested section) with source system, location, version, owner, last
change, canonical or working status, and any superseding reference. If the caller lacks access, it returns
an unavailable state without content.

## What it never does
It never edits, copies into a competing store, or shares the artefact, and never bypasses the
authoritative source's permissions.
