---
name: action_status
description: Reads an action's state - proposed, awaiting approval, approved, executed or failed - and whether a current approval binds exactly its payload.
phase: 1
kind: brain
authority: read
consequence: none
approval: none
reversible: true
---
## When to use
Before executing, to confirm an action is approved as it now stands. To answer "has this been approved or sent?".
After an execution, to read its recorded result.

## What it returns
The action's type, target and status, whether it needs approval under policy, and whether it is authorised now
(an approved approval whose hash matches the current payload). It also returns the approvals recorded against it
and the execution result, if there is one.

## What it never does
It never approves, executes or changes anything. "Authorised now: false" after an edit means the payload changed
since approval and reapproval is needed.
