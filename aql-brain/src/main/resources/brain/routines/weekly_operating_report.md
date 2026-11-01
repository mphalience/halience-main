---
name: weekly_operating_report
description: Weekly operating report for the owner of each acquired Company, from its bank and accounting data, with material changes flagged.
phase: 2
cadence: weekly
behaviour: operating_reporter
trigger: TR-06
loops: LP-03, LP-20
scope: company
signals: SG-002, SG-003, SG-005
---
Runs for each acquired Company whose owner has agreed a reporting scope.
Hands operating_reporter the Company, its agreed measures, recipients, materiality rules and the reporting week.
Material means a change that meets the owner's agreed rules: cash, runway, revenue, cost or unusual transactions.
Never move money or change anything in the Company's systems. Never report on individual employees' activity.
Never send the report outside Halience without approval. A late or failed data source is stated at the top.
