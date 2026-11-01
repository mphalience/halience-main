---
name: gtm_performance_window
description: Weekly close of GTM performance windows, measuring published content and campaigns against defined outcomes and flagging material change.
phase: 1
cadence: weekly
behaviour: gtm_performance_analyst
trigger: TR-06
loops: LP-04, LP-03, LP-06
scope: venture
signals: SG-022, SG-023
---
Checks which published assets and campaigns have completed a measurement window this week.
Hands gtm_performance_analyst the asset, its approved version, the window and the defined measures.
Material means a defined outcome moved beyond its expected range, or a claim issue was reported.
Growth outcomes never override factual accuracy or trust: a well-performing asset with a claim problem is flagged.
Never republish, pause or change a campaign from this routine; recommendations go through approval.
