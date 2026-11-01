---
name: workspace_files
description: Reads and writes files in a bounded, versioned build workspace for coding and site-build work.
phase: 2
kind: builtin
authority: execute
consequence: medium
approval: policy
reversible: true
---
## When to use
For venture build work: writing code, configuration and site content inside the venture's workspace, and
running permitted build steps there.

## What it returns
File contents for reads; for writes, the changed paths and the workspace revision.

## What it never does
It never writes outside the bounded workspace, never deploys to production or publishes externally, and
never handles secrets beyond those the workspace provides.
