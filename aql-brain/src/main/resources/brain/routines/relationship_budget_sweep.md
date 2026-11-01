---
name: relationship_budget_sweep
description: Weekly sweep of Owner, Renter and Landlord relationships for cost changes, budget variance, renewals and cost-reduction opportunities.
phase: 1
cadence: weekly
behaviour: financial_actions
trigger: TR-06
loops: LP-03
scope: user
signals: SG-017, SG-035, SG-008
---
Checks each active Relationship (Owner, Renter, Landlord) for ongoing home-management and household budget signals:
insurance and tenancy renewals, actual against expected property costs, budget variance, and cost-reduction
opportunities from true-cost intelligence.
Hands financial_actions the Relationship state, its retained records and their sources, and the user's permitted
budget context.
Material means a renewal inside its warning window, a cost change or variance above the configured threshold, or a
saving with a real consequence. Everything else is held.
Never act, switch provider or contact anyone for the user: it produces prioritised financial-management actions with
reasons, inside the regulated perimeter. Sensitive financial context is used only for this purpose and is never
treated as a Watch change.
