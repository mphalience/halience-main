---
name: authority_and_approval_matrix
description: First-cut authority ladder, consequence classes, approval modes, exact-payload approval binding and the Halie configuration progression
phase: 1
status: working
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-04, GV-05, GV-06, GV-10, GV-18
---
# Authority and approval matrix (working)

Permission says what may be accessed, used, retained or shared. Authority says what an actor may do. Approval
authorises one exact proposed action. None implies another, and capability never creates authority. AQL owns this
control pattern; business and product policy decide which actions exist, their consequence and who approves.

## Authority ladder (low → high)
read < compute < recommend < draft < create < plan < monitor < route < evaluate < learn < execute < execute_with_approval

| Level | Means | Example tools |
|---|---|---|
| read | Retrieve and return existing governed information | evidence_search, knowledge_search, artefact_open, delivery_read, journey_status, primitive_view, web_search, web_fetch |
| compute | Derive, calculate, score or record internal working results | calculate, score, record_evidence, record_intelligence, freeze_snapshot, conversation_state |
| recommend | Propose an action or change without effect | propose_action, primitive_propose |
| draft | Prepare unsent content | save_draft |
| create | Create an internal versioned artefact | create_artefact |
| plan | Compose plans or surface state | compose_canvas |
| monitor | Emit governed change signals | emit_signal |
| route | Direct work to a destination, human or approval | navigate, escalate, request_approval |
| evaluate | Record a quality or control verdict | record_evaluation |
| learn | Record outcomes and calibration | record_outcome |
| execute | Change shared or external state, or communicate | execute_action, send_message, api_request |
| execute_with_approval | Execute only with a human approval of the exact payload, every time | publish_content |

An agent's declared authority is the highest level it may exercise. It may list only tools at or below that level.
Each tool declares its own authority, consequence, approval mode and reversibility; the runtime checks all of them
at the point of use, not when the agent is loaded.

## Consequence classes
- **none**: no state change beyond the run's own working records (reading, computing).
- **low**: internal, reversible, visible mainly to the requester (save a draft, record evidence, create an internal artefact).
- **medium**: changes shared internal state or what others see, reversible with effort (route work, emit a signal,
  record an evaluation or outcome, apply a bounded configuration value).
- **high**: leaves Halience or reaches a customer, or is hard to reverse (send a message, publish content, write to
  an external system, change a primitive or methodology that affects outputs).
- **critical**: financial, legal or contractual commitment, transaction or investment step, permission or authority
  change, or irreversible deletion.

## Approval modes
- **none**: no approval needed; audit still applies where material.
- **policy**: a deterministic policy pre-authorises the action within explicit bounds (allow-listed targets, limits,
  value thresholds). Anything outside the bounds becomes required.
- **required**: a named authorised human approves the exact payload before execution.

Default mapping: none and low → none; medium → policy; high → required; critical → required, from an approver with
the matching authority. In Phase 1, consequential actions stay human-approved until a capability is explicitly
graduated (`GV-06`).

## Approval binding
- An approval binds approver, subject, exact action payload and version, applicable authority, conditions and expiry.
- The binding is a content hash of the canonical action payload (action type, target, content, parameters). At
  execution the runtime recomputes the hash; if it differs, the approval does not apply.
- Any material change to the payload, target, audience, evidence basis or consequence after approval requires
  reapproval (`LP-15`). Non-material formatting changes must not alter the canonical payload.
- Approval does not execute anything; execution is a separate authorised step that records actor, tool, before/after
  and result. Approval never broadens scope beyond the exact subject.
- A rejection ends that payload; a changed proposal is a new subject.

## No self-approval, no self-expansion
- No actor approves its own proposal. An agent never approves anything; another agent's endorsement is not approval.
- No agent, Halie included, can increase its own or another agent's scope, tools, authority, budget or autonomy.
  Changes to authority and approval configuration are proposed only, and approved by a Halience admin.

## Halie configuration progression
View → Propose → Review/Approve → Apply.
- **View**: inspect configuration, primitives and policy where permitted.
- **Propose**: a bounded amendment with current → proposed state, reason, impact, consequence and required approval.
  Creates no authority.
- **Review/Approve**: a Halience admin approves the exact version (hash-bound).
- **Apply**: the approved version takes effect with an Action record and revision, and can be rolled back.
Phase 1 baseline for Halie is View and Propose. Halie never approves and never applies its own proposals.

## Open implementation items
- Final consequence class per action type and per tool, and reversibility evidence.
- Approver roles per domain and consequence, including whether critical actions need two approvers.
- Monetary and value thresholds for policy mode.
- Approval expiry periods and the material-change rules per payload type.
- Graduation criteria and evidence for moving a capability from required to policy.
