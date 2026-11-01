---
name: send_message
description: Sends an approved message or email to the approved recipients exactly as approved and returns a delivery receipt.
phase: 1
kind: brain
authority: execute
consequence: high
approval: required
reversible: false
---
## When to use
Only to send a communication whose approval is recorded and bound to this exact content and recipient list.

## What it returns
A receipt with actor, approval reference, recipients, payload ref, channel, timestamp and result.

## What it never does
It never adds recipients, alters content, or sends without approval. A sent message cannot be recalled.
