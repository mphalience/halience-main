---
name: settle_buy
description: The Settle Buy Process for one Property, from plan and property intelligence through offer, documents and completion, ending in the Owner Relationship.
phase: 1
goal: Help the buyer complete a well-evidenced purchase and move into the Owner Relationship.
loops: LP-02, LP-14, LP-03, LP-15, LP-06
consumers: Settle, Ask Settle
---
## plan_process
- title: Create the Buy plan for this Property
- behaviour: process_planner
- done_when: output
- approval: none
- why: PA-017 gives ordered steps, dependencies, deadlines and the authority each step needs.

## gather_property_evidence
- title: Retrieve current Property and Area evidence
- behaviour: property_evidence_retriever
- loop: LP-14
- done_when: output
- approval: none
- why: Every later judgement rests on grounded, dated evidence (PA-001).

## derive_property_intelligence
- title: Derive fundamentals, costs and risks
- after: gather_property_evidence
- behaviour: property_intelligence
- done_when: output
- approval: none
- why: True cost and risk shape whether and how much to offer (PA-002).

## judge_price
- title: Form the Price Verdict
- after: derive_property_intelligence
- behaviour: price_verdict
- done_when: output
- approval: none
- why: A directional verdict on asking price with basis and confidence (PA-003).

## check_affordability
- title: Calculate indicative affordability for this Property
- after: plan_process
- behaviour: affordability
- done_when: output
- approval: none
- why: Indicative, assumption-explicit, and never a lender decision (PA-035, PA-036).

## prioritise_next
- title: Prioritise what matters now before offering
- after: judge_price, check_affordability
- behaviour: process_prioritiser
- done_when: output
- approval: none
- why: Ranks actions by consequence and deadline (PA-018).

## draft_offer
- title: Draft the offer communication
- after: prioritise_next
- behaviour: communication_drafter
- done_when: output
- approval: none
- why: An unsent draft grounded in the evidence and Process state (PA-014).

## submit_offer
- title: Send the approved offer
- after: draft_offer
- behaviour: action_executor
- loop: LP-15
- done_when: outcome
- approval: required
- why: External communication and offers are consequential and need explicit approval (PA-019).

## extract_documents
- title: Extract survey, search, mortgage and legal documents as they arrive
- after: submit_offer
- behaviour: document_extractor
- done_when: output
- approval: none
- why: Private documents become structured evidence with source location (PA-011).

## reconcile_documents
- title: Reconcile document findings against public evidence
- after: extract_documents
- behaviour: document_reconciler
- done_when: output
- approval: none
- why: Conflicts and gaps surface before exchange (PA-012).

## draft_enquiries
- title: Draft enquiries for the conveyancer or seller
- after: reconcile_documents
- behaviour: communication_drafter
- done_when: output
- approval: none
- why: Turns findings into specific questions without giving legal advice.

## send_enquiries
- title: Send approved enquiries
- after: draft_enquiries
- behaviour: action_executor
- loop: LP-15
- done_when: outcome
- approval: required
- why: Sending is a consequential external action.

## plan_finances
- title: Identify budgeting and property-finance actions
- after: check_affordability, reconcile_documents
- behaviour: financial_actions
- done_when: output
- approval: none
- why: PA-038 actions inside the regulated perimeter.

## build_purchase_pack
- title: Assemble the purchase document pack
- after: reconcile_documents
- behaviour: pack_builder
- done_when: output
- approval: none
- why: Keeps the buyer's evidence and documents in one versioned artefact with gaps listed (PA-016).

## exchange_and_complete
- title: Buyer exchanges contracts and completes
- after: send_enquiries, plan_finances, build_purchase_pack
- done_when: human
- approval: required
- why: The transaction is the buyer's decision, made with their conveyancer.

## record_completion
- title: Record the purchase outcome against frozen predictions
- after: exchange_and_complete
- behaviour: outcome_recorder
- loop: LP-06
- done_when: outcome
- approval: none
- why: Calibrates valuation, verdict and recommendations without hindsight.

## start_owner_relationship
- title: Transition to the Owner Relationship and plan ongoing management
- after: record_completion
- behaviour: process_planner
- done_when: outcome
- approval: none
- why: Buy ends by becoming the Owner Relationship for the Property.
