---
name: prompt_activities
description: All 43 Phase 1 Prompt Activities PA-001 to PA-043 with prompt class, consumer, activity, authority level and output
phase: 1
status: canonical
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
---
# Prompt Activities

A Prompt Activity is a concrete Phase 1 use of a Prompt Class for one product or domain need. Activities make the
classes executable without redefining the taxonomy. Authority is the highest level the activity needs.

| Code | Class | Consumer | Activity | Authority | Output |
|---|---|---|---|---|---|
| `PA-001` | `PC-01` | Settle | Retrieve current Property/Area evidence for a lookup | Read | Grounded evidence set |
| `PA-002` | `PC-05` | Settle | Derive property fundamentals, costs and intelligence dimensions | Compute | Derived intelligence |
| `PA-003` | `PC-06` | Settle | Directional Price Verdict from valuation vs asking price | Compute | Price Verdict + basis |
| `PA-004` | `PC-07` | Settle | Compute Property Score and Area Score | Compute | Score + breakdown + confidence |
| `PA-005` | `PC-08` | Settle | Personalised Settle Score and priorities | Compute | Personalised fit + breakdown, incl. affordability where sufficiently known |
| `PA-006` | `PC-09` | Settle | Compare Properties, Areas or reference baselines | Compute | Comparison + meaningful differences |
| `PA-007` | `PC-10` | Ask Settle | Explain a fact, risk, score, verdict, document item or next step | Read | Contextual grounded answer |
| `PA-008` | `PC-11` | Settle | Recommend questions/actions arising from property intelligence | Recommend | Prioritised recommendations |
| `PA-009` | `PC-13` | Settle Watch | Detect material Property/Area intelligence change | Monitor | Material change + reason |
| `PA-010` | `PC-13` | Settle Watch | Decide whether score movement is a consequence of external change | Monitor | Attributed score delta |
| `PA-011` | `PC-02` | Settle | Extract facts, terms and entities from an uploaded document | Compute | Structured document evidence |
| `PA-012` | `PC-04` | Settle | Reconcile document evidence against canonical/public evidence | Compute | Agreement/conflict/unknown set |
| `PA-013` | `PC-10` | Ask Settle | Explain a document or contradiction | Read | Grounded explanation |
| `PA-014` | `PC-12` | Settle | Draft communication from Process/property evidence | Draft | Draft communication |
| `PA-015` | `PC-12` | Settle Reveal | Generate report narrative/artefact from current intelligence | Create | Dated Report |
| `PA-016` | `PC-12` | Settle | Generate or pre-populate Sales Pack or Tenancy Pack | Create | Pack artefact + gaps |
| `PA-017` | `PC-14` | Settle | Generate/update next-step plan for Buy, Sell, Rent or Let | Plan | Plan + next actions |
| `PA-018` | `PC-11` | Settle | Prioritise what matters now in a Process/Relationship | Recommend | Priority list + reasons |
| `PA-019` | `PC-15` | Settle | Execute approved communication/action via connected tool | Execute | Action result + audit event |
| `PA-020` | `PC-17` | Settle; AQL | Escalate consequential, ambiguous or low-confidence case | Route | Human review / fallback route |
| `PA-021` | `PC-01` | Halie | Retrieve canonical requirements, decisions, docs and dependencies | Read | Answer + source links |
| `PA-022` | `PC-09` | Halie | Compare requirement state with issue-tracker, repository and technical evidence | Compute | Traceability/status delta |
| `PA-023` | `PC-13` | Halie | Detect blocker, stale status or material delivery change | Monitor | Material delivery signal |
| `PA-024` | `PC-10` | Halie | Explain current Halience surface/object and navigate to source | Read | Explanation / navigation target |
| `PA-025` | `PC-01` | GTM | Research audience, market, competitors, channels or claim evidence | Read | Research evidence set |
| `PA-026` | `PC-12` | GTM | Generate campaign/content asset from approved brief and evidence | Draft | Draft asset |
| `PA-027` | `PC-16` | GTM | Validate content for grounding, brand, privacy and commercial rules | Evaluate | Validation result + issues |
| `PA-028` | `PC-15` | GTM | Publish approved asset/campaign | Execute with approval | Published asset/result |
| `PA-029` | `PC-05` | GTM | Analyse campaign/content performance | Compute | Performance analysis |
| `PA-030` | `PC-18` | GTM | Connect content/campaign choices to observed outcomes | Learn | Outcome label / calibration record |
| `PA-031` | `PC-16` | AQL Operations | Evaluate generated output against grounding/quality criteria | Evaluate | Pass/fail/score + defects |
| `PA-032` | `PC-13` | AQL Operations | Monitor source/model/integration health | Monitor | Health signal |
| `PA-033` | `PC-17` | AQL Operations | Route failed/degraded capability to fallback or human | Route | Fallback / escalation |
| `PA-034` | `PC-18` | AQL | Capture observed Settle outcome against prior prediction/recommendation | Learn | Outcome-labelled event |
| `PA-035` | `PC-19` | Settle Explore; Ask Settle | Indicative affordability before a Property is chosen, and contextual mortgage/affordability for a selected Property | Compute | Free affordability calculation + transparent assumptions |
| `PA-036` | `PC-19` | Settle Reveal | Reveal affordability scenarios and sensitivity from financial, Property and True Cost inputs | Compute | Calculated scenarios/sensitivity for downstream analysis |
| `PA-037` | `PC-07` | Settle Reveal | Use affordability as a personal-fit dimension in Settle Score once sufficient | Compute | Settle Score + affordability contribution + breakdown |
| `PA-038` | `PC-11` | Settle | Budgeting, cost-reduction, tracking and property-finance actions in a Process/Relationship | Recommend | Prioritised financial-management actions |
| `PA-039` | `PC-10` | Halie | Continue a conversation using recent referents and active context | Read | Grounded answer + retained interaction state |
| `PA-040` | `PC-14` | Halience OS; Halie | Compose or recompose the canvas from authorised outputs using Attention, Work, Pulse and Team regions | Plan | Composable surface state + navigation targets |
| `PA-041` | `PC-03` | Halience OS; Halie | Resolve whether the interaction is with Halie or a person/message thread | Compute | Resolved interaction/channel state |
| `PA-042` | `PC-01` | Halience OS; Halie | Retrieve and surface an authorised canonical or working artefact with provenance/status | Read | Contextual artefact + source/status metadata |
| `PA-043` | `PC-17` | Halience OS; Halie | Route a bounded OS action (Open, Review, Ask Halie, approve, navigate) without implying broader authority | Route | Permitted target, or explicit denial/escalation |

## Notes
- Initiators vary (user, system, AQL, automation) but do not change the class.
- `PA-019` and `PA-028` are the only Phase 1 execution activities; both require approval or an authorised rule.
- `PA-036` calculates; interpretation of what the scenarios mean for the user belongs to downstream `PC-05`/`PC-08`.
- Halie activities (`PA-021` to `PA-024`, `PA-039` to `PA-043`) are read, compute, plan or route only in Phase 1.
