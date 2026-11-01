---
name: context_model
description: The context AQL uses to scope a task, CX-01 to CX-22, with source of truth, permission boundary and failure behaviour for each
phase: 1
status: canonical
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-04, GV-18
---
# Context model

Context scopes interpretation, retrieval, reasoning, Personalisation and Action. It is an input, not an `LP-01`
stage. Context may narrow relevance and permitted behaviour but never expands Permission or Authority. Missing,
ambiguous or unauthorised context stays explicit.

| Code | Context | Examples | Boundary | Failure behaviour |
|---|---|---|---|---|
| `CX-01` | User/Household identity | account, member, household role | PII separated from behavioural/learning data | Anonymous or unknown stays explicit |
| `CX-02` | Plan and capability entitlement | logged-out, Explore, Reveal, Settle | Deterministic gate | Return allowed depth / upgrade boundary, never fabricated capability |
| `CX-03` | Current surface/object | Property Overview, Guide, document item, Halience object | Only accessible current context | Ask or resolve when ambiguous |
| `CX-04` | Canonical entity identity | Property, Area, Place, Document, Report, Pack, List | No unsafe merge | Explicit unresolved/ambiguous entity |
| `CX-05` | Evidence/provenance state | value, source, date, freshness, confidence, claim class | Source, licence and privacy aware | Known unknown / insufficient evidence |
| `CX-06` | Current derived intelligence | scores, verdicts, findings, comparisons | Versioned methodology | Use current valid version or degrade explicitly |
| `CX-07` | Priorities/preferences/baseline | must-haves, weighted nice-to-haves, reference property, affordability context | Explicit user-controlled inputs | Never fabricate personalised fit |
| `CX-08` | Bounded Process state | Buy/Sell/Rent/Let phase, milestone, counterparties | Property + entitlement for an active Process | Generic preview only if no active Property |
| `CX-09` | Ongoing Relationship state | Owner/Renter/Landlord | Settle-owned, Property attached | No false finite completion |
| `CX-10` | Save/Library | saved Properties, Lists, Documents, Reports, Packs, Guides | Capability-specific persistence | Never infer ownership or relationship from retention |
| `CX-11` | Conversation/task history | prior turns, tool results, explicit goal | Scoped to permissions/context | Ask rather than silently import unrelated context |
| `CX-12` | Action/approval state | proposed action, actor, authority, approval, tool, result | Authority + audit required | Stop or escalate if approval/authority absent |
| `CX-13` | Temporal context | current date, deadline, effective date, snapshot | Timezone and effective-date aware | Never apply future or expired rule as current |
| `CX-14` | Jurisdiction | nation, local authority, legal effective date | Regulated perimeter | State uncertainty; no fabricated universal rules |
| `CX-15` | Persona/companion configuration | voice, behaviour, allowed tools | Never overrides truth or safety | Fall back to neutral compliant response |
| `CX-16` | Requirements/delivery | canonical docs, issue tracker, repositories, dependencies | Permission-aware read/explain/compare/navigate in Phase 1 | Expose stale or missing source |
| `CX-17` | Campaign/content | brief, audience, channel, approved claims, asset history | Human approval to publish | Never publish without approval |
| `CX-18` | Evaluation/control | criteria, eval version, model/rule version, incidents | Auditable | Fail closed or escalate by consequence |
| `CX-19` | Affordability/financial | income, deposit, commitments, price, term/rate assumptions, LTV | Sensitive, purpose-bound; temporary input distinct from persisted context; never infer lender approval | Indicative result with assumptions when calculable, else request inputs; never fabricate |
| `CX-20` | Active Halience context and referents | active venture/object/surface, recent referents, navigation origin | Only authorised context retained/resolved | Ask or resolve explicitly |
| `CX-21` | Conversation/channel state | Halie conversation, People/Message thread | Channel grants no extra data or action authority | Keep channels distinct; never merge human messages into Halie |
| `CX-22` | Organisational artefact | canonical/working status, source, version, owner, provenance | Permission-aware; source remains authoritative | Expose unavailable/stale/unauthorised rather than create a competing copy |

## Context assembly
Use the permitted context the task needs: actor and role, Work Context, entity, task or object, conversation
referents, time, permissions and authority, sensitivity, jurisdiction and applicable preferences. Retrieval and
context resolution check access scope before any model or tool use; action routing checks authority separately.

## Persistence
Context is mixed: ephemeral task and conversation context plus durable references where continuity requires.
Work Context is durable. Restricted information stays restricted even when linked to the same Work Context.
