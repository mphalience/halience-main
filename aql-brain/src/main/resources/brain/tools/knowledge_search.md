---
name: knowledge_search
description: Searches governed knowledge and organisational material and returns items with status, owner, version and effective date.
phase: 1
kind: brain
authority: read
consequence: none
approval: none
reversible: true
---
## When to use
To find Knowledge: requirements, decisions, guides, methods, rules, definitions and playbooks, and AQL's
own reference knowledge. Use it to ground answers about how things work or what has been decided.

## What it returns
Matching items with knowledge ID, title, excerpt, scope, status (source, working, proposed, approved or
canonical), owner, version, effective date, review state and source refs. Superseded items are flagged.

## What it never does
It never promotes, edits or writes knowledge, and never returns items the caller is not permitted to see.
Being returned by a search does not make an item canonical.
