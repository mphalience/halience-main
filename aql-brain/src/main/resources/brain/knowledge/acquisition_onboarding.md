---
name: acquisition_onboarding
description: How the Acquire build brings a bought Company onto the machine - access grant, bank and accounting data, baseline, reporting scope and weekly operating report.
phase: 2
status: working
owner: Finance
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-04, GV-07, GV-08, GV-18, GV-22
---
# Acquisition onboarding (Acquire build)

## Purpose
After Halience acquires a Company, the Acquire build brings it onto the machine: it reads the Company's bank and
accounting data, establishes a reconciled baseline and produces a weekly operating report for its owner. This is
groundwork for operating health (`LP-20`) and portfolio value creation (`LP-12`).

## Steps
1. Access grant: the owner grants read access to specific bank accounts and accounting systems for specific periods.
   The grant is recorded with who granted it and when.
2. Entity resolution: the Company, its entities, bank accounts and ledgers are resolved; unresolved mappings stay
   explicit.
3. Data read: bank and accounting data are read through the governed accounting adapter. The source systems remain
   authoritative; AQL holds evidence and references, not a competing ledger.
4. Reconciliation: bank against ledger; periods against each other; management against filed accounts where
   available. Conflicts and data-quality issues are listed.
5. Baseline: agreed headline measures for agreed periods, with definitions and sources, frozen as the reference
   point.
6. Reporting scope: the owner agrees measures, recipients, cadence and materiality rules.
7. Weekly operating report: produced each week from current data (see operating_reporter routine).

## Boundaries
- Read-only. AQL never moves money, posts entries or changes the Company's systems.
- Company data stays inside the Company's Work Context.
- Payroll and individual staff data are not brought into operating views beyond what the owner agreed. Reports are
  about the business, not employee monitoring (`GV-22`).
- Source failure, missing data and a true zero are distinct states (`GV-07`).

## Measures
Measures follow financial_health_framework and business_performance_framework once defined. Until then, each report
states the definition it used.

## Open items
Standard measure set, default materiality rules, owner sign-off on baseline, integration list beyond accounting and
bank data.
