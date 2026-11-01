---
name: loop_chains
description: Representative end-to-end loop compositions CH-01 to CH-12 with composition, branches, return points and end state
phase: 1
status: canonical
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
---
# Loop chains

Chains test how loops compose. They are representative, not mandatory workflows. A loop may invoke another, pause,
receive a governed result or state and resume. Approval, evaluation, attention and agent execution are invoked
only when their conditions apply.

- **`CH-01` Research → decision → action → learning** (P1 contracts, P2 orchestration). `LP-14` → `LP-10` → `LP-15`
  if required → `LP-09` and/or `LP-17` → `LP-06`. Branches: evidence gap, `LP-14` iterates; no approval needed,
  skip `LP-15`; human execution uses `LP-09` without `LP-17`. Return: `LP-15` returns authorised/rejected state to
  the caller, not to execution. End: decision lineage preserved through outcome and learning.
- **`CH-02` Monitoring → materiality → attention → response** (P1 monitoring, P2 attention/work). `LP-03` → `LP-08`
  if involvement needed → `LP-09` → `LP-17` if delegated → `LP-06`. Branches: not material, hold; material but no
  involvement, update state without Attention; approval-required response, `LP-15` first. End: handled or held,
  monitoring continues.
- **`CH-03` Companion request → intelligence → bounded action → response** (P1). `LP-02` → `LP-14` when deeper
  evidence is needed → `LP-15` if required → `LP-09`/`LP-17` → back to `LP-02`. Branches: simple answer stays in
  `LP-02`. Return: every invoked loop returns to `LP-02` so conversation and referents persist. End: grounded
  response or action result; interaction state persists.
- **`CH-04` Capture → organisational knowledge → later remediation** (P2, P1 interface-ready). `LP-07` → `LP-15`
  where policy requires → Knowledge; later change → `LP-16` → `LP-07` if re-promotion is needed. Branches: private
  material is not promoted; conflict is reconciled. End: knowledge current, restricted or superseded; uses remediated.
- **`CH-05` Supervised GTM lifecycle** (P1). `LP-04` composes `LP-14` → `LP-05` → `LP-15` → publish → `LP-03` →
  `LP-06`. Branches: quality failure, `LP-05` blocks or degrades; below approval threshold, skip `LP-15` only where
  policy permits. Return: specialists return state to `LP-04`, which owns the lifecycle. End: published, held or
  stopped activity with measured outcomes.
- **`CH-06` Investment opportunity → decision → outcome** (P2). `LP-18` composes `LP-14` → `LP-10` → `LP-15` →
  transact/stop → `LP-06`. Branches: material new evidence after approval needs reapproval; `LP-05` evaluates
  system quality only, never investment merit. End: governed invest, stop or hold with preserved basis.
- **`CH-07` Venture creation → launch → intervention/learning** (P2). `LP-19` composes `LP-14` → `LP-09` → `LP-15`
  at gates → launch → `LP-03` → `LP-08` → `LP-06`. Branches: agent work runs as `LP-17` under `LP-09`. End: venture
  advances, changes, scales or stops with evidence preserved.
- **`CH-08` Operating health → exception → coordinated response** (P3, foundations earlier). `LP-20` → `LP-03` →
  `LP-08` → `LP-14` if diagnosis needs evidence → `LP-09` → `LP-17` if delegated → `LP-06`. Branches: consequential
  response, `LP-15`. End: issue resolved, managed or escalated with learning captured.
- **`CH-09` Strategy → plan → execution → replan** (P2). `LP-11` → `LP-14` → `LP-10` → `LP-15` where governed →
  `LP-09` → `LP-03` → `LP-08` → `LP-06` → resume `LP-11`. Replanning uses current evidence without rewriting the
  prior decision basis. A deliberate loop-back, not a circular dependency.
- **`CH-10` Local learning → cross-product learning → governed knowledge** (P2/P3). `LP-06` → `LP-13` → `LP-05` →
  `LP-07` where shared knowledge is warranted. Branches: insufficient evidence, retain locally; privacy boundary
  fails, do not share; validation fails, hold or reject.
- **`CH-11` Evaluation failure → remediation/recovery** (P1/P2). `LP-05` → `LP-17` or `LP-09` for remediation →
  `LP-15` if the remediation needs approval → back to `LP-05` → `LP-06`. Evaluation owns the control decision;
  execution owns remediation; `LP-05` re-evaluates before release. End: passes, stays degraded or blocked, or escalates.
- **`CH-12` Information correction/reclassification → downstream remediation** (P2, P1 provenance foundations).
  `LP-16` → trace affected loops/artefacts → `LP-07` where knowledge is affected → `LP-15` where reapproval is
  needed → owning loop resumes with corrected state. `LP-16` does not rerun everything; it hands corrected or
  restricted state to the owning loop to recompute or resume.
