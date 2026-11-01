---
name: primitive_authoring
description: How AQL primitives are written, every front-matter key, what the runtime enforces, thinking levels, phase gating and the one door
phase: 1
status: canonical
owner: AQL
effective_date: 2026-11-01
review_by: 2027-02-01
governance: GV-05, GV-06, GV-10, GV-11
---
# Primitive authoring

The brain's behaviour is defined in Markdown primitives that the runtime loads, validates and runs on an agent
loop. Halie reads and, through the one door, proposes edits to all five types, so a capability gained for one build
is available to every build.

## The five primitive types
- **Behaviours** (`behaviours/<folder>/<name>.md`): how to do a kind of work. Each runnable agent does one job.
  Skills (`behaviours/skills/<name>.md`) are shared conduct, included in full into every agent that lists them.
- **Knowledge** (`knowledge/<name>.md`): what the founders and team know and how a method works, so agents reason
  with it. Neutral reference text, loaded on demand.
- **Journeys** (`journeys/<name>.md`): a chain of dependent milestones toward one goal.
- **Routines** (`routines/<name>.md`): regular checks, such as reporting, that call the other four on a cadence.
- **Tools** (`tools/<name>.md`): actuators on a model call, such as brain operations, generic API calls and coding.

## Common front matter
First line `---`, then one `key: value` per line, closing `---`. No YAML lists, quotes, nested keys or `#` in values.
Lists are comma-separated on one line. Keys are lower_snake_case.
- `name`: matches `^[a-z0-9_]{1,64}$`, equals the file name without `.md`, unique within its type.
- `description`: one line, at most 160 characters, saying what it produces or contains.
- `phase`: 1, 2 or 3.

## Behaviour (agent) keys
- `thinking`: low, medium or high (see below).
- `prompt_classes`: the brain jobs performed (at least one `PC-` code).
- `activities`: `PA-` codes implemented (optional).
- `loops`: `LP-` codes participated in (at least one).
- `authority`: the highest ladder level the agent may exercise (see authority_and_approval_matrix).
- `outputs`: `OUT-` codes produced. `context`, `signals`, `triggers`, `governance`: relevant `CX-`, `SG-`, `TR-`, `GV-` codes (optional).
- `consumers`: who uses the output (for example Halie, Settle, GTM, AQL Operations).
- `skills`, `knowledge`, `tools`: names of other primitives. `delegates`: behaviour folders a companion may hand work to
  (companions only).
- `runs`: inline or background. `max_steps`: optional step limit.
Body headings: `## Who and what`, `## What you do`, `## Rules`, `## Output`.

## Other types
- **Skill**: `governance` (codes enforced). Body: short imperative rules; no output format except output_envelope.
- **Knowledge**: `status` (canonical = settled; working = first-cut method to validate; proposed = placeholder for
  SMEs), `owner` (domain owner of meaning), `effective_date`, `review_by`, optional `governance`.
- **Journey**: `goal`, `loops`, optional `chain`, `consumers`. Body: one `## <milestone_key>` per milestone with
  title, after, behaviour, loop, done_when (output, approval, outcome, human), approval (none, policy, required),
  optional phase, and why.
- **Routine**: `cadence` (hourly, daily, weekly, monthly, quarterly), `behaviour`, `trigger`, `loops`, `scope`
  (system, product, venture, company, portfolio, user), optional `signals`.
- **Tool**: `kind` (brain, builtin, http), `authority`, `consequence` (none to critical), `approval` (none, policy,
  required), `reversible`; http tools add `method`, `base_url_env`, `auth_env`. Parameters are defined in code.

## What the runtime enforces
- Front-matter syntax, required keys, allowed values, name pattern, file-name match, uniqueness and description length.
- Every referenced code exists, and every referenced skill, knowledge item, tool, behaviour and delegate folder resolves.
- An agent lists only tools at or below its own authority. Tool authority, consequence and approval are checked
  again at the point of use; approval-required tools cannot run without a hash-matched approval.
- Journey milestones form a directed acyclic graph and name existing behaviours.
- Routines name an existing behaviour and a valid cadence and trigger.
- A primitive that fails validation is not run, and the failure is reported, not ignored.

## Thinking levels
`thinking` maps to a configured, ordered list of models for each level (low, medium, high). The router picks from
that list and falls back along it on failure. Choose the cheapest level that does the job well: low for
classification, routing and simple retrieval; medium for grounded answers, extraction and drafting; high for
multi-step judgement, research synthesis and planning. Primitives never name a model or provider.

## Phase gating
The runtime is configured with a maximum phase. Primitives above it still load and validate, so references stay
consistent, but do not run: such agents cannot be invoked, routines do not schedule, and later-phase journey
milestones show as not yet available.

## The one door
Every edit to any primitive is a proposal (primitive_propose) showing current → proposed content, reason, impact and
consequence. A Halience admin approves the exact version; Halience staff are the only admins. On apply, the runtime
writes an Action record and a new revision, and any revision can be rolled back the same way. Halie may view and
propose; it never approves or applies its own edits and never expands its own authority or tools.

## Rules of thumb
- **Agents**: one job, 25 to 60 lines. Pick the lowest authority that does the job. Include grounding,
  failure_honesty and output_envelope, plus untrusted_content where material is read. Name the output types.
- **Skills**: at most about 25 lines of imperative rules; one concern; no output format.
- **Knowledge**: one topic per file; neutral, no "you"; cite spec codes; state what is still open.
- **Journeys**: milestones as outcomes, not tasks; approval required for publish, send, transact, stage gates and invest.
- **Routines**: say what is checked, what is handed on, what counts as material, and what must never happen.
- **Tools**: say when to use, what returns, and what it never does; set consequence and reversibility honestly.
- Use only names in the catalogue; propose new primitives rather than inventing references. British English, plain prose.
