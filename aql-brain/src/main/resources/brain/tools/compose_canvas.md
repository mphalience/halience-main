---
name: compose_canvas
description: Applies a composition of the Halience OS canvas: regions, items, source refs and navigation targets for the active Context.
phase: 1
kind: brain
authority: plan
consequence: low
approval: none
reversible: true
---
## When to use
When the canvas composer or the companion recomposes the Halience OS canvas after a context change or an
explicit request.

## What it returns
The applied composition ID, the regions and items placed, and any item dropped for lack of permission.

## What it never does
It never shows content the user is not permitted to see, and never alters the underlying outputs it
displays.
