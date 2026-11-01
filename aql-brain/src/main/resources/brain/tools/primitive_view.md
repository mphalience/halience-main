---
name: primitive_view
description: Shows the current text, revision and references of an AQL primitive for an authorised Halience admin.
phase: 1
kind: brain
authority: read
consequence: none
approval: none
reversible: true
---
## When to use
When a Halience staff admin, through primitive_editor, needs to see how a behaviour, skill, knowledge item,
journey, routine or tool is currently defined, before proposing a change.

## What it returns
The primitive's full current text, type, name, revision, last change and its Action record, plus the
primitives that reference it or that it references. Visibility follows permission and sensitivity.

## What it never does
It never edits a primitive. The runtime refuses it for anyone who is not a Halience staff admin.
