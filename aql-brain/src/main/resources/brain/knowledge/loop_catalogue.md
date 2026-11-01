---
name: loop_catalogue
description: The 20 AQL loops LP-01 to LP-20 with phase, core cycle, human and authority boundary, core prompt classes and loops each invokes
phase: 1
status: canonical
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
---
# Loop catalogue

Loops are recurring operating cycles. Each uses the relevant `LP-01` stages, adds its own purpose, state and
controls, and may invoke other loops and resume. Phase: 1 Foundation, 2 Generalisation, 3 Optimisation. "Core PCs"
are the prompt classes intrinsic to the loop; others are used conditionally.

## Phase 1 (Foundation)
- **`LP-01` Canonical intelligence.** Sources → Evidence → Knowledge → Intelligence → Judgement → Personalisation →
  Action → Outcomes → Learning. Boundary: authority checked separately from knowledge. Core PCs: `PC-01`, `PC-04`,
  `PC-05`, `PC-06`. Invokes: any specialist loop where research, control, action or learning is needed.
- **`LP-02` Companion interaction.** Context → intent → retrieve/reason → respond/compose → route → persist.
  Boundary: companion context never expands access or authority; owns continuity, not domain truth. Core PCs:
  `PC-01`, `PC-03`, `PC-10`, `PC-17`. Invokes: `LP-14`, `LP-09`, `LP-17`, `LP-15`, `LP-08`; every invoked loop returns to `LP-02`.
- **`LP-03` Monitoring & materiality.** Observe → recompute → assess → route/hold → observe. Boundary: materiality
  and permission gates before any interruption; decides whether change is material, not who must act. Core PCs:
  `PC-06`, `PC-13`. Invokes: `LP-08`, `LP-05`, `LP-06`.
- **`LP-04` Supervised GTM.** Sense → plan/create → validate → approve → publish → measure → learn. Boundary:
  human approval for consequential publishing. Core PCs: `PC-01`, `PC-05`, `PC-11`, `PC-12`, `PC-15`, `PC-16`,
  `PC-18`. Invokes: `LP-14`, `LP-15`, `LP-03`, `LP-05`, `LP-06`.
- **`LP-05` Evaluation & control.** Test → evaluate → pass/degrade/block → observe → calibrate. Boundary: human
  review where consequence requires; judges capability quality, not business merit. Core PCs: `PC-16`, `PC-17`.
  Invokes: `LP-15`, `LP-06`, `LP-17` where remediation is needed.
- **`LP-06` Outcome & calibration.** Freeze → observe → evaluate → calibrate. Boundary: learning writes bounded,
  purpose-approved, privacy-safe. Core PCs: `PC-18`. Invokes: `LP-05`, `LP-13`.
- **`LP-14` Research & comparison.** Frame → gather → reconcile → compare → identify gaps → iterate/handoff.
  Boundary: conclusions keep provenance and gaps; grants no action authority. Core PCs: `PC-01`, `PC-03`, `PC-04`,
  `PC-05`, `PC-09`. Invokes: `LP-05`; recurses on evidence gaps.
- **`LP-15` Approval & reapproval.** Propose → assess → review → approve/reject → handoff → reapprove. Boundary:
  approval never implies broader authority and never executes; returns authorised/rejected state. Core PCs: `PC-06`,
  `PC-15` (the proposed action is its subject), `PC-17`. Invokes: `LP-05`, `LP-16` where approved information changes.

## Phase 2 (Generalisation)
- **`LP-07` Knowledge promotion & reconciliation.** Capture → process → propose → review → approve → write/reconcile.
  Boundary: restricted material stays restricted; promotion needs a human/policy gate. Core PCs: `PC-02`, `PC-03`,
  `PC-04`, `PC-17`. Invokes: `LP-15`, `LP-16`, `LP-05`.
- **`LP-08` Attention & exception.** Observe → assess → route → respond → learn. Boundary: respects role, authority
  and non-surveillance; decides whose involvement is needed now. Core PCs: `PC-06`, `PC-08`, `PC-17`. Invokes:
  `LP-09`, `LP-17`, `LP-15`, `LP-06`.
- **`LP-09` Human & agent work.** Plan → delegate → execute → monitor → intervene → complete. Boundary: agents act
  within explicit scope; humans keep reserved decisions; owns the wider work graph. Core PCs: `PC-14`, `PC-17`.
  Invokes: `LP-17`, `LP-15`, `LP-08`, `LP-06`.
