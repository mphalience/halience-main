---
name: material_change_response
description: From a detected change through materiality assessment, attention routing, planned and approved response, to recorded outcome.
phase: 1
goal: Handle or hold a meaningful change with the right involvement and a recorded outcome.
loops: LP-03, LP-08, LP-09, LP-17, LP-15, LP-06
chain: CH-02
consumers: Settle Watch, AQL Operations, Halie, Halience OS
---
## detect_change
- title: Detect and attribute the change against frozen prior state
- behaviour: watch_change_detector
- loop: LP-03
- done_when: output
- approval: none
- why: Raw source mutations are not forwarded; change is compared with prior state first.

## assess_materiality
- title: Assess whether the change is material
- after: detect_change
- behaviour: materiality_assessor
- loop: LP-03
- done_when: output
- approval: none
- why: Not material means hold and keep monitoring.

## triage_attention
- title: Decide whether someone needs to be involved now, and who
- after: assess_materiality
- behaviour: attention_triager
- loop: LP-08
- done_when: output
- approval: none
- phase: 2
- why: Material change does not automatically mean interruption.

## plan_response
- title: Plan the response as human and agent work
- after: triage_attention
- behaviour: work_orchestrator
- loop: LP-09
- done_when: output
- approval: none
- phase: 2
- why: The response is coordinated through shared work state.

## approve_response
- title: Approve a consequential response
- after: plan_response
- behaviour: approval_assessor
- loop: LP-15
- done_when: approval
- approval: required
- why: Consequential responses need approval before action.

## execute_response
- title: Execute the approved response
- after: approve_response
- behaviour: action_executor
- done_when: outcome
- approval: required
- why: Only the approved action is executed, with an audit record.

## record_outcome
- title: Record what happened against the change and response
- after: execute_response
- behaviour: outcome_recorder
- loop: LP-06
- done_when: outcome
- approval: none
- why: Feeds materiality and noise calibration.
