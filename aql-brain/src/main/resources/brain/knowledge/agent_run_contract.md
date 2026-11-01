---
name: agent_run_contract
description: The Agent Run shared contract and LP-17 execution and recovery lifecycle - scope, budget, state, handoff, failure classes and the no self-expansion rule.
phase: 2
status: working
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-04, GV-05, GV-06, GV-07, GV-10, GV-18
---
# Agent Run contract and execution (LP-17)

## What an Agent Run is
A bounded execution instance in which an Agent performs delegated work under defined scope and controls. Run state
is distinct from the Agent itself. An Agent Run is an execution object, not a source of authority, and not the wider
human and agent work graph (`LP-09`).

## Minimum contract
run_id; initiator; objective; Work Context; permitted data; permitted tools and actions; authority; resource and time
budget; dependencies; state; outputs; exception or failure; handoff or escalation; outcome or result; audit and
version references.

## Lifecycle
Scope, execute, observe, validate or hand off, recover or escalate, close.
- Scope: set by an authorised delegation. Nothing runs without it.
- Execute: only permitted tools and actions, on permitted data, within budget.
- Observe: state and receipts are visible for review.
- Validate / hand off: outputs are checked, independently where consequential, before they are relied on.
- Recover / escalate: retry within remaining budget, hand to a human, or close as failed.
- Close: result, receipts and open items recorded.

## Non-negotiable rules
- An Agent cannot self-expand its permissions, authority, tools, scope or budget. Requests for more are proposals
  decided by an authorised human.
- Capability to perform an action never creates authority to perform it.
- Approval covers the exact action and version approved; a changed payload needs reapproval. Approval does not
  execute anything.
- Material exceptions route to an authorised human or agent.
- Partial or failed runs are never reported as complete (`GV-07`).

## Failure classes
Tool or integration failure; missing or invalid input; authority or approval absent; budget or time exhausted;
ambiguous objective; bound breach. Each has a defined route: retry, handoff, escalate or close.

## Open items
Retry and idempotency rules for external actions, partial-completion semantics, resumability, budget units.
