---
name: venture_build
description: The Build build - launch a venture milestone by milestone from commission through discovery, build, launch gate, publishing and scale, change or stop.
phase: 2
goal: Take a commissioned venture to launch and a governed scale, change or stop decision with learning preserved.
loops: LP-19, LP-14, LP-09, LP-17, LP-15, LP-04, LP-03, LP-08, LP-10, LP-06
chain: CH-07
consumers: Venture Studio, Halie
---
## commission_brief
- title: Prepare the commission brief and initial milestone plan
- behaviour: venture_commissioner
- loop: LP-19
- done_when: output
- approval: none
- why: The commission gate needs thesis, evidence, scope and open questions in one place.

## commission_gate
- title: Commission gate decision (G0)
- after: commission_brief
- behaviour: venture_stage_gate
- loop: LP-15
- done_when: approval
- approval: required
- why: Starting a venture is a governed stage gate.

## record_commission
- title: Record the commission decision with frozen basis
- after: commission_gate
- behaviour: decision_recorder
- loop: LP-10
- done_when: output
- approval: none
- why: Later outcomes are judged against what was known at commission.

## discovery_research
- title: Test the riskiest assumptions with customer and market evidence
- after: record_commission
- behaviour: researcher
- loop: LP-14
- done_when: output
- approval: none
- why: Discovery gaps are filled with evidence before building.

## build_gate
- title: Build decision gate (G1)
- after: discovery_research
- behaviour: venture_stage_gate
- loop: LP-15
- done_when: approval
- approval: required
- why: Committing build effort is a governed stage gate.

## plan_build
- title: Plan build milestones and delegate human and agent work
- after: build_gate
- behaviour: work_orchestrator
- loop: LP-09
- done_when: output
- approval: policy
- why: Each milestone gets an owner, scope, budget and done condition.

## build_product
- title: Build the product in the authorised workspace
- after: plan_build
- behaviour: venture_builder
- loop: LP-17
- done_when: output
- approval: policy
- why: Coding happens within the approved scope under the workspace tool's policy.

## build_site
- title: Build the venture site and launch content drafts
- after: plan_build
- behaviour: venture_builder
- loop: LP-17
- done_when: output
- approval: policy
- why: Site build follows the playbook; nothing goes live at this step.

## supervise_build_runs
- title: Supervise build runs against scope and budget
- after: build_product, build_site
- behaviour: agent_run_supervisor
- loop: LP-17
- done_when: output
- approval: none
- why: Agents cannot self-expand scope, tools or budget; handoffs are validated.

## validate_launch_content
- title: Validate launch claims and content
- after: build_site
- behaviour: gtm_content_validator
- loop: LP-05
- done_when: output
- approval: none
- why: Customer-facing claims must be grounded and inside the regulated perimeter.

## launch_gate
- title: Launch readiness gate (G2)
- after: supervise_build_runs, validate_launch_content
- behaviour: venture_stage_gate
- loop: LP-15
- done_when: approval
- approval: required
- why: Going live is a governed stage gate.

## launch_publish
- title: Publish the approved launch content and site
- after: launch_gate
- behaviour: gtm_publisher
- loop: LP-04
- done_when: outcome
- approval: required
- why: Publishing executes only the approved version.

## observe_launch
- title: Monitor post-launch evidence for material change
- after: launch_publish
- behaviour: materiality_assessor
- loop: LP-03
- done_when: output
- approval: none
- why: Separates meaningful change from launch noise.

## route_attention
- title: Route material post-launch issues to the right people
- after: observe_launch
- behaviour: attention_triager
- loop: LP-08
- done_when: output
- approval: none
- why: Interventions start with the right person, not a broadcast.

## scale_change_stop_gate
- title: Scale, change or stop gate (G3)
- after: route_attention
- behaviour: venture_stage_gate
- loop: LP-15
- done_when: approval
- approval: required
- why: Scale and stop decisions are reserved human decisions.

## record_gate_decision
- title: Record the scale, change or stop decision with basis
- after: scale_change_stop_gate
- behaviour: decision_recorder
- loop: LP-10
- done_when: output
- approval: none
- why: Preserves decision lineage for later learning.

## learn
- title: Record venture outcomes against frozen gate bases
- after: record_gate_decision
- behaviour: outcome_recorder
- loop: LP-06
- done_when: outcome
- approval: none
- why: Builds evidence on what works for future ventures.
