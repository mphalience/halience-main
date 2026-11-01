---
name: settle_rent
description: The Settle Rent Process for one Property, through property intelligence, application and tenancy documents, ending in the Renter Relationship.
phase: 1
goal: Help the renter secure a well-understood tenancy and move into the Renter Relationship.
loops: LP-02, LP-14, LP-15, LP-06
consumers: Settle, Ask Settle
---
## plan_process
- title: Create the Rent plan for this Property
- behaviour: process_planner
- done_when: output
- approval: none
- why: PA-017 sets steps, dependencies and deadlines for renting.

## gather_property_evidence
- title: Retrieve current Property and Area evidence
- behaviour: property_evidence_retriever
- loop: LP-14
- done_when: output
- approval: none
- why: The renter sees grounded evidence about the home and area.

## derive_property_intelligence
- title: Derive running costs, condition and risks
- after: gather_property_evidence
- behaviour: property_intelligence
- done_when: output
- approval: none
- why: Running costs and risks matter as much as the rent.

## check_affordability
- title: Check budget fit for the rent and running costs
- after: derive_property_intelligence
- behaviour: affordability
- done_when: output
- approval: none
- why: Indicative only, with assumptions explicit and limits stated.

## prioritise_next
- title: Prioritise viewing questions and application steps
- after: plan_process, check_affordability
- behaviour: process_prioritiser
- done_when: output
- approval: none
- why: Ranks what matters now by consequence and deadline.

## draft_application
- title: Draft the application and questions for the agent or landlord
- after: prioritise_next
- behaviour: communication_drafter
- done_when: output
- approval: none
- why: Unsent draft grounded in the Process state.

## submit_application
- title: Send the approved application
- after: draft_application
- behaviour: action_executor
- loop: LP-15
- done_when: outcome
- approval: required
- why: Sending personal details externally is consequential.

## extract_tenancy_documents
- title: Extract terms from the tenancy agreement and related documents
- after: submit_application
- behaviour: document_extractor
- done_when: output
- approval: none
- why: Lease terms become structured evidence with source location.

## reconcile_tenancy_documents
- title: Reconcile tenancy terms against evidence and the agreed offer
- after: extract_tenancy_documents
- behaviour: document_reconciler
- done_when: output
- approval: none
- why: Surfaces conflicts and gaps before signing.

## plan_finances
- title: Plan deposit, fees and monthly budget
- after: reconcile_tenancy_documents, check_affordability
- behaviour: financial_actions
- done_when: output
- approval: none
- why: PA-038 actions within the regulated perimeter.

## sign_tenancy
- title: Renter signs the tenancy and pays the deposit
- after: plan_finances
- done_when: human
- approval: required
- why: Entering a tenancy is the renter's decision.

## record_tenancy
- title: Record the tenancy outcome
- after: sign_tenancy
- behaviour: outcome_recorder
- loop: LP-06
- done_when: outcome
- approval: none
- why: Feeds Rent Process evaluation.

## start_renter_relationship
- title: Transition to the Renter Relationship and plan renewals and obligations
- after: record_tenancy
- behaviour: process_planner
- done_when: outcome
- approval: none
- why: Rent ends by becoming the Renter Relationship.
