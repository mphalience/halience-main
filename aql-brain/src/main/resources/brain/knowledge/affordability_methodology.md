---
name: affordability_methodology
description: Affordability methodology affordability-v0.1 - deterministic formulas, input classification, Explore and Reveal depth, sufficiency and regulated boundary.
phase: 1
status: working
owner: Settle
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-02, GV-08, GV-11, GV-14, GV-15, GV-17
---
# Affordability methodology (affordability-v0.1)

Affordability is a Calculate job (PC-19, PA-035, PA-036): a defined quantitative calculation from explicit inputs
and governed assumptions. It is deterministic and versioned. The runtime calculator executes the formulas below;
agents never compute these numbers in prose. Results are indicative information, not a lender decision.

## Inputs and classification
Every input and output is tagged with its provenance class (GV-17):
- known: from a governed source, such as a listed asking price or a governed rate assumption.
- user-provided: supplied by the user, such as income, deposit, commitments, chosen term or rate.
- assumed: a methodology default, labelled as an assumption, such as the income multiple band or stress add-on.
- derived: calculated from the above by a formula in this file.

Inputs (CX-19): gross annual income per applicant; deposit (amount or percentage); monthly commitments; selected
Property price where a Property is in context; mortgage term in years; annual interest rate; repayment type.

## Formulas (first cut)
Notation: P = price, D = deposit, L = loan, R = annual rate as a decimal, r = R / 12, n = term years x 12.

1. Deposit and loan from price
   - If deposit is a percentage d: D = P x d.
   - L = P - D. If D >= P, there is no loan and repayment outputs are zero; say so.
2. Loan to value: LTV = L / P, shown as a percentage to one decimal place.
3. Monthly repayment, capital and interest (annuity):
   M = L x r x (1 + r)^n / ((1 + r)^n - 1). If r = 0, M = L / n.
4. Monthly repayment, interest-only: M = L x R / 12. The capital L remains owed at the end of the term; say so.
5. Indicative borrowing range: household income x income multiple, where household income is the sum of applicants'
   gross annual incomes and the multiple band is 4.0 (low) to 4.5 (high). The band is an assumption of this
   methodology, not a lender rule; lenders apply their own criteria.
   Indicative buying range (before a Property is selected) = borrowing range + deposit amount.
6. Stress-rate scenario: recompute M at R + 0.03 (rate plus 3 percentage points). The add-on is an assumption.

Presentation: money to the nearest pound; rates to two decimal places; every figure labelled indicative.

## Commitments
Commitments are recorded and shown alongside results. In v0.1 they do not adjust the income multiple; that is an
open item. Where commitments are material relative to income, state that they may reduce what a lender offers.

## Depth by entitlement (CX-02)
- Explore: the useful free headline - indicative borrowing and buying range, or for a selected Property the loan,
  LTV and monthly repayment, with assumptions shown. Not degraded to the point of uselessness.
- Reveal: scenarios and sensitivity - rate steps, the stress scenario, deposit and term variations, interest-only
  comparison, and combination with true-cost intelligence. Calculate produces the numbers; Derive (PC-05) and
  Personalise (PC-08) interpret what they mean for the user.
- Settle Score: consumes the result as the affordability personal-fit dimension when sufficient (PA-037). It never
  changes Property Score or Area Score.

## Sufficiency
- Borrowing range: at least one income figure.
- Buying range: income plus deposit.
- Property repayment and LTV: price plus deposit, with rate and term supplied or a governed default labelled assumed.
- When inputs are missing, return what is calculable with its limits and request the rest. Never fabricate income,
  deposit or commitments, and never fabricate a Settle Score contribution.

## Regulated boundary (GV-15, GV-17)
Outputs never imply eligibility, approval, a guarantee, or personalised mortgage advice. Requests to recommend a
lender, product or regulated course of action are outside the perimeter and are routed or escalated.

## Privacy
Financial inputs are sensitive and purpose-bound (permission_consent_basis). Temporary calculation inputs are distinct
from persisted financial context; persistence needs the applicable permission. They are not shared learning data.

## Versioning
Each output carries the methodology version (affordability-v0.1) and the assumption values used. Any change to a
formula or default is a version change (SG-026) with regression evaluation. User input changes recompute (SG-033)
but are not Watch events; governed rate or assumption changes may recompute but need materiality before notification.

## Open items for Settle, Finance and SMEs
- Governed default rate and term values, and the rate/lender source strategy.
- Whether commitments, dependants or outgoings adjust the multiple band.
- Scenario set and step sizes for Reveal; final estimate labels and legal wording.
- Financial-context persistence rules.
