---
name: knowledge_capture
description: From captured or source material through processing, promotion proposal, review and approval to governed organisational Knowledge, with later remediation.
phase: 2
goal: Turn permitted captured material into approved organisational Knowledge, or hold it explicitly restricted.
loops: LP-07, LP-15, LP-16
chain: CH-04
consumers: Halie, Halience OS
---
## process_capture
- title: Process the capture into restricted, provenance-tagged source material
- behaviour: capture_processor
- loop: LP-07
- done_when: output
- approval: none
- why: Captured material is governed source material, not truth; consent is checked first.

## propose_promotion
- title: Assess eligibility and prepare the promotion proposal
- after: process_capture
- behaviour: knowledge_promoter
- loop: LP-07
- done_when: output
- approval: none
- why: Private or restricted material is not promoted by default; conflicts go to reconciliation.

## review_change
- title: Review the proposed Knowledge change and its impact
- after: propose_promotion
- behaviour: knowledge_change_reviewer
- loop: LP-07
- done_when: output
- approval: none
- why: Impact on consumers is understood before approval.

## approve_promotion
- title: Knowledge owner approves the exact proposed version
- after: review_change
- behaviour: approval_assessor
- loop: LP-15
- done_when: approval
- approval: required
- why: Knowledge promotion is consequential and owner-approved.

## write_knowledge
- title: Write the approved Knowledge through the governed path
- after: approve_promotion
- behaviour: action_executor
- loop: LP-07
- done_when: outcome
- approval: required
- why: The write is separate from approval and leaves an Action record.

## remediate_if_changed
- title: Remediate if the Knowledge is later reclassified or corrected
- after: write_knowledge
- behaviour: reclassification_remediator
- loop: LP-16
- done_when: outcome
- approval: none
- why: Applies only when classification or validity later changes.
