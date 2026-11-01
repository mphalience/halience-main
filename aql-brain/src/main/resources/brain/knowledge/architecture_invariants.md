---
name: architecture_invariants
description: Cross-cutting invariants AQL must preserve, including AR-01 identity and AR-02 temporal truth, and the fixed Phase 1 constraints
phase: 1
status: canonical
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-07, GV-09, GV-11, GV-18
---
# Architecture invariants

These state required behaviour. Physical design (services, stores, pipelines, APIs) is a technical decision, as
long as it preserves these invariants.

## `AR-01` Common identity and reference
Stable typed identities and references across entities, Sources, Evidence, Knowledge, Intelligence, Judgements,
Decisions, Actions, Outcomes, Learning, external artefacts and versioned outputs. References survive normal source
changes and migrations, or fail explicitly rather than silently relinking. Unresolved identity, aliases and
supersession are representable. No requirement that every object live in one store.

## `AR-02` Temporal truth and versions
Distinguish event (observed) time, captured time and effective (valid) time. Preserve object, method and version
history; support supersession, correction, revocation and reclassification; freeze the prior Evidence,
Intelligence, Judgement, Recommendation, Decision and Action state needed for hindsight-safe evaluation and audit.
Historical truth is never silently overwritten, while erasure obligations remain enforceable.

## Data, context and integration invariants
- **Source authority**: connecting or ingesting a source does not make AQL the system of record (see source_authority).
- **Meaningful datum**: where applicable, AQL can establish what a datum means, which entity it refers to, its
  provenance, time and version, permitted use and uncertainty.
- **Conflict and uncertainty**: conflicting, stale, missing or insufficient evidence stays explicit; no silent merge
  or clean fabricated answer.
- **Context boundaries**: context narrows relevance; it never expands permission or authority.
- **Connected systems**: AQL reads, writes or acts through a system only within that system's role, the actor's
  authority and governance.
- **Replaceable integration boundary**: where a connection is shared, products depend on a stable AQL contract, not
  a provider-specific response. Product-specific direct integrations remain valid where sharing adds nothing.
- **Corrections propagate**: affected downstream state is traceable so the owning capability can remediate,
  recompute or re-enter.
- **Failure is distinguishable**: source failure differs from missing evidence and from a valid zero or negative
  result; consequential tasks never silently degrade to unsafe substitutes.
- **Minimisation**: only needed, permitted information crosses boundaries.

## Fixed Phase 1 constraints
Treated as canonical, not open for rediscovery: the signal architecture; the 19 outcome families; observed vs
inferred; source and time retention; frozen prior-state snapshots; no-feedback is not a label; user edits excluded
from Watch; materiality and change lineage; the three top-level Scores (Property, Area, Settle); affordability as a
personal-fit input to Settle Score, never Property Score; recommendation and execution kept separate; failure,
uncertainty and known unknowns explicit.

## Do not turn invariants into configuration
Business rules that legitimately change may be configurable (see ownership_and_configuration). The invariants
above are not configuration and cannot be switched off by any primitive or proposal.

## Still implementation-level
Exact capture mechanisms, consent and minimisation implementation, attribution windows and materiality thresholds
per signal, source adapter inventory, evaluation thresholds, tool and authority allow-lists, affordability
methodology, rate source strategy, financial-context persistence, Settle Score affordability weighting and final
legal wording.
