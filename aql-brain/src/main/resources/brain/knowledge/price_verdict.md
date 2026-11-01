---
name: price_verdict
description: Working method for the directional Price Verdict - valuation evidence against asking price, bands, confidence and its place in the Property Score.
phase: 1
status: working
owner: Settle
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-01, GV-02, GV-03, GV-11, GV-15
---
# Price Verdict

Price Verdict is a directional judgement of where the asking price sits relative to valuation evidence (PA-003,
PC-06, OUT-04). It sits inside the Property Score price/value dimension (SG-007); it is not a fourth Score.

## Inputs
- Subject Property, resolved (CX-04).
- Current asking price, with source and date.
- Valuation evidence: AQL valuation output and its inputs (comparable sales, market evidence), with method version.
- Confidence and sufficiency of the valuation.

## Working method
1. Confirm the asking price is current and the valuation is for the same resolved Property.
2. Calculate the gap: (asking price - valuation) / valuation, as a percentage. This is arithmetic, done with the
   calculate tool, not judgement.
3. Map the gap to a directional band, for example: below valuation evidence; in line with valuation evidence;
   above valuation evidence. Band edges are an open item.
4. Weight the verdict by valuation confidence. A wide valuation range or thin comparable evidence lowers confidence;
   below sufficiency, return insufficient evidence rather than a verdict.
5. Record basis: valuation figure or range, asking price, gap, comparables used, method version, confidence, limits.

## Rules
- Directional, not a price prediction, a guarantee of value, or a formal valuation. Label it as such.
- Not regulated advice and not a negotiation instruction; recommendations about offers belong to PC-11 behaviours.
- A change in asking price, valuation evidence or verdict band can be a material price/value change (SG-007) and is
  assessed by Watch materiality, not alerted directly.
- Freeze the verdict, valuation and evidence snapshot so later sale outcomes can calibrate it without hindsight
  (Valuation and Listing outcome families, PC-18).

## Open items for Settle and SMEs
- Band names and edges, and whether bands vary by market or property type.
- Minimum comparable evidence and valuation confidence needed for a verdict.
- How the verdict maps into the price/value dimension score.
