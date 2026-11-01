---
name: os_object_model
description: The canonical Halience OS objects by family, with what each is, how it relates to others and the boundary it must keep
phase: 1
status: canonical
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-18, GV-19
---
# Halience OS object model

These are product and semantic objects, not a physical data model. AQL supplies shared intelligence, context and
control contracts; Halience OS or the domain owns most objects' meaning.

Canonical work hierarchy: Function → Project → Work Type → Work Block → Task.

## Organisation
- **Organisation**: the Halience operating environment containing Functions, People, Agents and organisational
  Knowledge. A scope, not one physical system.
- **Function**: an organisational domain where work sits; contains Projects.
- **Company**: a legal or operating business Halience may evaluate, acquire, own, operate or advise. Not synonymous
  with a Venture.
- **Venture**: a venture created through the Halience venture studio, which may exist before a company is formed.

## Actors
- **Person**: a human who can perform, own, review, approve, decide or receive work. Identity, role, permission and
  authority are distinct concerns.
- **Team**: a group of People and Agents. Membership does not broaden access or authority.
- **Agent**: an AI worker acting within defined permissions, authority, tools, data scope and limits. Cannot
  self-expand any of them.

## Work
- **Project**: bounded work toward a defined outcome; instantiated from templates where appropriate.
- **Work Type**: a classification of work within a Project; not a container.
- **Work Block**: an outcome-oriented group of related Tasks; may depend on other Work Blocks.
- **Task**: the lowest executable unit; assigned to a Person or Agent.
- **Agent Run**: a bounded execution instance of an Agent; run state is distinct from the Agent and supports review,
  recovery, escalation and audit.

## Context
- **Context**: the active scope of what the user is viewing, discussing or working on. Scopes retrieval; cannot expand
  permission or authority.
- **Work Context**: the durable governed boundary binding actors, work, artefacts, evidence, decisions, permissions and
  history. Does not make restricted content visible.

## Information
- **Evidence**: sourced information with provenance, freshness and confidence. Access does not make information Evidence.
- **Knowledge**: governed information promoted or reconciled into an approved scope. Private or accessible material is
  not organisational truth by default.
- **Artefact**: a document, file, record, model or report. Preserve provenance and status; avoid competing truths
  where an authoritative system exists.

## Communication
- **Conversation**: a persistent interaction with Halie retaining history, active context and recent referents.
- **Message**: communication between People, or a Person and Halie. If operationally significant, Halie may propose a
  linked Task, blocker, Decision, Action or Commitment rather than treating the message as that object.

## Decision and action
- **Recommendation**: a proposed course of action. Not a Decision; creates no authority.
- **Decision**: a recorded choice by an authorised Person or governed process. Material Decisions preserve what was
  known, who decided, scope and time.
- **Approval**: an authorised control decision accepting an exact proposed action, change or version. Does not execute
  it; material semantic change requires reapproval.
- **Action**: a consequential operation performed or prepared by a Person, Agent or system. Capability to perform an
  Action never creates authority to perform it.

## Direction
- **Objective**: a longer-term intended outcome. Not itself Work.
- **Priority**: what matters most now in a context. Influences focus and sequencing; does not replace the underlying object.

## Operational
- **Signal**: a meaningful change, event or piece of evidence that may affect understanding or action (AQL semantics).
- **Pulse Item**: the contextual, user-facing representation of a material change worth knowing. Awareness, not involvement.
- **Attention Item**: something that needs a user's involvement now. A routing object that references, never replaces,
  the underlying Decision, Task, Approval or blocker.
- **Commitment**: a recorded obligation to do something by a condition or time; may generate Attention.
- **Issue**: a current problem needing resolution or explicit acceptance.
- **Risk**: a potential future threat; once realised it may become an Issue.
