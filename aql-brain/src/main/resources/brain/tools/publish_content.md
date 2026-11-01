---
name: publish_content
description: Publishes an approved content asset to an approved channel on schedule and returns a publication receipt.
phase: 1
kind: brain
authority: execute_with_approval
consequence: high
approval: required
reversible: false
---
## When to use
Only to publish a content asset whose human approval is recorded for this exact version and channel.

## What it returns
A receipt with actor, approval reference, asset version, channel, publication time, public location and
result.

## What it never does
It never publishes without approval, never publishes a changed version under an earlier approval, and
cannot unpublish what others have already seen.
