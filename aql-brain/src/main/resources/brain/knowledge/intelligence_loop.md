---
name: intelligence_loop
description: The canonical LP-01 intelligence loop, its nine stages, what is not a stage, and how specialist loops use and compose with it
phase: 1
status: canonical
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-01, GV-02, GV-05
---
# The canonical intelligence loop (`LP-01`)

Sources → Evidence → Knowledge → Intelligence → Judgement → Personalisation → Action → Outcomes → Learning

## Stages
1. **Sources**: originating material or systems. Access, licence, privacy and jurisdiction apply before use.
2. **Evidence**: governed observed or extracted claims with provenance, freshness, confidence and conflict state.
3. **Knowledge**: governed reusable information for an authorised scope.
4. **Intelligence**: derived or modelled results, with method version, assumptions and confidence.
5. **Judgement**: conclusions against explicit criteria, with an inspectable basis.
6. **Personalisation**: applying permitted context to set significance, fit or priority without rewriting facts.
7. **Action**: proposed, prepared or executed operations. Authority is checked separately from knowledge.
8. **Outcomes**: observed or inferred results linked to the frozen prior state they evaluate.
9. **Learning**: bounded, purpose-approved calibration and lessons.

## What is not a stage
- **Context** scopes every stage as an input; it is not itself a stage and cannot expand permission or authority.
- **Analysis** is a human-facing expression of Intelligence plus Judgement, not a canonical stage.
- **Recommendation** and **Decision** are governed objects that sit around the loop. A Recommendation may remain
  transient; a Decision is preserved through `LP-10`.

## Rules carried through the loop
- Each stage keeps its epistemic status: Evidence is not Intelligence; Intelligence is not Judgement; Judgement is
  not a Decision; knowing or recommending never implies authority to act.
- Recommendation, preparation and execution are separate authority levels (`GV-05`).
- Failure, uncertainty and known unknowns stay explicit at every stage (`GV-07`).
- Prior state is frozen before outcomes are used for evaluation, so later information cannot rewrite what was known.

## Execution model
A prompt may be explicit natural language, a structured system instruction, an event-driven evaluation, a
scheduled job or an internal AQL reasoning task. Not every prompt is user-visible. User, automation, AQL and
external-event initiators share the same Prompt Class taxonomy; initiator and trigger are attributes, not classes.

## How specialist loops relate to `LP-01`
A specialist loop may use `LP-01` stages directly, consume their outputs, govern transitions between them, or
return Actions, Outcomes and Learning to them. It need not use every stage. Examples:
- `LP-03` recomputes Intelligence and Judgement after a change before deciding materiality.
- `LP-14` iterates the early stages (Sources to Judgement) until a question is sufficiently supported.
- `LP-15` governs the transition from proposed to authorised Action.
- `LP-06` uses frozen prior state plus Outcomes to produce Learning.

## Composition rule
A loop may invoke another loop, pause while it runs, receive a governed result or state, and resume. Approval,
evaluation, attention and agent execution are invoked only when their conditions apply. See loop_catalogue and
loop_chains.
