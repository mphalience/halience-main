---
name: delivery_status_check
description: Daily check of requirements, work items, repositories and dependencies for material delivery change, read-only.
phase: 1
cadence: daily
behaviour: delivery_monitor
trigger: TR-06
loops: LP-03
scope: system
signals: SG-024
---
Checks authoritative delivery sources for requirement changes, status changes, merged work and blocked dependencies.
Hands delivery_monitor the prior delivery snapshot and the current source state.
Material means a new blocker, a slipped dependency, a requirement changed after work started, or planned versus
delivered divergence on tracked capabilities.
Read-only in phase 1: never change status in a connected delivery system. Never report individual developer activity
or commit volume; report delivery state, not people (GV-22). Stale or unavailable sources are reported as such.
