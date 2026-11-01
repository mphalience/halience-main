---
name: health_read
description: Reads current health telemetry for capabilities, sources, connectors and models - healthy, degraded, failed or unknown, since when.
phase: 1
kind: brain
authority: read
consequence: none
approval: none
reversible: true
---
## When to use
To monitor source, model and integration health (PA-032). To choose a fallback for a failed or degraded capability
(PA-033). To explain honestly to a user why something is unavailable.

## What it returns
For each capability with telemetry: its state (none meaning healthy, degraded, failed or blocked), a detail line,
since when it has been in that state, and when it was last seen.

## What it never does
It never treats missing telemetry as healthy. A capability with no telemetry has unknown health, and that is what
it reports.
