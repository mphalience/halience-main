---
name: strategy_cycle
description: One strategy and planning cycle from objectives and scenarios through human allocation decisions, execution, monitoring and measured replan.
phase: 2
goal: Execute a plan tied to objectives, measure it and replan from current evidence.
loops: LP-11, LP-14, LP-10, LP-15, LP-09, LP-03, LP-08, LP-06
chain: CH-09
consumers: Halie, Halience OS
---
## frame_objectives
- title: Restate objectives, owners, measures and horizon
- behaviour: strategy_planner
- loop: LP-11
- done_when: output
- approval: none
- why: Planning starts from measurable objectives.

## research
- title: Gather evidence the scenarios need
- after: frame_objectives
- behaviour: researcher
- loop: LP-14
- done_when: output
- approval: none
- why: Scenarios rest on evidence, with gaps explicit.

## build_scenarios
- title: Build and compare scenarios and propose priorities
- after: research
- behaviour: strategy_planner
- loop: LP-11
- done_when: output
- approval: none
- why: Options with trade-offs and assumptions for the owners to choose between.

## allocation_decision
- title: Leadership chooses priorities and allocation
- after: build_scenarios
- done_when: human
- approval: required
- why: Material allocation and strategy decisions are human-owned.

## record_decision
- title: Record the allocation decision with frozen basis
- after: allocation_decision
- behaviour: decision_recorder
- loop: LP-10
- done_when: output
- approval: none
- why: Replans must not rewrite earlier decision bases.

## plan_execution
- title: Plan and delegate the work
- after: record_decision
- behaviour: work_orchestrator
- loop: LP-09
- done_when: output
- approval: policy
- why: The plan becomes owned Work Blocks and Tasks.

## monitor
- title: Monitor execution and conditions for material change
- after: plan_execution
- behaviour: materiality_assessor
- loop: LP-03
- done_when: output
- approval: none
- why: Material change may trigger a replan.

## route_attention
- title: Route material changes to plan owners
- after: monitor
- behaviour: attention_triager
- loop: LP-08
- done_when: output
- approval: none
- why: Owners are involved when something warrants it.

## measure_outcomes
- title: Record outcomes against the plan
- after: route_attention
- behaviour: outcome_recorder
- loop: LP-06
- done_when: outcome
- approval: none
- why: Variance against plan is evidence for the next cycle.

## replan
- title: Propose a replan from current evidence
- after: measure_outcomes
- behaviour: strategy_planner
- loop: LP-11
- done_when: output
- approval: none
- why: Deliberate loop-back into the next cycle, not a circular dependency.
