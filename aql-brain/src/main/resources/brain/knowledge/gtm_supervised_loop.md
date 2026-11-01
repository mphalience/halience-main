---
name: gtm_supervised_loop
description: The supervised GTM loop LP-04 - stages, the behaviour for each stage, human approval for publishing, and what may stay assisted or manual in Phase 1.
phase: 1
status: canonical
owner: GTM
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-01, GV-05, GV-06, GV-10, GV-16
---
# Supervised GTM loop (LP-04)

LP-04 runs repeatable go-to-market work from sensing through publishing, measurement and learning. Phase 1 consumer:
Settle GTM, with the loop designed to serve future ventures. LP-04 owns the lifecycle; specialist loops own reusable
mechanics (CH-05): research (LP-14), evaluation (LP-05), approval (LP-15), monitoring (LP-03), calibration (LP-06).

## Stages
1. Sense: audience, market, channel, demand and claim evidence (SG-021, PA-025, gtm_researcher). May remain
   assisted or manual in Phase 1.
2. Plan and create: opportunity and campaign planning (may remain assisted or manual), then a draft asset from an
   approved brief and evidence (PA-026, gtm_content_writer).
3. Validate: grounding, brand, privacy, regulated and commercial rules (PA-027, gtm_content_validator). A failure
   blocks or returns the draft for revision.
4. Approve: a named human approves the specific asset, channel and schedule (TR-09, LP-15). Approval covers only what
   was approved; a material change after approval needs reapproval.
5. Publish: the approved asset is published through publish_content (PA-028, gtm_publisher), logged with actor,
   approval, channel, payload and result (OUT-14, OUT-15).
6. Measure: performance against the defined measures and baseline (PA-029, gtm_performance_analyst, SG-023).
   A material performance change enters LP-03.
7. Learn: connect content and campaign choices to observed outcomes (PA-030, gtm_outcome_calibrator, OUT-18).

## Fixed rules
- Publishing always needs human approval in Phase 1 (GV-06, SG-022, CX-17). No policy threshold skips it.
- Performance never overrides factual accuracy, independence or trust (SG-023, GTM outcome family).
- Generated claims stay grounded in governed evidence (GV-01); claim class is kept through transformation (GV-02).
- Recommendation, draft and execution are separate authority levels (GV-05).
- Phase 1 captures outcomes and supports calibration; broad autonomous optimisation is deferred (GV-16).

## Exits
Research insufficient: iterate in LP-14. Quality failure: block or degrade in LP-05. Approval rejected: return to
create with the reasons. Each specialist returns its state to LP-04.
