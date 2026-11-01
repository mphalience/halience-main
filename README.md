# Halience

AQL is the Halience brain. Halie is the chatbot that fronts it. This repository holds a first cut of AQL, built
against the AQL Phase 1 Execution Spec. It also holds the two LLM libraries the brain runs on.

| Module | What it is |
|---|---|
| `halience-llm-router` | Routes LLM requests across providers, with fallback, capability-aware adaptation and cost ordering. Credentials come from `HALIENCE_<PROVIDER>_API_KEY` (falling back to the provider's usual variable). |
| `halience-agent-loop` | The agent loop: planning, tool calls, sub-tasks, delegation, history compression, cost/time bounds, structured answers. |
| `aql-brain` | The brain: a Spring Boot app that loads, validates, governs and runs the Markdown primitives. |

Java 25, Maven, Spring Boot 4.1, Spring Data JPA (H2 locally, PostgreSQL via the `postgres` profile).

## Build and run

```
mvn install                                   # builds all three modules and runs their unit tests
java -jar aql-brain/target/aql-brain-0.1.0-SNAPSHOT.jar
```

Set at least one key, e.g. `HALIENCE_ANTHROPIC_API_KEY` or `HALIENCE_OPENAI_API_KEY`. The app starts without any keys:
behaviour runs then fail honestly, and the governance endpoints still work. For web search, set
`HALIENCE_TAVILY_API_KEY` or `HALIENCE_BRAVE_API_KEY`. For PostgreSQL, run with `--spring.profiles.active=postgres`
and set `HALIENCE_DATABASE_URL`, `HALIENCE_DATABASE_USER` and `HALIENCE_DATABASE_PASSWORD`.

Identity is a first-cut stub. Callers pass `X-Halience-Actor`, `X-Halience-Roles` (e.g. `admin`),
`X-Halience-Scopes` and `X-Halience-Tenant` headers, which are expected from a trusted gateway. Replace this with the
real identity provider before exposing AQL.

## The brain is Markdown

Everything the brain does is defined in `aql-brain/src/main/resources/brain/`, under the five primitive types:

| Folder | Primitive | Count |
|---|---|---|
| `behaviours/<area>/` | **Behaviours**: one job each (an agent). Front matter gives the prompt classes, activities, loops, authority, outputs, skills, knowledge, tools and `thinking: low \| medium \| high`. | 74 |
| `behaviours/skills/` | Shared conduct included in full in every agent that lists it: grounding, claim honesty, failure honesty, permission and authority, privacy and non-surveillance, the output envelope, the voices of Halie and Ask Settle. | 17 |
| `knowledge/` | What is true or how a method works: the spec's vocabulary and contracts, plus Settle, GTM and later-phase methods. Each file has a status: `canonical`, `working` or `proposed`. | 52 |
| `journeys/` | Dependent milestones toward one goal (a DAG). These cover the Loop Chains, the Settle Buy/Sell/Rent/Let processes, and Build / Acquire / Invest. | 18 |
| `routines/` | Regular checks on a cadence that run a behaviour (Watch sweep, source health, delivery status, eval sampling...). | 15 |
| `tools/` | Actuators with their authority, consequence and approval mode. `brain` tools are Java handlers, `builtin` tools come from the agent loop, and `http` tools are generic API reads. | 33 |

The behaviour areas are `halie`, `core`, `delivery`, `ops`, `admin`, `settle` and `gtm` for Phase 1. Phase 2 adds
`knowledge_governance`, `work`, `learning`, `investment`, `venture` and `acquire`; Phase 3 adds `portfolio` and
`operations`. `knowledge/primitive_authoring.md` describes every front-matter key.

**Thinking levels.** `thinking` maps to an ordered list of models per level in `application.yml` (`halience.llm.thinking`).
Models whose provider has no key are skipped, and the router then chooses at the same depth.

**Phases.** `halience.brain.max-phase` (default 1) decides what runs. Later-phase primitives load and validate but
cannot run, be delegated to, or be offered as tools. A Phase 1 journey can contain later-phase milestones; they are
skipped until enabled.

**The one door.** Halie (via `admin/primitive_editor`) or a person proposes an exact new version of a primitive. A
Halience admin who did not request it approves that version, and an admin applies it. Each step leaves an Action
record. The approval is bound to a hash of the payload, so a changed proposal needs reapproval. Every applied version
is kept, and any primitive can be rolled back. The starters only seed an empty store, or refresh primitives nobody has
edited. A proposal that would break validation is refused.

## Halie

[docs/HALIE.md](docs/HALIE.md) explains how Halie works: a turn end to end, who it delegates to, the rules the
runtime enforces, how it edits its own primitives, and where to change things.

## How a request runs

1. **Classify**: `BrainService` → `IntentClassifier` runs the low-thinking `intent_classifier` behaviour to pick the
   prompt class and target behaviour. If that fails, it falls back to the consumer's companion: Halie, or Ask Settle
   inside Settle.
2. **Run**: `BehaviourRunner` records an Agent Run and builds the agent (`BrainAgents`): instructions, skills,
   on-demand knowledge, gated tools and delegates. It runs the agent with the context envelope and asks for the
   output envelope (claims with claim class, evidence refs, confidence and sufficiency; known unknowns; failure state;
   proposed actions; route).
3. **Govern**: a deterministic grounding check catches observed claims with no evidence and evidence the run never
   retrieved. `SufficiencyRouter` routes by sufficiency, confidence, consequence and failure. The output is persisted
   with behaviour revision and method versions.
4. **Act**: `ActionService` and `ApprovalService` keep recommendation, preparation, approval and execution separate.
   Execute tools run only an approved action whose payload hash still matches. Without a connector, an action goes to
   the outbox and says so.

Other deterministic pieces: the affordability calculator (`affordability-v0.1`, PC-19), the score engine
(affordability only ever counts toward the Settle Score), the materiality gate (raw events never notify, and user
edits are never Watch changes), the Signal Contract validator, hindsight-safe outcome capture with frozen snapshots,
permission checks (private Halie context is visible only to its owner), canvas composition and bounded navigation.

## Tests

Unit tests only. `aql-brain` has 125 tests, including:
- `BrainPrimitivesTest`: the shipped primitives validate with no errors.
- `SpecCoverageTest`: every PA-001…PA-043 has a Phase 1 behaviour. Every Prompt Class, Loop and Loop Chain is
  realised, every governance control is enforced or named, every output, context, signal and trigger code is
  described, and all 19 outcome families are listed.
- `BehaviourRunnerTest`: real shipped behaviours run through the real agent loop against a scripted mock model, with
  no network.

## Known gaps in this first cut

- Model access is through the router's existing providers. AWS Bedrock (per the technical slides) still needs its own
  router adapter.
- Delivery connectors (requirements, issue tracker, repository) and outbound connectors (email, publishing) are
  interfaces (`DeliverySource`, `Connector`) with no implementations yet.
- Evidence and knowledge search is keyword `LIKE`; semantic retrieval (the agent loop's vector search) is not wired in.
- Health telemetry is in memory. Background delegates run inline.
- Not yet represented: valuation production, Area intelligence derivation and true-cost (R36) knowledge.
- Domain methodologies (score weights, thresholds, investment criteria) are `working` or `proposed` knowledge awaiting
  SME input, as the spec intends.
