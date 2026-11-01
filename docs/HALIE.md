# How Halie works

Halie is the assistant Halience staff talk to. It sits in front of AQL, the brain: Halie keeps the conversation going,
and AQL does the work. In Phase 1, Halie **reads, retrieves, synthesises, explains and navigates**. It keeps track of
the conversation, but it owns no facts of its own and has no special powers. Context never lets it see or do more
than the person it is talking to.

Halie is not a separate program. It is the `halie` behaviour (`aql-brain/src/main/resources/brain/behaviours/halie/halie.md`)
plus a few specialist helpers, all run by the AQL runtime. To change how Halie behaves, edit its Markdown, through
the one door if it is already live.

## One turn, end to end

```
POST /api/halie/conversations/{id}/messages   { "message": "Where are we with Watch?", "context": { ... } }
```

1. **Find the conversation** (`HalieService` → `ConversationService`). The conversation stores the active context
   (which venture, object or screen the user is on) and a list of recent referents (the things recently discussed,
   newest first). The user's message is saved.
2. **Build the context** (`ContextEnvelope`). This bundles who is asking, their roles, the trigger, the time, the
   active context, the referents, the last few turns, and anything the caller adds (e.g. the current screen). It is
   written into the operating context of the run. It narrows what Halie looks at; it never grants access.
3. **Classify the request before the big model runs** (`IntentClassifier`). A cheap, low-thinking behaviour
   (`intent_classifier`) works out what kind of job this is (which prompt classes, such as PC-10 Explain or PC-09
   Compare), which behaviour should take it, and how consequential it is. If it can't decide, or picks something
   that is off or not allowed for this user, the request falls back to `halie` itself.
4. **Run Halie** (`BehaviourRunner`). Halie runs the companion loop (LP-02):
   *context → intent → retrieve/reason → respond/compose → route → persist*.
   - It resolves "it", "that requirement" or "the second one" against the referents. If two things could match, it
     asks rather than guesses.
   - It answers simple questions directly from governed sources: `knowledge_search`, `evidence_search`,
     `artefact_open`, `delivery_read`, `journey_status`.
   - It hands specialised work to a delegate (see below) and folds the result back into the conversation.
   - It can recompose the user's canvas (`compose_canvas`) or send them to a source or screen (`navigate`).
   - Anything consequential becomes a proposed action plus an approval request (`propose_action`,
     `request_approval`). Halie can't execute anything itself.
   - When the evidence is weak, the stakes are high or something has failed, it escalates (`escalate`).
5. **Check the answer** (also `BehaviourRunner`). Halie returns a structured envelope: the answer plus each claim with
   its claim class (observed, derived or interpretive), evidence refs, confidence and sufficiency, together with
   known unknowns, failure state and any proposed actions. Then the runtime checks it deterministically:
   - **Grounding:** an "observed" claim with no evidence, or one citing evidence the run never retrieved, is downgraded
     and recorded as a known unknown.
   - **Routing:** insufficient evidence or a failure combined with a higher-consequence request is routed to human
     review or escalated. It is never silently passed off as fine.
6. **Save** the reply, a link to its stored output (with the behaviour revision and model tier), and the updated
   referents. The response carries the text, the full envelope and any grounding problems.

## Who Halie hands work to

Halie's `delegates:` names folders of specialist behaviours. Admin and later-phase folders are only offered when
allowed.

| Folder | Used for |
|---|---|
| `halie/` | Organisational knowledge (PA-021), artefacts with provenance (PA-042), explaining the current screen (PA-024), Halie vs People channel (PA-041), canvas composition (PA-040), bounded OS actions (PA-043) |
| `delivery/` | Requirement → issue tracker → repository traceability (PA-022) and blocker/stale/material change detection (PA-023) |
| `core/` | Research and comparison, reconciliation, analysis, judgement, recommendations, plans, drafts |
| `gtm/` | Supervised GTM: research, content, validation, human-approved publishing, measurement |
| `ops/` | Output evaluation, health, fallback, escalation, execution of already-approved actions |
| `admin/` | `primitive_editor`: Halie viewing and proposing changes to its own primitives (Halience admins only) |
| `investment/`, `venture/`, `acquire/`, `work/`, `learning/`, `knowledge_governance/` | Phase 2: off until `halience.brain.max-phase` is raised |
| `portfolio/`, `operations/` | Phase 3 |

