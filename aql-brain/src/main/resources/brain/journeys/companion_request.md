---
name: companion_request
description: A companion request from intent through grounded response, optional research and bounded approved action, returning the result to the conversation.
phase: 1
goal: Give the user a grounded response or action result while interaction state persists.
loops: LP-02, LP-14, LP-15, LP-09, LP-17
chain: CH-03
consumers: Halie, Ask Settle
---
## classify_intent
- title: Classify the request and resolve its context
- behaviour: intent_classifier
- loop: LP-02
- done_when: output
- approval: none
- why: Starts the right behaviour with the right context and consequence class.

## research
- title: Gather deeper evidence when the answer needs it
- after: classify_intent
- behaviour: researcher
- loop: LP-14
- done_when: output
- approval: none
- why: Research gaps are filled with evidence, not guesswork; simple requests skip this.

## respond
- title: Respond and compose the surface
- after: research
- behaviour: halie
- loop: LP-02
- done_when: output
- approval: none
- why: The user gets a grounded answer with provenance and limits.

## route_action
- title: Route any requested action to the right capability
- after: respond
- behaviour: action_router
- loop: LP-02
- done_when: output
- approval: none
- why: Companion context never expands access or action authority.

## approve_action
- title: Approve a consequential action
- after: route_action
- behaviour: approval_assessor
- loop: LP-15
- done_when: approval
- approval: required
- why: Consequential actions need approval of the exact payload.

## execute_action
- title: Execute the approved action
- after: approve_action
- behaviour: action_executor
- done_when: outcome
- approval: required
- why: Execution is separate from recommendation and logged.

## return_result
- title: Return the result to the conversation and persist referents
- after: execute_action
- behaviour: halie
- loop: LP-02
- done_when: output
- approval: none
- why: Every invoked loop returns to the companion so the conversation stays coherent.
