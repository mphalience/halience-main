---
name: version_change_regression
description: Daily regression evaluation of capabilities affected by model, prompt, rule, methodology or configuration version changes.
phase: 1
cadence: daily
behaviour: output_evaluator
trigger: TR-06
loops: LP-05
scope: system
signals: SG-026, SG-025
---
Checks for version changes since the last run (SG-026), including primitive edits applied through the one door.
Hands output_evaluator each affected capability with old and new versions and its regression reference set.
Material means a regression against the reference set or release criteria on any affected capability.
On regression, route to degrade, block or rollback according to consequence; never leave a regressed consequential
capability running silently. Never approve or apply a fix from this routine.