Delegates get their own tools and authority. A delegate never inherits Halie's tools, and Halie never inherits a
delegate's.

## The rules Halie can't break

These are enforced by the runtime, not just written into the prompt:

- **Permission before the model sees anything.** Every retrieval tool checks the record's classification and scope
  against the user. Restricted items need the exact scope, and private items are visible only to their owner.
- **A Halie conversation is private to its owner.** Other staff and system routines can't read it. It is never used
  for reporting on colleagues (GV-22).
- **Separate channels.** A People/Message thread is a different conversation. Halie never posts into one, and human
  threads are never merged into a Halie conversation.
- **Least privilege.** Halie's authority is `route`. Its tools can read, compose, navigate, propose and escalate. It
  has no tool that sends, publishes or changes another system.
- **Approval is exact.** Execution needs a person to approve the exact payload (a content hash). Any change means
  approving again, and nobody approves their own request.
- **Honesty about failure.** An unconfigured source says "unavailable", never "nothing found". A failed run comes back
  as a failure. Halie may not say it has sent, saved or changed something unless a tool returned success.
- **Status of knowledge.** Retrieved material carries its status (canonical, approved, working or proposed) and its
  authoritative source. Something Halie can retrieve is not automatically organisational truth (GV-19).

## Halie editing itself

Halience staff can ask Halie for a new capability ("add a behaviour that summarises board packs"). The `admin` delegate
`primitive_editor` reads the relevant primitives and proposes the exact new Markdown with a reason, impact and risk.
That creates an Action record and an approval request; nothing changes yet. Another Halience admin approves, an admin
applies it (`POST /api/primitives/actions/{id}/apply`), and the new revision is live. Every revision is kept, and
`POST /api/primitives/rollback` restores an earlier one. A proposal that would break validation (an unknown tool, an
agent using a tool above its authority, a journey cycle) is refused before it reaches anyone.

## Where to change things

| To change... | Edit |
|---|---|
| Halie's job, steps and delegates | `brain/behaviours/halie/halie.md` |
| Halie's voice and persona | `brain/behaviours/skills/halie_voice.md` |
| The rules every answer follows | `brain/behaviours/skills/*.md` (grounding, failure_honesty, output_envelope...) |
| What Halie knows about the canvas, referents and delivery | `brain/knowledge/halience_os_canvas.md`, `interaction_state.md`, `delivery_intelligence.md` |
| Which models it uses | `halience.llm.thinking.medium` in `application.yml` (Halie is `thinking: medium`; the classifier is `low`) |
| How a turn is handled | `HalieService`, `IntentClassifier`, `BehaviourRunner`, `ConversationService` |

## API

| Call | Purpose |
|---|---|
| `POST /api/halie/conversations` | Start a conversation, optionally with an active context |
| `POST /api/halie/conversations/{id}/messages` | Send a message; returns the reply, envelope, output id and grounding problems |
| `GET /api/halie/conversations/{id}/messages` | History (owner only) |
| `PUT /api/halie/conversations/{id}/context` | Enter or leave a scoped Context (`{}` returns to All Halience) |

All calls need `X-Halience-Actor`, and optionally `X-Halience-Roles`, `X-Halience-Scopes` and `X-Halience-Tenant`.
This is a first-cut stub for a trusted gateway, to be replaced by the real identity provider.

## Phase 1 limits

Halie can't act on its own: it proposes actions and asks for approval. Delivery connectors (requirements, issue
tracker, repository) are an interface with no implementation yet, so `delivery_read` reports them as unavailable.
Proactive interventions, attention triage and richer cross-domain synthesis are Phase 2.
