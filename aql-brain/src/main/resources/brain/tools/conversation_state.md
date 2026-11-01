---
name: conversation_state
description: Reads and updates Halie interaction state: conversation history, recent referents, active Context and channel identity.
phase: 1
kind: brain
authority: compute
consequence: low
approval: none
reversible: true
---
## When to use
At the start of a companion turn to load continuity, and at the end to persist it. Also used by the
channel resolver and canvas composer to read or set the active Context and channel.

## What it returns
For a read: conversation ID, channel, recent turns and tool results, recent referents with refs, active
Context, navigation origin, permission scope and updated time. For a write: the stored state and its
version.

## What it never does
It never merges a People/Message thread into a Halie conversation, never retains context the user is not
authorised to hold, and never grants data or action authority.
