---
name: save_draft
description: Saves an unsent draft, such as a message, document or content asset, with its sources, audience and approval state.
phase: 1
kind: brain
authority: draft
consequence: low
approval: none
reversible: true
---
## When to use
After drafting content that someone will review, edit, approve or send later.

## What it returns
The draft ID and version, and its approval state (unsent, awaiting review).

## What it never does
It never sends, publishes or shares the draft.
