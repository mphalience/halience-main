---
name: settle_domain
description: Settle domain model - entities, entitlement depth, Processes and Relationships, scores, Watch, Library and Ask Settle, with the canonical Phase 1 constraints.
phase: 1
status: canonical
owner: Settle
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-01, GV-02, GV-04, GV-08, GV-14, GV-15, GV-17
---
# Settle domain

Settle is the first Halience product and the reference implementation of AQL: UK property intelligence for
people buying, selling, renting or letting a home, and for managing it afterwards.

## Entities
- Property, Area and Place are the canonical intelligence entities (CX-04). Entity resolution never unsafe-merges;
  an unresolved or ambiguous Property or Area stays explicit (PC-03).
- User and Household carry identity (CX-01). PII is kept separate from behavioural and learning data.
- Document, Report, Pack, List and Guide are retained artefacts in Save / Library (CX-10). Retaining a Property does
  not imply ownership or a Relationship.

## Entitlement depth (CX-02)
Logged-out, Explore, Reveal and Settle (plus temporary Reveal). Entitlement is a deterministic gate (GV-04): it
sets output depth, tools and persistence. When a request exceeds the entitlement, return the allowed depth and
the upgrade boundary rather than a fabricated capability.
- Explore: Property overview, material risk at overview depth, the free headline affordability calculation.
- Reveal: full breakdowns, Reports, affordability scenarios and sensitivity, personalised Settle Score.
- Settle: active personalised Processes and Relationships attached to a Property.
Material risk honesty is never paywalled (SG-006): a material risk is surfaced at Explore depth.

## Processes and Relationships
- Processes are finite and bounded: Buy, Sell, Rent, Let (CX-08). Each has phases, milestones, deadlines and
  counterparties. A generic preview is allowed without an active Property; personalised Process needs a Property and
  Settle entitlement.
- Relationships are ongoing: Owner, Renter, Landlord (CX-09). Buy leads to Owner, Rent to Renter, Let to Landlord;
  Sell ends Owner where appropriate. A Relationship has no false finite completion state.
- See settle_processes.

## Intelligence
- Pipeline: Sources, Evidence, Knowledge, Intelligence, Judgement, Personalisation, Action, Outcomes, Learning (LP-01).
- Three top-level Scores: Property Score, Area Score and Settle Score (see settle_scores). Price Verdict sits inside
  the Property Score price/value dimension (see price_verdict).
- Affordability is personal context (CX-19, see affordability_methodology). It may feed Settle Score when
  sufficient; it never changes Property Score or Area Score (GV-14).
- Missing evidence lowers confidence; it never silently lowers a score. A known unknown is a first-class state (SG-005).
- Claims keep their class: sourced/observed, derived/modelled, or interpretive/judgement (GV-02).

## Documents
User documents are evidence with provenance, not automatically canonical truth (PC-04, SG-014). Raw private documents
never become shared learning data (GV-08). See document_intelligence.

## Watch
Settle Watch alerts only on external, material, user-relevant intelligence change (PA-009). Preference, edit and
affordability-input changes may recompute but are never Watch change events (SG-011, SG-012, SG-030, SG-033).
Score movement is carried on the causal event. See watch_materiality.

## Ask Settle
One companion inside Settle (LP-02). The initiator and context change; the prompt-class taxonomy does not. It answers
from the strongest available context, respects entitlement, and hands specialist work to Settle behaviours.

## Regulated perimeter
Settle gives information and indicative calculations, not regulated mortgage, legal or financial advice (GV-15,
GV-17). Affordability outputs never imply lender eligibility, approval or guarantee. Legal and conveyancing content is
jurisdiction- and effective-date-aware (CX-14) and high-consequence knowledge changes may require a human gate
(SG-031). Final legal and regulatory wording is an open implementation item.
