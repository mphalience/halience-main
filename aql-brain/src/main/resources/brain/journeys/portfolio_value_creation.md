---
name: portfolio_value_creation
description: One portfolio value-creation cycle - monitor, diagnose, recommend interventions, governed human decision, intervene, measure, learn and reassess.
phase: 3
goal: Improve portfolio business value through evidenced, governed interventions and learn from their effect.
loops: LP-12, LP-03, LP-14, LP-10, LP-15, LP-09, LP-06, LP-13
consumers: Portfolio, Investment, Halie
---
## monitor_portfolio
- title: Review current state of each business
- behaviour: portfolio_diagnostician
- loop: LP-12
- done_when: output
- approval: none
- why: Operating and ownership reports are read against plan and case.

## assess_change
- title: Assess material change across the portfolio
- after: monitor_portfolio
- behaviour: materiality_assessor
- loop: LP-03
- done_when: output
- approval: none
- why: Focuses diagnosis where something meaningful changed.

## research_drivers
- title: Gather evidence on performance drivers
- after: assess_change
- behaviour: researcher
- loop: LP-14
- done_when: output
- approval: none
- why: Diagnosis gaps are filled with evidence.

## diagnose_and_recommend
- title: Diagnose and propose interventions
- after: research_drivers
- behaviour: portfolio_diagnostician
- loop: LP-12
- done_when: output
- approval: none
- why: Interventions with impact, timing, dependencies and ability to influence.

## intervention_decision
- title: Owners decide on interventions and hold, scale or exit
- after: diagnose_and_recommend
- done_when: human
- approval: required
- why: Material company interventions and hold, scale or exit decisions are reserved human decisions.

## record_decision
- title: Record the decision with frozen basis
- after: intervention_decision
- behaviour: decision_recorder
- loop: LP-10
- done_when: output
- approval: none
- why: Preserves what was known when the intervention was chosen.

## execute_intervention
- title: Plan and coordinate the intervention work
- after: record_decision
- behaviour: work_orchestrator
- loop: LP-09
- done_when: output
- approval: policy
- why: Interventions run as owned work within approved scope.

## measure_effect
- title: Record intervention outcomes
- after: execute_intervention
- behaviour: outcome_recorder
- loop: LP-06
- done_when: outcome
- approval: none
- why: Effect is measured against the frozen basis.

## share_learning
- title: Compare learning across businesses where privacy-safe
- after: measure_effect
- behaviour: cross_product_learner
- loop: LP-13
- done_when: output
- approval: none
- why: Only approved privacy-safe signals cross company boundaries.

## reassess
- title: Reassess the portfolio for the next cycle
- after: measure_effect
- behaviour: portfolio_diagnostician
- loop: LP-12
- done_when: output
- approval: none
- why: Each cycle starts from current evidence.
