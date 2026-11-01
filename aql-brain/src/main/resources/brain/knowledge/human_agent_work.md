---
name: human_agent_work
description: How LP-09 coordinates human and agent work through the canonical work hierarchy, Work Context, delegation, dependencies and reserved human decisions.
phase: 2
status: working
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-04, GV-05, GV-06, GV-18, GV-22
---
# Human and agent work (LP-09)

## Purpose
`LP-09` coordinates human and agent work through shared governed work state. Cycle: Plan, delegate, execute, monitor,
intervene, complete. Phase 1 is interface-ready only.

## Work hierarchy
Function, Project, Work Type, Work Block, Task (see os_object_model).
- Project: bounded work towards a defined outcome, often instantiated from a Project Template.
- Work Type: a classification, not a container.
- Work Block: outcome-oriented group of Tasks; may depend on other Work Blocks.
- Task: the lowest executable unit, assigned to a Person or Agent.
Objectives and Priorities direct work but are not work themselves. Commitments, Issues, Risks and Decisions link to
work and may generate Attention.

## Work Context
The durable governed boundary that binds actors, work, artefacts, evidence, decisions, permissions, authority and
history. Membership does not make restricted linked objects visible to everyone in it (`GV-18`).

## Delegation
- Human delegation names an owner and due condition.
- Agent delegation creates an Agent Run with explicit scope, permitted data, tools, actions, authority and budget
  (see agent_run_contract). `LP-17` owns that run's execution and recovery; `LP-09` owns the wider work graph.
- A delegation proposal is not authority. Consequential delegations go through `LP-15`.

## Reserved human decisions
Investment, transaction, stage gate, scale or stop, material company interventions, strategy and material allocation
remain with authorised humans unless explicitly delegated. Agents prepare and recommend.

## Monitoring and intervention
Track progress against Work Blocks, dependencies and commitments. Interventions include reassign, resequence,
escalate or request a human decision. Track work state, never personal activity volume (`GV-22`).

## Completion
A Work Block closes when its done condition is met, with result, artefacts and outcomes linked for `LP-06`.

## Open items
Template library, delegation approval thresholds, agent task types and budget defaults.
