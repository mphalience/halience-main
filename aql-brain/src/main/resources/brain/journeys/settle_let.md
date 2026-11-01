---
name: settle_let
description: The Settle Let Process for an owned Property, through compliance, Tenancy Pack, tenant selection and agreement, ending in the Landlord Relationship.
phase: 1
goal: Help the landlord let the Property compliantly and move into the Landlord Relationship.
loops: LP-02, LP-14, LP-15, LP-06
consumers: Settle, Ask Settle
---
## plan_process
- title: Create the Let plan for this Property
- behaviour: process_planner
- done_when: output
- approval: none
- why: PA-017 sets preparation, compliance, marketing and tenancy steps.

## gather_property_evidence
- title: Retrieve current Property and Area evidence
- behaviour: property_evidence_retriever
- loop: LP-14
- done_when: output
- approval: none
- why: Compliance and marketing rest on grounded evidence.

## derive_property_intelligence
- title: Derive costs, condition and compliance risks
- after: gather_property_evidence
- behaviour: property_intelligence
- done_when: output
- approval: none
- why: Shows obligations and costs before letting.

## extract_compliance_documents
- title: Extract certificates and compliance documents
- after: plan_process
- behaviour: document_extractor
- done_when: output
- approval: none
- why: Compliance documents become structured evidence with dates and expiries.

## reconcile_compliance
- title: Reconcile compliance documents against requirements and evidence
- after: extract_compliance_documents, derive_property_intelligence
- behaviour: document_reconciler
- done_when: output
- approval: none
- why: Gaps and expiries surface before the Property is marketed.

## build_tenancy_pack
- title: Build the Tenancy Pack with gaps listed
- after: reconcile_compliance
- behaviour: pack_builder
- done_when: output
- approval: none
- why: PA-016 pre-populates the Tenancy Pack for the jurisdiction and date.

## share_tenancy_pack
- title: Share the approved Tenancy Pack and listing details
- after: build_tenancy_pack
- behaviour: action_executor
- loop: LP-15
- done_when: outcome
- approval: required
- why: Publishing and disclosure need the landlord's explicit approval.

## prioritise_next
- title: Prioritise applicants, viewings and outstanding obligations
- after: share_tenancy_pack
- behaviour: process_prioritiser
- done_when: output
- approval: none
- why: Ranks what matters now by consequence and deadline.

## draft_communications
- title: Draft communications to applicants or the letting agent
- after: prioritise_next
- behaviour: communication_drafter
- done_when: output
- approval: none
- why: Unsent drafts grounded in the pack and Process state.

## send_communications
- title: Send approved communications
- after: draft_communications
- behaviour: action_executor
- loop: LP-15
- done_when: outcome
- approval: required
- why: External communication is consequential.

## plan_finances
- title: Plan letting costs, rent budget and tracking
- after: derive_property_intelligence
- behaviour: financial_actions
- done_when: output
- approval: none
- why: PA-038 actions within the regulated perimeter.

## agree_tenancy
- title: Landlord selects the tenant and signs the tenancy
- after: send_communications, plan_finances
- done_when: human
- approval: required
- why: Choosing a tenant and entering a tenancy is the landlord's decision.

## record_tenancy
- title: Record the letting outcome
- after: agree_tenancy
- behaviour: outcome_recorder
- loop: LP-06
- done_when: outcome
- approval: none
- why: Feeds Let Process evaluation.

## start_landlord_relationship
- title: Transition to the Landlord Relationship and plan obligations and renewals
- after: record_tenancy
- behaviour: process_planner
- done_when: outcome
- approval: none
- why: Let ends by becoming the Landlord Relationship.
