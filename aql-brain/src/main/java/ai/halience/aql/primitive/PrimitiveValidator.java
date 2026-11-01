package ai.halience.aql.primitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import ai.halience.aql.journey.JourneyDefinition;
import ai.halience.aql.journey.JourneyEngine;
import ai.halience.aql.routine.RoutineDefinition;
import ai.halience.aql.spec.ContextDomain;
import ai.halience.aql.spec.GovernanceControl;
import ai.halience.aql.spec.Loop;
import ai.halience.aql.spec.LoopChain;
import ai.halience.aql.spec.OutputType;
import ai.halience.aql.spec.PromptActivity;
import ai.halience.aql.spec.PromptClass;
import ai.halience.aql.spec.SignalClass;
import ai.halience.aql.spec.SpecCode;
import ai.halience.aql.spec.Trigger;
import ai.halience.aql.spec.Vocabulary.KnowledgeStatus;

/**
 * Checks a whole primitive set: each primitive's front matter, every reference between primitives, every spec code,
 * least privilege (an agent's tools never need more authority than the agent has, and an agent implementing an
 * activity has the authority the spec gives it), phase consistency (nothing depends on a later-phase primitive that
 * would be switched off) and journey structure. The runtime refuses to make a set with errors live.
 */
public final class PrimitiveValidator {

    private PrimitiveValidator() {
    }

    public static List<ValidationIssue> validate(PrimitiveSet set) {
        List<ValidationIssue> issues = new ArrayList<>();
        checkNames(set, issues);
        for (Primitive p : set.all()) {
            try {
                switch (p.type()) {
                    case BEHAVIOUR -> {
                        if (p.isSkill()) {
                            checkSkill(p, issues);
                        } else {
                            checkAgent(p, set, issues);
                        }
                    }
                    case KNOWLEDGE -> checkKnowledge(p, issues);
                    case TOOL -> ToolManifest.of(p);
                    case JOURNEY -> checkJourney(p, set, issues);
                    case ROUTINE -> checkRoutine(p, set, issues);
                }
            } catch (IllegalArgumentException e) {
                issues.add(ValidationIssue.error(p.key(), e.getMessage()));
            }
        }
        return issues;
    }

    public static List<ValidationIssue> errors(PrimitiveSet set) {
        return validate(set).stream().filter(ValidationIssue::isError).toList();
    }

    private static void checkNames(PrimitiveSet set, List<ValidationIssue> issues) {
        Map<String, String> seen = new HashMap<>();
        for (Primitive p : set.all()) {
            if (!Primitive.NAME.matcher(p.name()).matches()) {
                issues.add(ValidationIssue.error(p.key(), "name '" + p.name() + "' must match " + Primitive.NAME));
            }
            if (!p.name().equals(p.fileName())) {
                issues.add(ValidationIssue.error(p.key(), "name '" + p.name() + "' must match the file name"));
            }
            if (p.description().isBlank()) {
                issues.add(ValidationIssue.error(p.key(), "description is required"));
            } else if (p.description().length() > 160) {
                issues.add(ValidationIssue.warning(p.key(), "description is over 160 characters"));
            }
            if (p.body().isBlank()) {
                issues.add(ValidationIssue.error(p.key(), "body can't be empty"));
            }
            // Agents and skills share the behaviour namespace so references are unambiguous.
            String namespace = p.type().name() + ":" + p.name();
            String previous = seen.putIfAbsent(namespace, p.key());
            if (previous != null) {
                issues.add(ValidationIssue.error(p.key(), "name '" + p.name() + "' is also used by " + previous));
            }
        }
    }

    private static void checkSkill(Primitive p, List<ValidationIssue> issues) {
        codes(p, "governance", GovernanceControl.class, issues);
    }

    private static void checkKnowledge(Primitive p, List<ValidationIssue> issues) {
        String status = p.frontMatter().getOrDefault("status", "canonical");
        try {
            KnowledgeStatus parsed = KnowledgeStatus.valueOf(status.strip().toUpperCase(java.util.Locale.ROOT));
            if (parsed != KnowledgeStatus.CANONICAL && parsed != KnowledgeStatus.WORKING
                    && parsed != KnowledgeStatus.PROPOSED) {
                issues.add(ValidationIssue.error(p.key(), "knowledge status must be canonical, working or proposed"));
            }
        } catch (IllegalArgumentException e) {
            issues.add(ValidationIssue.error(p.key(), "knowledge status '" + status + "' is unknown"));
        }
        if (p.field("owner") == null) {
            issues.add(ValidationIssue.warning(p.key(), "knowledge should name its owner (GV-19)"));
        }
        codes(p, "governance", GovernanceControl.class, issues);
    }

