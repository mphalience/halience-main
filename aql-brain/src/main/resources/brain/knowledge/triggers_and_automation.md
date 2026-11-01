---
name: triggers_and_automation
description: What starts or re-runs AQL work, TR-01 to TR-13, with initiator, prior-state and materiality requirements for each
phase: 1
status: canonical
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-06, GV-13
---
# Triggers and automation

A trigger starts evaluation. It does not by itself imply a meaningful change or an authorised action.

| Code | Trigger | Examples | Initiator | Prior state | Materiality gate | Note |
|---|---|---|---|---|---|---|
| `TR-01` | Explicit user request | Ask Settle, Halie question, Compare, affordability calculator, draft email | User | Conditional | No | Wording is intent input, not the taxonomy |
| `TR-02` | Contextual UI action | Explain this, Review, What should I ask, calculate against this Property | System, User | Yes | No | Same companion with context and intent attached |
| `TR-03` | Source/data event | planning update, price evidence, dataset refresh, rate assumption update | External event, System | Yes | Downstream | Raw event not user-relevant; recompute then assess |
| `TR-04` | Document event | upload, receipt, new version | System, User | Conditional | Downstream | Private-document boundary applies |
| `TR-05` | Process state event | offer submitted, milestone complete, tenancy state | External event, System, User | Yes | Conditional | Can trigger next-step reevaluation |
| `TR-06` | Time/schedule | expiry approaching, Watch sweep, source-health check | Automation | Conditional | Conditional | Time alone does not imply change |
| `TR-07` | Material change | risk severity changed, price position changed | AQL | Yes | Already passed | May notify, reprioritise or replan |
| `TR-08` | Evaluation/control event | eval fail, source degraded, permission revoked | AQL, System | Conditional | Operational | May force fallback or block execution |
| `TR-09` | Human approval | approve GTM publish, approve external message | User | Yes | No | An authority transition for one exact action |
| `TR-10` | Outcome event | sale completed, action succeeded, campaign converted | External event, System, User | Yes | No | Links to frozen prior output/action snapshot |
| `TR-11` | Delivery-system event | issue status change, merged code, dependency blocked | External event, System | Yes | Yes | Halie read-only in Phase 1 |
| `TR-12` | GTM workflow event | draft ready, approval rejected, metrics window complete | Automation, System, User | Yes | Conditional | Human supervision for consequential publishing |
| `TR-13` | Halience context/channel event | enter a Context, return to All Halience, open Halie history, switch to a person thread | System, User | Yes | No | Changes interaction state only |

## Automation rules
- Scheduled routines (`TR-06`) check, recompute and assess; they hand results to a behaviour and never act without
  the authority and approval that action requires.
- Event triggers enter the owning loop (usually `LP-03` for change), not a universal integration workflow.
- Where a trigger needs prior state, compare against the frozen prior snapshot, not a reconstructed one.
- Consequential execution after any trigger still needs `TR-09` approval where policy requires it.
