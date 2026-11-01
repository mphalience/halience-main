---
name: settle_scores
description: Structure of the three Settle Scores (Property, Area, Settle), their inputs, confidence and sufficiency rules, and the open SME items on dimensions and weights.
phase: 1
status: working
owner: Settle
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-01, GV-03, GV-10, GV-11, GV-14
---
# Settle Scores

Settle has exactly three top-level Scores (Summary, What is fixed). Each is a transparent score with a breakdown,
a confidence state and a methodology version (PC-07, OUT-05). The structure below is a working first cut; exact
dimensions, weights and formulas are Settle domain and SME decisions.

## Property Score (PA-004)
- What it measures: the quality and value of the Property itself, from governed evidence.
- Indicative dimension groups: condition and fundamentals; running and ownership cost; risk (hazards and
  applicability); price/value. Price Verdict sits inside the price/value dimension (see price_verdict).
- Inputs: Property evidence and derived Property intelligence (PA-002). Never user preferences, never affordability.

## Area Score (PA-004)
- What it measures: the quality of the Area around the Property, from governed area evidence.
- Indicative dimension groups: amenity and access; environment and risk; community and services; market context.
- Inputs: Area evidence only. Never user preferences, never affordability.

## Settle Score (PA-005, PA-037)
- What it measures: personal fit between this user or Household and this Property and Area.
- Inputs: explicit user priorities, must-haves and weighted nice-to-haves, places and reference baseline (CX-07);
  Property and Area intelligence; affordability state when sufficient relevant financial context exists (CX-19).
- Affordability is a major personal-fit dimension of Settle Score. It is consumed as a calculated result from the
  affordability methodology, never recomputed inside scoring.
- Personalisation re-weights and contextualises; it does not rewrite the underlying evidence (GV-14, OUT-06).

## Fixed rules
1. Affordability never changes Property Score or Area Score.
2. No computed Settle Score before sufficient inputs. Below sufficiency, return the insufficient state and the
   inputs needed, not a guessed or default score. Never fabricate a financial contribution.
3. Missing evidence affects confidence, not a silently negative score. A dimension without sufficient evidence is
   shown as unknown and excluded or flagged according to method, with confidence lowered.
4. Confidence is not evidence sufficiency (Signal Contract): report both.
5. Score movement is a consequence of evidence change or explicit personal-context change, never a separate source
   signal (SG-009, SG-010, SG-012). Only external causes can surface through Watch.
6. Every score output carries method version, dimensions, confidence and personalisation state (OUT-05) and is
   auditable (GV-10, GV-11).

## Breakdown contract
For each dimension: value or band, contribution, evidence refs, confidence, and known unknowns. For Settle Score
also: which preferences drove the weighting and whether affordability was included, excluded as insufficient, or
not supplied.

## Open items for Settle and SMEs
- Final dimension list and definitions for each Score.
- Weights, aggregation formula and scale (numeric range or bands).
- Per-dimension sufficiency thresholds and the treatment of unknown dimensions.
- Settle Score affordability weighting and sufficiency rule (Summary, implementation-level).
- Confidence contract (numeric value plus band) and calibration against outcomes (Score outcome family).
