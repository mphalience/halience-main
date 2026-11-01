---
name: evaluation_remediation
description: From an evaluation failure through degrade or block, remediation planning, approved fix, re-evaluation and observed effectiveness.
phase: 1
goal: Return a failing capability to a passing state, or keep it safely degraded or blocked with an auditable record.
loops: LP-05, LP-17, LP-09, LP-15, LP-06
chain: CH-11
consumers: AQL Operations, Halie
---
## evaluate
- title: Evaluate the capability or output against versioned criteria
- behaviour: output_evaluator
- loop: LP-05
- done_when: output
- approval: none
- why: The control decision starts from an explicit, versioned evaluation result.

## apply_control_state
- title: Degrade, block or route to fallback according to consequence
- after: evaluate
- behaviour: fallback_router
- loop: LP-05
- done_when: output
- approval: policy
- why: Consequential capabilities must not silently keep failing.

## plan_remediation
- title: Plan the remediation with owner and authority per step
- after: apply_control_state
- behaviour: planner
- loop: LP-09
- done_when: output
- approval: none
- why: Remediation is governed work, not an ad hoc change.

## propose_primitive_fix
- title: Propose any primitive change needed through the one door
- after: plan_remediation
- behaviour: primitive_editor
- done_when: output
- approval: none
- why: Halie proposes edits to its own primitives but never applies or approves them.

## approve_remediation
- title: Approve the exact remediation change
- after: propose_primitive_fix
- behaviour: approval_assessor
- loop: LP-15
- done_when: approval
- approval: required
- why: Consequential remediation needs approval; approval itself changes nothing.

## supervise_remediation_run
- title: Supervise any delegated agent run doing the fix
- after: approve_remediation
- behaviour: agent_run_supervisor
- loop: LP-17
- done_when: output
- approval: none
- phase: 2
- why: Delegated remediation stays inside its approved scope and budget.

## apply_remediation
- title: Apply the approved change
- after: approve_remediation
- behaviour: action_executor
- done_when: outcome
- approval: required
- why: Applying is separately authorised and leaves an Action record that can be rolled back.

## re_evaluate
- title: Re-evaluate before release or recovery
- after: apply_remediation, supervise_remediation_run
- behaviour: output_evaluator
- loop: LP-05
- done_when: output
- approval: none
- why: Evaluation owns the control decision; execution owns the fix.

## observe_effect
- title: Record whether the remediation worked in production
- after: re_evaluate
- behaviour: outcome_recorder
- loop: LP-06
- done_when: outcome
- approval: none
- why: Effectiveness is observed, not assumed.
