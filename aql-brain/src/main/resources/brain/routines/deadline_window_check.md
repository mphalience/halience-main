---
name: deadline_window_check
description: Daily check of Process and Relationship deadlines, expiries and response windows, reprioritising what matters now for each user.
phase: 1
cadence: daily
behaviour: process_prioritiser
trigger: TR-06
loops: LP-03
scope: user
signals: SG-016, SG-017
---
Checks active Processes and Relationships for milestones, expiries, renewals and response windows approaching or
passed.
Hands process_prioritiser the Process or Relationship state, dates and their sources.
Material means a deadline within its configured warning window, a passed deadline, or an expiry with consequence.
A time trigger does not imply any external change. Never act or send on the user's behalf; produce priorities with
reasons. Dates of uncertain source are flagged as uncertain, not presented as fixed.
