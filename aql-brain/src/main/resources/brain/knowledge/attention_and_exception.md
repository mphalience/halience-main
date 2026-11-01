---
name: attention_and_exception
description: How LP-08 decides whether something needs a person to be involved now, who, and how - Attention vs Pulse vs materiality, routing and non-surveillance.
phase: 2
status: working
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-13, GV-18, GV-22
---
# Attention and exception (LP-08)

## Purpose
`LP-08` decides what materially needs involvement now and protects attention from routine status noise. Cycle:
Observe, assess, route, respond, learn. Phase 1 provides the representational primitives; intelligent prioritisation
is phase 2.

## Distinctions
- Materiality (`LP-03`) answers: did something meaningful change? Attention (`LP-08`) answers: does someone need to be
  involved now, and who? Material change does not automatically mean interruption.
- Priority is what matters most within a context. Attention is a presentation and routing object for a specific
  person. Neither replaces the underlying object.
- An Attention Item references a Decision, blocker, Commitment, review, Approval, Issue, exception or agent request
  that needs involvement. A Pulse Item is a material change worth knowing that needs no action.

## Assessment factors
- Consequence if no one acts, and when that consequence lands.
- Whether action by a person is actually required, or the item is already handled.
- Who holds the ownership and authority to act; relevance to their role and Work Context.
- Confidence in the underlying signal; low confidence may justify review rather than interruption.

## Treatments
Attention now; include in the next digest; Pulse Item for awareness; hold and continue monitoring. Escalation
respects role, context and authority.

## Non-surveillance (GV-22)
Attention routes on legitimate work state: commitments, dependencies, deadlines, decisions and outcomes. It never
turns private Halie context, message or activity volume, or behavioural telemetry into attention about a person's
performance. Private context stays private by default.

## Permission
Routing does not widen visibility (`GV-18`). If the right recipient lacks access to the underlying object, the
access question is escalated; content is not leaked into the Attention Item.

## Learning
Whether routed items were useful (acted on, dismissed, late) feeds `LP-06`. No response is not a label.

## Open items
Thresholds per item type, digest cadence and quiet-hours rules, escalation chains per role.
