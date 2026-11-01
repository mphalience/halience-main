---
name: emit_signal
description: Emits a governed signal, such as a material change or health state, with old and new refs, cause, materiality and confidence.
phase: 1
kind: brain
authority: monitor
consequence: low
approval: none
reversible: false
---
## When to use
When monitoring has determined that a change is material, or an operational health state needs to reach
operations.

## What it returns
The event ID and the signal as recorded under the signal contract.

## What it never does
It never notifies a person directly; routing is done by the runtime. It never emits a raw source mutation
as a user-relevant change.
