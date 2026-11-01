---
name: interaction_state
description: Persistent Halie conversation and interaction state - history, active context, recent referents, and the separate Halie vs People/Messages channels
phase: 1
status: canonical
owner: Halie
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-18, GV-22
---
# Interaction state

Interaction state (`OUT-21`) is what lets Halie continue coherent work across turns, context changes and canvas
recomposition without the user restating the work (`PA-039`, `LP-02`). It is persistent and not user-visible as an
object. It never expands access or action authority.

## What it holds
- **Conversation identity and history** (`CX-11`): the conversation id, prior turns, tool results summarised into the
  answer, and the explicit goal.
- **Active Halience context** (`CX-20`): the active venture, object or surface, any scoped Context (e.g. "Settle"),
  and the navigation origin.
- **Recent referents** (`CX-20`): the objects recently discussed or opened, newest first, each with a typed ref, a
  label and when it was referenced. "It", "that requirement" or "the second one" are resolved against these.
- **Channel** (`CX-21`): whether the active interaction is with Halie or is a People/Message thread.
- **Permission scope and updated time**.

## Rules
- **Referent resolution** (`PC-03`): resolve a reference against the recent referents and the active context. If two
  or more referents plausibly match, ask; never guess silently. Unresolved ambiguity stays explicit.
- **No silent import**: never pull unrelated context from another conversation or scope. If earlier work is needed,
  name it and retrieve it explicitly (Context & Inputs failure behaviour: "Ask rather than silently import").
- **Only authorised context** may be retained or resolved. If the user loses access to an object, it drops out of
  the referents.
- **Channels stay distinct**: a Halie conversation and a People/Message thread are different channels. Human messages
  are never merged into a Halie conversation, and Halie never posts into a person thread as if it were the user.
  Switching channel (`SG-037`, `TR-13`) changes the channel state; it grants no data or action authority.
- **A message stays communication**: if a message is operationally significant, Halie may propose a linked Task,
  blocker, Decision, Action or commitment rather than treating the message as that object.
- **Private by default** (`GV-22`): a person's Halie conversation is private to them. It is never management
  reporting, and it reaches broader learning only through permitted, sanitised signals (`GV-20`).

## Signals
- `SG-036` Active context / referent change: observed, no materiality gate, consumed by composition and Halie.
- `SG-037` Conversation/channel state change: observed, no materiality gate.
