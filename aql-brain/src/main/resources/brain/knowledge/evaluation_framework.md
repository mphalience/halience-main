---
name: evaluation_framework
description: First-cut AQL evaluation method covering dimensions, release gates, production sampling, regression, verdicts and independence
phase: 1
status: working
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-10, GV-11, GV-12
---
# Evaluation framework (working)

Capabilities have pre-release evaluation criteria and production monitoring proportionate to risk (`GV-12`). AQL
owns the evaluation mechanics (`PC-16`, `LP-05`, `OUT-16`); domains define what good means. This is a first-cut
method; suites and thresholds remain to be set per capability.

## What is evaluated
Generated outputs, capabilities, sources, models and workflows. `PC-16` judges AQL quality; it never judges the
business merit of a decision (that is `PC-06` within a domain loop).

## Evaluation dimensions (applied as relevant)
1. **Grounding**: material claims cite governed evidence that actually supports them (`GV-01`).
2. **Claim honesty**: observed, derived and interpretive claims correctly classified (`GV-02`).
3. **Confidence and sufficiency**: stated confidence and sufficiency are justified; low support changed behaviour (`GV-03`).
4. **Failure honesty**: failures, degradation and unknowns are reported, never claimed as success (`GV-07`).
5. **Permission and authority adherence**: no access beyond scope, no action beyond authority, approvals respected.
6. **Regulated perimeter**: no regulated advice; financial outputs labelled indicative with assumptions (`GV-15`, `GV-17`).
7. **Personalisation honesty**: preferences did not rewrite facts (`GV-14`).
8. **Materiality quality**: no noise, no missed material change (`GV-13`; Watch outcome family).
9. **Persona and channel rules**: voice, brand and channel constraints respected.
10. **Envelope completeness**: output envelope fields present and consistent with the summary.

## Verdicts
Each evaluation returns pass, degrade or block, with specific defects, evidence for each defect, criteria version,
subject version and evaluator (`OUT-16`, `SG-025`). Fail on a high-consequence capability escalates (`PC-17`).

## When evaluations run
- **Pre-release gate**: a capability or primitive version passes its suite before it runs for real users.
- **Production sampling**: a sample of live outputs is evaluated daily; higher-consequence capabilities are sampled
  more heavily, and every consequential action output is checked.
- **Version change regression**: any model, prompt, rule, methodology or primitive change (`SG-026`) triggers
  regression against the reference set before and after.
- **User challenge**: corrections and challenges (`SG-029`) are evaluated with the original snapshot preserved.
- **Health**: source, model and integration health feeds fallback and degradation (`PA-032`, `PA-033`).

## Control responses (`LP-05`)
Pass → release or continue. Degrade → bounded fallback with disclosure. Block → stop release or execution and route
remediation (`CH-11`); re-evaluate before recovery.

## Independence
Criteria and version are explicit. An agent does not certify its own output where independence is needed; use a
different evaluator configuration, a deterministic check or a human. Criteria are not tuned to make a result pass.

## Domain evaluation frameworks
Domain methodologies (dimensions, criteria, benchmarks, thresholds, weights, evidence and sufficiency rules) are
owned by domain experts. Not every criterion is scored: methods include metrics, qualitative judgement, binary
eligibility rules, benchmark comparison, thresholds and weighted scoring. Investment, business performance,
financial health and portfolio frameworks are in workshop discovery; see investment_opportunity_evaluation,
business_performance_framework and financial_health_framework.

## Open implementation items
- Evaluation suites and reference (gold) sets per capability and per Prompt Activity.
- Pass, degrade and block thresholds per dimension and consequence level.
- Sampling rates and the minimum Phase 1 operations surface.
- Which evaluators are deterministic, model-based or human for each dimension.
- Graduation criteria for relaxing human approval on a capability.
