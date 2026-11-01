---
name: knowledge_staleness_review
description: Weekly review of governed Knowledge for passed review dates, superseded sources and effective-date changes.
phase: 1
cadence: weekly
behaviour: knowledge_change_reviewer
trigger: TR-06
loops: LP-03, LP-05
scope: system
signals: SG-031
---
Checks governed Knowledge items for passed review_by dates, source or effective-date changes and supersession.
Hands knowledge_change_reviewer each affected item with its owner, sources and consumers.
Material means Knowledge that consumers rely on is stale, superseded or no longer effective for its jurisdiction.
Never edit Knowledge from this routine; propose changes to the owner through the one door. Never treat working or
private material as a replacement for stale Knowledge.
