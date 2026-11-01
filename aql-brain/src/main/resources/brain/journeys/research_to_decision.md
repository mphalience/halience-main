---
name: research_to_decision
description: Research a material question to sufficiency, form a judgement and recommendation, record the human decision, act with approval and learn from the outcome.
phase: 1
goal: Reach a governed decision on a material question with preserved basis and observed outcome.
loops: LP-14, LP-10, LP-15, LP-09, LP-17, LP-06
chain: CH-01
consumers: Halie, Halience OS, Investment, Venture Studio
---
## frame_and_gather
- title: Frame the question and gather evidence
- behaviour: researcher
- loop: LP-14
- done_when: output
- approval: none
- why: The decision needs a defined question and a grounded evidence base.

## reconcile_evidence
- title: Reconcile conflicts and identify gaps
- after: frame_and_gather
- behaviour: evidence_reconciler
- loop: LP-14
- done_when: output
- approval: none
- why: Conflicts and known unknowns must be explicit before analysis.

## analyse
- title: Derive the analysis the question needs
- after: reconcile_evidence
- behaviour: analyst
- loop: LP-01
- done_when: output
- approval: none
- why: Turns evidence into intelligence with method and assumptions visible.

## judge
- title: Judge against explicit criteria
- after: analyse
- behaviour: judge
- loop: LP-01
- done_when: output
- approval: none
- why: An inspectable judgement with confidence and limits is the basis for recommendation.

## recommend
- title: Recommend options and next action
- after: judge
- behaviour: recommender
- loop: LP-01
- done_when: output
- approval: none
- why: A recommendation creates no authority; it frames the human choice.

## record_decision
- title: Record the authorised person's decision with frozen basis
- after: recommend
- behaviour: decision_recorder
- loop: LP-10
- done_when: human
- approval: policy
- phase: 2
- why: Decision-time evidence and judgement are preserved separately from later outcomes.

## approve_action
- title: Approve the exact action the decision requires
- after: record_decision
- behaviour: approval_assessor
- loop: LP-15
- done_when: approval
- approval: required
- why: Approval returns an authorised or rejected state; it does not execute.

## plan_work
- title: Coordinate the human and agent work to carry out the decision
- after: approve_action
- behaviour: work_orchestrator
- loop: LP-09
- done_when: output
- approval: none
- phase: 2
- why: Delegated work runs inside explicit scope and authority.

## execute_action
- title: Execute the approved action
- after: approve_action
- behaviour: action_executor
- loop: LP-01
- done_when: outcome
- approval: required
- why: Only the approved version is executed, with an audit record.

## record_outcome
- title: Record the observed outcome against the frozen basis
- after: execute_action, plan_work
- behaviour: outcome_recorder
- loop: LP-06
- done_when: outcome
- approval: none
- why: Outcome learning closes the lineage without hindsight rewriting.
