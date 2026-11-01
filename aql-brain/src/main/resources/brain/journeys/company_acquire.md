---
name: company_acquire
description: The Acquire build - bring a bought Company onto the machine, read its bank and accounting data, set a baseline and run its weekly operating report.
phase: 2
goal: Have the acquired Company reporting weekly to its owner from reconciled bank and accounting data.
loops: LP-14, LP-03, LP-08, LP-20, LP-12, LP-06
consumers: Acquire, Halie
---
## grant_data_access
- title: Owner grants read access to bank and accounting systems
- done_when: human
- approval: required
- why: Nothing is read without a recorded, scoped access grant.

## onboard_company
- title: Resolve the Company and read bank and accounting data
- after: grant_data_access
- behaviour: acquisition_onboarder
- loop: LP-14
- done_when: output
- approval: none
- why: Establishes entities, accounts and ledgers with the source systems kept authoritative.

## reconcile_baseline
- title: Reconcile bank, ledger and accounts into a frozen baseline
- after: onboard_company
- behaviour: evidence_reconciler
- loop: LP-14
- done_when: output
- approval: none
- why: Weekly reports need a trusted reference point with conflicts explicit.

## onboarding_plan
- title: Propose the onboarding plan and reporting scope
- after: reconcile_baseline
- behaviour: planner
- done_when: output
- approval: none
- why: Sets data gaps to close, measures, recipients, cadence and materiality rules.

## agree_reporting_scope
- title: Owner agrees measures, recipients and materiality rules
- after: onboarding_plan
- done_when: human
- approval: required
- why: What is reported and to whom is the owner's decision.

## first_operating_report
- title: Produce the first weekly operating report
- after: agree_reporting_scope
- behaviour: operating_reporter
- loop: LP-03
- done_when: output
- approval: none
- why: Proves the report end to end before the weekly routine takes over.

## start_weekly_routine
- title: Owner confirms the report and the weekly routine starts
- after: first_operating_report
- done_when: human
- approval: policy
- why: The weekly_operating_report routine runs from here.

## route_material_change
- title: Route material operating changes to the owner
- after: start_weekly_routine
- behaviour: attention_triager
- loop: LP-08
- done_when: output
- approval: none
- why: Material changes reach the owner without becoming noise.

## operating_health_handoff
- title: Hand the Company into operating health assessment
- after: start_weekly_routine
- behaviour: operating_health_analyst
- loop: LP-20
- done_when: output
- approval: none
- phase: 3
- why: Weekly reporting is groundwork for LP-20.

## portfolio_handoff
- title: Include the Company in portfolio review
- after: operating_health_handoff
- behaviour: portfolio_diagnostician
- loop: LP-12
- done_when: output
- approval: none
- phase: 3
- why: Post-acquisition evidence feeds portfolio value creation.
