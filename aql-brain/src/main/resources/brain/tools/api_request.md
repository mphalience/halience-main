---
name: api_request
description: Makes a request to an allow-listed external API. GET and HEAD are treated as reads; other methods follow approval policy and are logged.
phase: 1
kind: builtin
authority: execute
consequence: medium
approval: policy
reversible: false
---
## When to use
To call an allow-listed external system that has no dedicated tool. GET and HEAD requests are treated as
reads; POST, PUT, PATCH and DELETE change external state and follow the approval policy for that endpoint.

## What it returns
Status code, response body (truncated where large) and timing. State-changing calls also return an audit
receipt with actor, endpoint, payload ref and result.

## What it never does
It never calls endpoints outside the allow-list, never sends credentials in logs, and never makes a
state-changing call without the approval policy being satisfied.
