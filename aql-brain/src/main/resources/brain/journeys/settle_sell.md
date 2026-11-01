---
name: settle_sell
description: The Settle Sell Process for an owned Property, through pricing evidence, Sales Pack, offers and completion, ending the Owner Relationship where appropriate.
phase: 1
goal: Help the seller reach a well-evidenced sale and close the Owner Relationship where appropriate.
loops: LP-02, LP-14, LP-15, LP-06
consumers: Settle, Ask Settle
---
## plan_process
- title: Create the Sell plan for this Property
- behaviour: process_planner
- done_when: output
- approval: none
- why: PA-017 sets steps, dependencies and deadlines for the sale.

## gather_property_evidence
- title: Retrieve current Property and Area evidence
- behaviour: property_evidence_retriever
- loop: LP-14
- done_when: output
- approval: none
- why: Pricing and disclosure start from grounded evidence.

## derive_property_intelligence
- title: Derive fundamentals, costs and risks buyers will see
- after: gather_property_evidence
- behaviour: property_intelligence
- done_when: output
- approval: none
- why: Shows the seller what the evidence says about the Property.

## judge_price
- title: Form a price position for the proposed asking price
- after: derive_property_intelligence
- behaviour: price_verdict
- done_when: output
- approval: none
- why: A directional view on asking price with basis and confidence.

## extract_seller_documents
- title: Extract title, compliance and property documents
- after: plan_process
- behaviour: document_extractor
- done_when: output
- approval: none
- why: Seller documents become structured evidence for the pack.

## reconcile_seller_documents
- title: Reconcile documents against public evidence
- after: extract_seller_documents, gather_property_evidence
- behaviour: document_reconciler
- done_when: output
- approval: none
- why: Surfaces conflicts before buyers or their advisers do.

## build_sales_pack
- title: Build the Sales Pack with gaps listed
- after: reconcile_seller_documents, judge_price
- behaviour: pack_builder
- done_when: output
- approval: none
- why: PA-016 pre-populates the Sales Pack from retained evidence.

## share_sales_pack
- title: Share the approved Sales Pack
- after: build_sales_pack
- behaviour: action_executor
- loop: LP-15
- done_when: outcome
- approval: required
- why: Disclosure and publication are the seller's choice and need explicit approval.

## prioritise_next
- title: Prioritise viewings, offers and outstanding items
- after: share_sales_pack
- behaviour: process_prioritiser
- done_when: output
- approval: none
- why: Ranks what matters now by consequence and deadline.

## draft_responses
- title: Draft responses to offers and buyer enquiries
- after: prioritise_next
- behaviour: communication_drafter
- done_when: output
- approval: none
- why: Unsent drafts grounded in the pack and Process state.

## send_responses
- title: Send approved responses
- after: draft_responses
- behaviour: action_executor
- loop: LP-15
- done_when: outcome
- approval: required
- why: External communication is consequential.

## accept_offer
- title: Seller accepts an offer
- after: send_responses
- done_when: human
- approval: required
- why: Accepting an offer is the seller's decision.

## plan_finances
- title: Identify sale costs, proceeds and finance actions
- after: accept_offer
- behaviour: financial_actions
- done_when: output
- approval: none
- why: PA-038 within the regulated perimeter.

## exchange_and_complete
- title: Seller exchanges contracts and completes
- after: accept_offer, plan_finances
- done_when: human
- approval: required
- why: The transaction is the seller's decision, made with their conveyancer.

## record_sale
- title: Record the sale outcome against frozen predictions
- after: exchange_and_complete
- behaviour: outcome_recorder
- loop: LP-06
- done_when: outcome
- approval: none
- why: Calibrates price position and seller recommendations.

## end_owner_relationship
- title: End the Owner Relationship for this Property where appropriate
- after: record_sale
- behaviour: process_planner
- done_when: outcome
- approval: none
- why: Sell ends the Owner Relationship where appropriate, without a false completion of other Relationships.