    private static void checkAgent(Primitive p, PrimitiveSet set, List<ValidationIssue> issues) {
        BehaviourSpec spec = BehaviourSpec.of(p);
        Consumer<String> error = message -> issues.add(ValidationIssue.error(p.key(), message));
        if (spec.promptClasses().isEmpty()) {
            error.accept("prompt_classes must name at least one Prompt Class");
        }
        if (spec.loops().isEmpty()) {
            error.accept("loops must name at least one Loop");
        }
        List<PromptClass> classes = codes(p, "prompt_classes", PromptClass.class, issues);
        for (PromptActivity activity : codes(p, "activities", PromptActivity.class, issues)) {
            if (!spec.authority().covers(activity.authority())) {
                error.accept(activity.code() + " needs authority " + activity.authority().wireValue()
                        + " but the behaviour only has " + spec.authority().wireValue());
            }
            if (!classes.contains(activity.promptClass())) {
                error.accept(activity.code() + " is a " + activity.promptClass().code()
                        + " activity; add it to prompt_classes");
            }
        }
        codes(p, "loops", Loop.class, issues);
        codes(p, "outputs", OutputType.class, issues);
        codes(p, "context", ContextDomain.class, issues);
        codes(p, "signals", SignalClass.class, issues);
        codes(p, "triggers", Trigger.class, issues);
        codes(p, "governance", GovernanceControl.class, issues);

        for (String skill : spec.skills()) {
            reference(p, set.skill(skill), "skill", skill, issues);
        }
        for (String knowledge : spec.knowledge()) {
            reference(p, set.named(PrimitiveType.KNOWLEDGE, knowledge), "knowledge", knowledge, issues);
        }
        for (String toolName : spec.tools()) {
            Optional<Primitive> tool = set.named(PrimitiveType.TOOL, toolName);
            if (reference(p, tool, "tool", toolName, issues)) {
                try {
                    ToolManifest manifest = ToolManifest.of(tool.get());
                    if (!spec.authority().covers(manifest.authority())) {
                        error.accept("tool " + toolName + " needs authority " + manifest.authority().wireValue()
                                + " but the behaviour only has " + spec.authority().wireValue());
                    }
                } catch (IllegalArgumentException ignored) {
                    // reported against the tool itself
                }
            }
        }
        for (String folder : spec.delegates()) {
            if (folder.equals(PrimitiveType.SKILLS_FOLDER)) {
                error.accept("delegates can't list the skills folder");
            } else if (set.agentsIn(folder).isEmpty()) {
                error.accept("delegates folder '" + folder + "' has no agents");
            }
        }
    }

    private static void checkJourney(Primitive p, PrimitiveSet set, List<ValidationIssue> issues) {
        JourneyDefinition journey = JourneyDefinition.of(p);
        codes(p, "loops", Loop.class, issues);
        if (journey.chain() != null && SpecCode.parse(LoopChain.class, journey.chain()).isEmpty()) {
            issues.add(ValidationIssue.error(p.key(), "chain '" + journey.chain() + "' is not a Loop Chain code"));
        }
        JourneyEngine.structuralProblems(journey).forEach(m -> issues.add(ValidationIssue.error(p.key(), m)));
        for (JourneyDefinition.Milestone m : journey.milestones()) {
            if (m.loop() != null && SpecCode.parse(Loop.class, m.loop()).isEmpty()) {
                issues.add(ValidationIssue.error(p.key(), "milestone " + m.key() + ": unknown loop " + m.loop()));
            }
            if (m.behaviour() == null) {
                if (m.doneWhen() != JourneyDefinition.DoneWhen.HUMAN
                        && m.doneWhen() != JourneyDefinition.DoneWhen.APPROVAL) {
                    issues.add(ValidationIssue.warning(p.key(), "milestone " + m.key()
                            + " has no behaviour; it can only be completed by a person"));
                }
                continue;
            }
            Optional<Primitive> agent = set.agent(m.behaviour());
            if (agent.isEmpty()) {
                issues.add(ValidationIssue.error(p.key(), "milestone " + m.key() + ": unknown behaviour "
                        + m.behaviour()));
            } else if (agent.get().phase() > m.phase()) {
                issues.add(ValidationIssue.error(p.key(), "milestone " + m.key() + " is phase " + m.phase()
                        + " but behaviour " + m.behaviour() + " is phase " + agent.get().phase()
                        + "; flag the milestone with phase: " + agent.get().phase()));
            }
        }
    }

    private static void checkRoutine(Primitive p, PrimitiveSet set, List<ValidationIssue> issues) {
        RoutineDefinition routine = RoutineDefinition.of(p);
        if (SpecCode.parse(Trigger.class, routine.trigger()).isEmpty()) {
            issues.add(ValidationIssue.error(p.key(), "trigger '" + routine.trigger() + "' is not a Trigger code"));
        }
        codes(p, "loops", Loop.class, issues);
        codes(p, "signals", SignalClass.class, issues);
        reference(p, set.agent(routine.behaviour()), "behaviour", routine.behaviour(), issues);
    }

    /** Checks a referenced primitive exists and is not in a later phase than the one referring to it. */
    private static boolean reference(Primitive from, Optional<Primitive> target, String kind, String name,
            List<ValidationIssue> issues) {
        if (target.isEmpty()) {
            issues.add(ValidationIssue.error(from.key(), "unknown " + kind + " '" + name + "'"));
            return false;
        }
        if (target.get().phase() > from.phase()) {
            issues.add(ValidationIssue.error(from.key(), kind + " '" + name + "' is phase " + target.get().phase()
                    + " but this is phase " + from.phase() + ", so it would be switched off"));
        }
        return true;
    }

    private static <E extends Enum<E> & SpecCode> List<E> codes(Primitive p, String field, Class<E> type,
            List<ValidationIssue> issues) {
        List<E> parsed = new ArrayList<>();
        for (String code : p.list(field)) {
            Optional<E> value = SpecCode.parse(type, code);
            if (value.isPresent()) {
                parsed.add(value.get());
            } else {
                issues.add(ValidationIssue.error(p.key(), field + ": '" + code + "' is not a " + type.getSimpleName()
                        + " code"));
            }
        }
        return parsed;
    }
}
