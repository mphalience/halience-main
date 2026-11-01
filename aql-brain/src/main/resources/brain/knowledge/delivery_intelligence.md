---
name: delivery_intelligence
description: Halie delivery intelligence - tracing requirements to issue tracker and repository evidence, staleness and blockers, read-only in Phase 1
phase: 1
status: canonical
owner: Halie
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-07, GV-18, GV-19
---
# Delivery intelligence

Halie answers "where are we with X?" by tracing canonical requirements and decisions to delivery evidence: issue
tracker items, repository changes and technical documents (`PA-021`, `PA-022`, `PA-023`, `CX-16`, `SG-024`, `TR-11`).
Phase 1 is permission-aware read, explain, compare, monitor and navigate. Broader operational workflow authority is
deferred to Phase 2.

## Sources and authority
- **Requirements and decisions**: the canonical requirements documents and decision records. These are the
  statement of what must be true.
- **Issue tracker**: work items, their status, assignee, links and history. Authoritative for planned and in-progress
  work state.
- **Repository**: merged changes, branches, pull requests and tests. Authoritative for what is implemented.
- **Technical documents**: design and development detail.
Each source stays authoritative for its own data type. Halie does not copy them into a competing truth (`GV-19`).

## Traceability
A requirement's delivery state is a comparison (`PC-09`, `OUT-07`) between:
1. what the requirement says must be true;
2. the work items that claim to deliver it;
3. the repository evidence that it is implemented and tested.
The delta is classified as one of: delivered (all three agree), in progress, planned but not started, untracked (a
requirement with no work item), unevidenced (work item done, no repository evidence), or drifted (the implementation
or work item no longer matches the requirement). Missing links are evidence gaps, not assumptions.

## Staleness and blockers (`PA-023`)
- **Stale**: a work item whose status hasn't changed within its expected window, or a requirement edited after its
  work items were closed.
- **Blocker**: an explicit blocked status or link, or a dependency that is itself blocked or not started when its
  dependants are due.
- **Materiality**: a delivery change is material when it changes a requirement's delivery state, blocks a dependency,
  or moves a committed date. Routine status churn is not material. Only material delivery signals are surfaced.

## Failure honesty
- A connector that is unavailable or stale is reported as such (`GV-07`), distinct from "no work found".
- Permission is checked before retrieval (`GV-18`): an item the user can't see in the tracker or repository is not
  surfaced through Halie.
