---
name: settle_processes
description: The Settle Processes (Buy, Sell, Rent, Let) and Relationships (Owner, Renter, Landlord) - state, milestones, deadlines, transitions and action boundaries.
phase: 1
status: canonical
owner: Settle
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-05, GV-06, GV-10, GV-15
---
# Settle Processes and Relationships

## Processes (CX-08)
A Process is a bounded, finite piece of work attached to a Property: Buy, Sell, Rent or Let. Settle's Process engine
owns its state: phase, milestones, activities, deadlines and counterparties. A personalised Process needs an active
Property and Settle entitlement; without one, only a generic preview is offered.

Indicative shape of each Process (exact milestones are Settle product decisions):
- Buy: search and shortlist; viewings; affordability and finance; offer and negotiation; survey; legal and
  conveyancing; exchange; completion.
- Sell: preparation and Sales Pack; valuation and listing; viewings and offers; legal and conveyancing; exchange;
  completion.
- Rent: search; viewings; application and referencing; tenancy agreement and deposit; move-in.
- Let: preparation and compliance; Tenancy Pack; marketing; tenant selection; tenancy agreement; deposit; move-in.

## Relationships (CX-09)
A Relationship is ongoing home management attached to a Property: Owner, Renter or Landlord. Buy leads to Owner,
Rent to Renter, Let to Landlord; Sell ends Owner where appropriate. A Relationship has no false finite completion
state. It covers insurance, renewals, expiries, maintenance, compliance and costs (SG-017, SG-035).

## State and signals
- Process state transitions are typed events with actor, time and basis (SG-015).
- Deadlines and time windows (SG-016) may trigger reevaluation without implying any external change.
- User inputs to a Process are user edits (SG-030): they recompute plans and priorities but are not Watch events.
- Process outcomes (offer, purchase, tenancy, completion) are captured against frozen prior outputs (SG-019).

## Planning and prioritising
- A plan (PA-017, OUT-13) gives ordered steps, dependencies, checkpoints and the authority each step needs.
- Priorities (PA-018) rank what matters now by consequence, deadline proximity and dependency, with reasons.
- Financial management in a Process or Relationship (PA-038) covers budgeting, cost reduction, tracking and
  property-finance actions; it is distinct from the affordability calculation itself.

## Action boundaries
- Recommendation, draft and execution are separate authority levels (GV-05).
- External communication and state-changing actions need explicit approval or an authorised rule (PA-019, GV-06),
  and are logged with actor, basis, tool and result (GV-10).
- Legal, conveyancing and finance guidance stays inside the regulated perimeter (GV-15): Settle explains and
  organises; it does not give regulated legal or mortgage advice.
- Retaining a Property or document never implies ownership or a Relationship (CX-10).

## Open items
Final milestone sets per Process and nation, deadline rules, and the action taxonomy with authority per action.
