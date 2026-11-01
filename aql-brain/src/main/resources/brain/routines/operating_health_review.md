---
name: operating_health_review
description: Monthly operating health review per company across finance, risk, capacity, compliance, suppliers and knowledge, without employee surveillance.
phase: 3
cadence: monthly
behaviour: operating_health_analyst
trigger: TR-06
loops: LP-20, LP-03
scope: company
signals: SG-002, SG-005, SG-024, SG-028
---
Runs for each company in scope of operating health management.
Hands operating_health_analyst the company's operating evidence for the month and the agreed health measures.
Material means an exception that passes configured rules in any health area.
Never use private Halie context, activity volume or behavioural telemetry, and never rank or profile individuals.
Never act on a response from this routine; consequential responses go through approval.
