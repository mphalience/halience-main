---
name: source_health_check
description: Hourly check of source and integration freshness and health, routing degraded sources to fallback and confidence handling.
phase: 1
cadence: hourly
behaviour: health_monitor
trigger: TR-06
loops: LP-05, LP-03
scope: system
signals: SG-002, SG-028
---
Checks each source adapter and integration for late refresh, unavailability, malformed data and degraded responses.
Hands health_monitor the expected refresh schedule, last successful read and recent error state per source.
Material means a source that consequential capabilities rely on is late, failing or degraded beyond its freshness
window. Operational incidents may route directly to operations without a user materiality gate.
Never treat a source failure as missing evidence or as a valid zero. Never silently swap in an unsafe substitute
source for a consequential task; route to fallback and let confidence fall.
