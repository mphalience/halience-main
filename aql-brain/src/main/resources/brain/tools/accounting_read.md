---
name: accounting_read
description: Reads bank and accounting data for an acquired company, such as accounts, transactions, balances and ledgers, read-only.
phase: 2
kind: http
authority: read
consequence: none
approval: none
reversible: true
method: GET
base_url_env: HALIENCE_ACCOUNTING_API_URL
auth_env: HALIENCE_ACCOUNTING_API_KEY
---
## When to use
For Acquire work: onboarding an acquired company and producing its operating report from its bank and
accounting data.

## What it returns
Accounts, balances, transactions, ledger entries and period totals, with the source system, as-of time
and any sync gaps.

## What it never does
It is read-only. It never creates payments, journals or changes in the accounting system, and returns
data only for companies the caller is authorised for.
