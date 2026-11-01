---
name: information_remediation
description: From a correction, redaction or reclassification through impact tracing, Knowledge reconciliation, reapproval and targeted resume of owning loops.
phase: 2
goal: Correct, restrict or supersede affected state and remediate downstream uses auditably.
loops: LP-16, LP-07, LP-15, LP-09
chain: CH-12
consumers: Halience OS, AQL Operations
---
## trace_impact
- title: Record the change and trace every downstream use
- behaviour: reclassification_remediator
- loop: LP-16
- done_when: output
- approval: none
- why: Remediation is targeted at real impact, not a blind cascade.

## restrict_without_disclosure
- title: Restrict or redact affected items without revealing content
- after: trace_impact
- behaviour: action_executor
- loop: LP-16
- done_when: outcome
- approval: required
- why: Restricted content must not leak through remediation itself.

## reconcile_knowledge
- title: Reconcile affected organisational Knowledge
- after: trace_impact
- behaviour: knowledge_promoter
- loop: LP-07
- done_when: output
- approval: none
- why: Knowledge affected by the change goes back through LP-07.

## reapprove
- title: Reapprove approved outputs or actions that relied on the information
- after: reconcile_knowledge
- behaviour: approval_assessor
- loop: LP-15
- done_when: approval
- approval: required
- why: Material semantic change to approved state requires reapproval.

## resume_owning_loops
- title: Hand corrected state to owning loops to recompute or resume
- after: restrict_without_disclosure, reapprove
- behaviour: work_orchestrator
- loop: LP-09
- done_when: output
- approval: none
- why: Owning loops recompute only where needed.

## verify
- title: Verify each remediation item and record what remains open
- after: resume_owning_loops
- behaviour: reclassification_remediator
- loop: LP-16
- done_when: outcome
- approval: none
- why: Items close only when owners confirm; untraceable impact stays visible.
