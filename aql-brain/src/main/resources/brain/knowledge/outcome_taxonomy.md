---
name: outcome_taxonomy
description: The canonical 19 Phase 1 outcome families with representative events, sources, learning use and the boundary each must keep
phase: 1
status: canonical
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-16, GV-20
---
# Outcome taxonomy

Outcomes connect prior intelligence, recommendations and actions to later results. Every outcome event (`SG-032`)
belongs to exactly one family, records its source, and links to the frozen prior state it evaluates.

Observed sources: User-confirmed, Public record, System event, Professional document, AQL output, Action record,
Analytics, GTM workflow, Inference.

| # | Family | Representative events | Learning use | Boundary |
|---|---|---|---|---|
| 1 | Listing | listed, withdrawn, asking-price change, time-on-market milestone | Price/market calibration; Watch evaluation | A listing change is evidence, not automatically material |
| 2 | Valuation | AQL valuation snapshot, professional valuation, later sale price | Valuation calibration | Freeze model and evidence snapshot first |
| 3 | Offer | drafted, submitted, accepted, rejected, revised, withdrawn | Recommendation/process evaluation | Never infer acceptance from silence |
| 4 | Transaction | progressed, stalled, failed, completed | Process-state analysis | Internal analytical family, not user-facing journey ontology |
| 5 | Survey / condition | issue found, severity, condition later confirmed | Risk/condition calibration | Professional document is evidence, not universal truth |
| 6 | Negotiation | reduction requested/agreed, concession, failed | Recommendation effectiveness | Separate advice quality from counterpart behaviour |
| 7 | Legal / conveyancing | search or title issue, enquiry raised/resolved, exchange, completion | Process guidance evaluation | High-consequence content stays governed and human-gated |
| 8 | Mortgage / finance | affordability snapshot, AIP, application, offer, declined, assumption change | Affordability and scenario calibration | Minimised, purpose-bound; indicative affordability is not a lender decision |
| 9 | Rental | application, offer, tenancy start/end, deposit, renewal | Rent Process evaluation | Rent Process transitions to Renter Relationship |
| 10 | Ownership | completion, maintenance/insurance/renewal outcome, ownership ends | Long-term owner learning | Ongoing Relationship, not a finite Transaction |
| 11 | Seller | listing readiness, Sales Pack completion, sale outcome | Sell Process evaluation | Keep seller disclosure/publication boundary explicit |
| 12 | Risk | risk identified, severity changed, later confirmed or cleared | Risk and materiality calibration | Material risk honesty is never gated by payment |
| 13 | Recommendation | accepted, dismissed, acted, result observed | Recommendation quality | Non-action is not automatically negative |
| 14 | Process/Relationship | Buy→Owner, Rent→Renter, Let→Landlord, Sell ends Owner, stage outcomes | Lifecycle and next-action calibration | Canonical product ontology, distinct from Transaction |
| 15 | Watch | alert issued, change confirmed, alert noisy/duplicate, change missed | Materiality and noise evaluation | User preference edits excluded from external-change events |
| 16 | Document intelligence | classification right/wrong, conflict resolved, finding confirmed | Document-intelligence evaluation | Raw private documents never communal learning by default |
| 17 | Score | Property/Area/Settle Score snapshot, later evidence, confidence calibration | Score calibration | Movement is a consequence; affordability never changes Property or Area Score |
| 18 | User feedback | helpful/not helpful, correction, challenge, reported error | Quality/evaluation signal | No feedback is neither success nor failure; avoid engagement optimisation |
| 19 | GTM | asset approved/rejected, published, reach, conversion, claim issue, campaign result | Content/campaign calibration | Growth never overrides accuracy, independence or trust |

## Rules
- Freeze the prior prediction, recommendation, action, evidence and method version before the outcome is used.
- An outcome records what happened, not whether the earlier decision was reasonable given what was knowable.
- Corrections and revocations propagate to every evaluation and learning consumer.
- Families and rules are fixed; exact capture source, consent, minimisation and attribution window per individual
  outcome remain implementation-level and are validated in build.
