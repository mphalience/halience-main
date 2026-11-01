---
name: operating_health_response
description: From an operating health exception through materiality, attention, diagnosis, approved response and measured outcome, without employee surveillance.
phase: 3
goal: Resolve, manage or escalate a material operating issue with state updated and learning captured.
loops: LP-20, LP-03, LP-08, LP-14, LP-09, LP-17, LP-15, LP-06
chain: CH-08
consumers: Halience Ops, Halie
---
## observe_health
- title: Assess operating health from governed operating evidence
- behaviour: operating_health_analyst
- loop: LP-20
- done_when: output
- approval: none
- why: Health is read from work, commitments, dependencies and business state, never personal monitoring.

## assess_materiality
- title: Decide whether the exception is material
- after: observe_health
- behaviour: materiality_assessor
- loop: LP-03
- done_when: output
- approval: none
- why: No material exception means continue monitoring.

## triage_attention
- title: Route the exception to the people with authority to respond
- after: assess_materiality
- behaviour: attention_triager
- loop: LP-08
- done_when: output
- approval: none
- why: Escalation respects role, authority and non-surveillance.

## research_diagnosis
- title: Gather evidence the diagnosis needs
- after: triage_attention
- behaviour: researcher
- loop: LP-14
- done_when: output
- approval: none
- why: Diagnosis rests on evidence, not assumption.

## diagnose
- title: Diagnose causes and propose responses
- after: research_diagnosis
- behaviour: operating_health_analyst
- loop: LP-20
- done_when: output
- approval: none
- why: Responses come with owner, consequence and authority needed.

## plan_response
- title: Plan the response as human and agent work
- after: diagnose
- behaviour: work_orchestrator
- loop: LP-09
- done_when: output
- approval: none
- why: The response is coordinated through shared work state.

## approve_response
- title: Approve a consequential response
- after: plan_response
- behaviour: approval_assessor
- loop: LP-15
- done_when: approval
- approval: required
- why: Material interventions are human decisions.

## supervise_delegated_work
- title: Supervise any delegated agent runs
- after: approve_response
- behaviour: agent_run_supervisor
- loop: LP-17
- done_when: output
- approval: none
- why: Agent runs stay inside approved scope and budget.

## measure_outcome
- title: Record the outcome and feed the next health assessment
- after: supervise_delegated_work
- behaviour: outcome_recorder
- loop: LP-06
- done_when: outcome
- approval: none
- why: Learning on what responses work.
