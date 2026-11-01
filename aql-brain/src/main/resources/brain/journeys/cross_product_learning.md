---
name: cross_product_learning
description: From permitted local learning through sanitised cross-product comparison, independent validation and approved sharing to optional governed Knowledge.
phase: 2
goal: Produce validated privacy-safe shared learning, or an explicit local, held or rejected state.
loops: LP-06, LP-13, LP-05, LP-15, LP-07
chain: CH-10
consumers: AQL Learning, Halie
---
## collect_local_learning
- title: Collect permitted local outcomes and calibration records
- behaviour: outcome_recorder
- loop: LP-06
- done_when: output
- approval: none
- why: Only learning with an explicit permitted reuse level is eligible.

## compare_across_contexts
- title: Sanitise and compare patterns across products or companies
- after: collect_local_learning
- behaviour: cross_product_learner
- loop: LP-13
- done_when: output
- approval: none
- why: Only approved privacy-safe signals cross boundaries.

## validate_pattern
- title: Independently validate the shared learning candidate
- after: compare_across_contexts
- behaviour: output_evaluator
- loop: LP-05
- done_when: output
- approval: none
- why: The producer of a pattern does not certify it.

## approve_sharing
- title: Approve sharing the validated learning across boundaries
- after: validate_pattern
- behaviour: approval_assessor
- loop: LP-15
- done_when: approval
- approval: required
- why: Crossing product or company boundaries is a governed decision.

## propose_knowledge
- title: Propose promotion to organisational Knowledge where warranted
- after: approve_sharing
- behaviour: knowledge_promoter
- loop: LP-07
- done_when: output
- approval: none
- why: Not all shared learning needs to become Knowledge.

## approve_knowledge
- title: Knowledge owner approves the promotion
- after: propose_knowledge
- behaviour: approval_assessor
- loop: LP-15
- done_when: approval
- approval: required
- why: Knowledge promotion is consequential.

## observe_use
- title: Observe how shared learning performs where applied
- after: approve_sharing
- behaviour: cross_product_learner
- loop: LP-13
- done_when: outcome
- approval: none
- why: Shared learning is revisited as new outcomes arrive.
