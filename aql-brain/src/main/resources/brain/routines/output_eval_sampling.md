---
name: output_eval_sampling
description: Daily sampling of production outputs for grounding, claim honesty and quality evaluation against versioned criteria.
phase: 1
cadence: daily
behaviour: output_evaluator
trigger: TR-06
loops: LP-05
scope: system
signals: SG-025
---
Samples recent production outputs per capability, weighted towards higher-consequence outputs.
Hands output_evaluator each sampled output with its evidence, method and prompt versions and the current criteria.
Material means a failure rate or failure type that crosses the capability's release threshold, or any failure on a
consequential output.
Evaluation records criteria and version with every result. Never let a capability certify its own outputs where
independence is needed. Never sample private content into shared evaluation sets without permission (GV-08).
