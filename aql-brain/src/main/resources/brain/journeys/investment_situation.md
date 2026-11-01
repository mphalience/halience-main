---
name: investment_situation
description: The Invest build - Origination to Ownership on one Situation, through screening, diligence, valuation, governed decision, transaction and ownership reporting.
phase: 2
goal: Carry one Situation from origination to a governed invest or stop decision and, if invested, to ownership reporting and learning.
loops: LP-18, LP-14, LP-10, LP-15, LP-03, LP-06
chain: CH-06
consumers: Investment, Halie
---
## originate
- title: Capture the opportunity and open the Situation
- behaviour: opportunity_screener
- loop: LP-18
- done_when: output
- approval: none
- why: One Situation carries the opportunity from origination to ownership.

## screen
- title: Screen against the Investment Opportunity Evaluation framework
- after: originate
- behaviour: opportunity_screener
- loop: LP-18
- done_when: output
- approval: none
- why: Proposes progress, investigate, watch or eliminate with evidence and gaps.

## screening_decision
- title: Investment team decides to progress, investigate, watch or eliminate
- after: screen
- done_when: human
- approval: required
- why: Progressing or stopping a Situation is a reserved human decision.

## diligence
- title: Run diligence research on the target
- after: screening_decision
- behaviour: diligence_researcher
- loop: LP-14
- done_when: output
- approval: none
- why: Findings, conflicts and gaps by area, without investment judgement.

## reconcile_diligence
- title: Reconcile management figures with independent evidence
- after: diligence
- behaviour: evidence_reconciler
- loop: LP-14
- done_when: output
- approval: none
- why: Unresolved conflicts must be visible before valuation.

## valuation
- title: Value the Company and model returns
- after: reconcile_diligence
- behaviour: valuation_analyst
- loop: LP-18
- done_when: output
- approval: none
- why: Ranges, scenarios and sensitivities with explicit assumptions.

## recommendation
- title: Produce the framework-based recommendation pack
- after: valuation
- behaviour: investment_recommender
- loop: LP-18
- done_when: output
- approval: none
- why: Synthesises evidence and judgement for the decision-makers.

## investment_decision
- title: Authorised decision-makers decide to invest, hold or stop
- after: recommendation
- done_when: human
- approval: required
- why: The investment decision is a reserved human decision.

## record_decision
- title: Record the decision with frozen basis
- after: investment_decision
- behaviour: decision_recorder
- loop: LP-10
- done_when: output
- approval: none
- why: Decision-time evidence and judgement are preserved for later learning.

## approve_transaction
- title: Approve the exact transaction terms
- after: record_decision
- behaviour: approval_assessor
- loop: LP-15
- done_when: approval
- approval: required
- why: Material new evidence after approval requires reapproval before transacting.

## transact
- title: People and advisers execute the transaction
- after: approve_transaction
- done_when: outcome
- approval: required
- why: AQL never transacts; it records that the transaction happened.

## ownership_reporting
- title: Report on the held position against the investment case
- after: transact
- behaviour: ownership_reporter
- loop: LP-03
- done_when: output
- approval: none
- why: Ownership is the end of the Situation, reported against the frozen case.

## learn
- title: Record outcomes against the frozen decision basis
- after: ownership_reporting
- behaviour: outcome_recorder
- loop: LP-06
- done_when: outcome
- approval: none
- why: Calibrates screening, valuation and recommendation without hindsight.
