---
name: journey_status
description: Returns the status of a journey and its milestones: done, in progress, blocked or not started, with dependencies and evidence.
phase: 1
kind: brain
authority: read
consequence: none
approval: none
reversible: true
---
## When to use
To see where a journey stands: which milestones are complete, which can start, which are blocked and why.
Planners, the companion and canvas composition use it.

## What it returns
The journey, its goal, and each milestone with key, title, state, the milestones it waits on, the
behaviour responsible, completion basis (output, approval, outcome or human) and timestamps.

## What it never does
It never marks a milestone complete or changes journey state.
