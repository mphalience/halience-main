---
name: halience_os_canvas
description: The dynamic Halience OS canvas - Attention, Work, Pulse and Team regions, what each holds, how composition and recomposition work in Phase 1
phase: 1
status: canonical
owner: Halie
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-04, GV-18, GV-22
---
# Halience OS canvas

The canvas is the composable surface a Halience OS user sees. It is built from authorised outputs and explicit
Context using reusable regions (`PA-040`, `OUT-20`). Phase 1 establishes the regions and explicit recomposition;
intelligent role-, priority- and materiality-driven composition is Phase 2 (Coverage Matrix, "Dynamic Halience OS
canvas foundation").

## Regions
- **Attention**: things that currently need the user's involvement: a decision, blocker, commitment, review,
  approval, exception or agent request. An Attention Item is a presentation and routing object that references the
  underlying object (Decision, Task, Approval, Issue...). It never replaces it. Attention means "needs involvement
  now", which is distinct from importance or notification (`LP-08` is Phase 2; Phase 1 provides the representation).
- **Work**: the user's work in the canonical hierarchy Function → Project → Work Type → Work Block → Task, plus
  artefacts in the active Work Context.
- **Pulse**: material business change, exceptions or trends worth knowing in the current context. A Pulse Item is the
  contextual, user-facing representation of a material Signal. Pulse is for awareness, not involvement. Raw source
  mutations never appear in Pulse (`GV-13`).
- **Team**: the people and agents working in the current context, their commitments and dependencies. It shows
  legitimate work, commitment, dependency and outcome state, never activity volume or private Halie context (`GV-22`).

## Composition rules
- **Inputs**: active Context (`CX-20`), interaction state (`OUT-21`), authorised outputs and artefact context
  (`CX-22`), and the user's permissions.
- **Permission first**: every item is checked for permission before it is placed. Context narrows relevance but never
  expands permission (`GV-18`). An item the user may not see is left out silently. It is not shown as redacted unless
  the product needs to show that something exists.
- **Recomposition triggers**: an explicit context entry or exit, an object/surface navigation, or a request for a
  different representation (`SG-038`, `TR-13`). In Phase 1 the canvas does not recompose itself on inferred priority.
- **Runtime only**: the composition (`OUT-20`) is runtime state. The items it references stay owned by their source
  systems.
- **Provenance**: each placed item carries its source/output ref, canonical or working status, and a composition
  reason, so the user can open the authoritative source.
- **Navigation**: each item may offer bounded actions (Open, Review, Ask Halie, approve, navigate) through action
  routing (`PA-043`, `OUT-22`). Offering an action never implies authority to execute it.

## Contexts
"All Halience" is the organisation-wide view. Entering a scoped Context (a venture such as Settle, a company, a
project) recomposes the regions for that scope. Returning to All Halience restores the organisation view.
