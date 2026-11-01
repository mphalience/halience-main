---
name: ownership_reporting
description: Monthly ownership report on each held position, comparing performance with the frozen investment case and flagging material change.
phase: 2
cadence: monthly
behaviour: ownership_reporter
trigger: TR-06
loops: LP-18, LP-03, LP-06
scope: portfolio
signals: SG-005, SG-032
---
Runs for each held position in the Invest build.
Hands ownership_reporter the position, its frozen investment case, agreed measures and the reporting month.
Material means an investment case assumption drifting or breaking, or a measure crossing a configured rule.
Never rewrite the investment case in hindsight. Missing company reporting is a gap, not a zero. Hold, add or exit
decisions are never made here; they belong to the Investment team.
