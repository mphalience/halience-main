---
name: request_approval
description: Raises an approval request for an exact proposed action or change and version, routed to the approver required by policy.
phase: 1
kind: brain
authority: route
consequence: low
approval: none
reversible: true
---
## When to use
When a proposed action or change requires approval, or an approved subject has changed materially and
needs reapproval.

## What it returns
The approval request ID, the approver role or roles, and its state (pending).

## What it never does
It never records an approval decision. A request is not an approval, and the requester can never approve
their own request.
