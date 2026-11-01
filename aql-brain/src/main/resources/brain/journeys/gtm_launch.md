---
name: gtm_launch
description: Supervised GTM launch from market sensing through content creation, validation, approved publishing, measurement and calibration.
phase: 1
goal: Publish approved, grounded GTM content and learn from its measured outcomes.
loops: LP-04, LP-14, LP-05, LP-15, LP-03, LP-06
chain: CH-05
consumers: GTM, Halie
---
## sense_market
- title: Research audience, market and competitor signals for the brief
- behaviour: gtm_researcher
- loop: LP-14
- done_when: output
- approval: none
- why: Content must start from grounded evidence, not assumption.

## create_content
- title: Draft content and claims against the brief and approved claims
- after: sense_market
- behaviour: gtm_content_writer
- loop: LP-04
- done_when: output
- approval: none
- why: Produces unsent drafts that keep claims traceable to evidence.

## validate_content
- title: Validate claims, grounding, brand and regulated-perimeter rules
- after: create_content
- behaviour: gtm_content_validator
- loop: LP-05
- done_when: output
- approval: none
- why: Quality failures block or send content back before anyone is asked to approve it.

## approve_publish
- title: Human approval of the exact content and channel
- after: validate_content
- behaviour: approval_assessor
- loop: LP-15
- done_when: approval
- approval: required
- why: Consequential publishing stays human-approved in phase 1.

## publish
- title: Publish the approved version
- after: approve_publish
- behaviour: gtm_publisher
- loop: LP-04
- done_when: outcome
- approval: required
- why: Publishing executes only the exact approved version; changes need reapproval.

## measure_performance
- title: Measure performance over the agreed window and flag material change
- after: publish
- behaviour: gtm_performance_analyst
- loop: LP-03
- done_when: output
- approval: none
- why: Performance is judged against defined measures, never above factual accuracy.

## calibrate
- title: Record outcomes against the frozen content and recommendation
- after: measure_performance
- behaviour: gtm_outcome_calibrator
- loop: LP-06
- done_when: outcome
- approval: none
- why: Learning connects what was published to what happened without hindsight rewriting.