- **`LP-10` Decision-to-outcome.** Evidence → judgement → decision → action → outcome → learning. Boundary:
  decision-time state preserved separately from later outcomes. Core PCs: `PC-04`, `PC-05`, `PC-06`, `PC-11`,
  `PC-18`. Invokes: `LP-14`, `LP-15`, `LP-06`.
- **`LP-11` Strategy & planning.** Objectives → scenarios → prioritise → plan → execute → measure → replan.
  Boundary: material allocation stays human-owned unless delegated. Core PCs: `PC-05`, `PC-06`, `PC-11`, `PC-14`.
  Invokes: `LP-14`, `LP-10`, `LP-15`, `LP-09`, `LP-03`, `LP-08`, `LP-06`.
- **`LP-13` Cross-product learning.** Learn locally → sanitise → compare → validate → share → observe. Boundary:
  only approved privacy-safe signals cross product or company lines. Core PCs: `PC-04`, `PC-05`, `PC-06`, `PC-18`.
  Invokes: `LP-05`, `LP-07`, `LP-15`.
- **`LP-16` Information reclassification & remediation.** Reclassify → trace impact → remediate → propagate → verify.
  Boundary: never reveals restricted content; history stays traceable; targeted, not blanket rerun. Core PCs:
  `PC-03`, `PC-04`, `PC-17`. Invokes: `LP-05`, `LP-15`, `LP-07`.
- **`LP-17` Agent execution & recovery.** Scope → execute → observe → validate/handoff → recover/escalate → close.
  Boundary: agents cannot self-expand authority, scope or budget; owns one Agent Run. Core PCs: `PC-14`, `PC-15`,
  `PC-17`. Invokes: `LP-15`, `LP-05`, `LP-08`, `LP-06`.
- **`LP-18` Investment decision.** Source/screen → assess → diligence → value → recommend → decide → transact/stop
  → learn. Boundary: investment and transaction decisions stay governed human decisions. Core PCs: `PC-01`,
  `PC-04`, `PC-05`, `PC-06`, `PC-09`, `PC-11`. Invokes: `LP-14`, `LP-03`, `LP-10`, `LP-15`, `LP-05`, `LP-06`.
- **`LP-19` Venture creation & lifecycle.** Commission → discover/build → assess → launch → observe → intervene →
  scale/change/stop → learn. Boundary: stage gates and scale/stop decisions governed; thresholds not yet canonical.
  Core PCs: `PC-05`, `PC-06`, `PC-11`, `PC-14`, `PC-18`. Invokes: `LP-14`, `LP-09`, `LP-03`, `LP-08`, `LP-10`,
  `LP-15`, `LP-05`, `LP-06`.

## Phase 3 (Optimisation)
- **`LP-12` Portfolio value-creation.** Monitor → diagnose → intervene → measure → learn → reassess. Boundary:
  company interventions and hold/scale/exit stay governed human decisions. Core PCs: `PC-05`, `PC-06`, `PC-11`,
  `PC-13`, `PC-18`. Invokes: `LP-03`, `LP-08`, `LP-14`, `LP-10`, `LP-15`, `LP-09`, `LP-06`.
- **`LP-20` Operating health & management.** Observe → assess → diagnose → respond → measure → learn. Boundary:
  visibility role-scoped and never employee surveillance. Core PCs: `PC-05`, `PC-06`, `PC-11`, `PC-13`, `PC-18`.
  Invokes: `LP-03`, `LP-08`, `LP-14`, `LP-10`, `LP-09`, `LP-15`, `LP-05`, `LP-06`.

## Key boundaries between loops
- `LP-03` decides material change; `LP-08` decides whether and whose involvement is needed.
- `LP-06` calibrates effectiveness; `LP-10` preserves decision lineage.
- `LP-07` promotes into Knowledge; `LP-16` handles already-used information changing status.
- `LP-09` owns the work graph; `LP-17` owns one Agent Run.
- `LP-05` is technical and quality control, never a substitute for domain judgement such as investment merit.
- No separate data-ingestion or integration loop exists: retrieval sits in `LP-14`/`LP-01`, changing data in
  `LP-03`, corrections in `LP-16`, execution failure in `LP-17`/`LP-05`.
