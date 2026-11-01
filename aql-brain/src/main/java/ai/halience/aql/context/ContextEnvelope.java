package ai.halience.aql.context;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ai.halience.aql.governance.Actor;
import ai.halience.aql.spec.Trigger;

/**
 * The context envelope for one brain run (spec "Context" shared contract): actor, active object/surface, conversation
 * and referents, time, entitlement, jurisdiction and permitted scopes. It scopes interpretation and retrieval but can
 * never expand permission or authority: those come from the actor, checked deterministically at each boundary.
 *
 * @param activeContext the active venture/object/surface (CX-03, CX-20), e.g. {@code {"venture":"vnt_settle"}}
 * @param referents     recent referents, newest first (CX-20)
 * @param extra         caller-supplied context (entitlement, jurisdiction, process state, preferences...), each key
 *                      named after the CX it carries where possible
 */
public record ContextEnvelope(Actor actor, Trigger trigger, Instant now, String conversationId, String channel,
                              Map<String, Object> activeContext, List<Map<String, Object>> referents,
                              Map<String, Object> extra) {

    public ContextEnvelope {
        activeContext = activeContext == null ? Map.of() : Map.copyOf(activeContext);
        referents = referents == null ? List.of() : List.copyOf(referents);
        extra = extra == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(extra));
        trigger = trigger == null ? Trigger.TR_01 : trigger;
    }

    public static ContextEnvelope of(Actor actor, Trigger trigger, Instant now) {
        return new ContextEnvelope(actor, trigger, now, null, null, Map.of(), List.of(), Map.of());
    }

    public ContextEnvelope withExtra(String key, Object value) {
        Map<String, Object> next = new LinkedHashMap<>(extra);
        next.put(key, value);
        return new ContextEnvelope(actor, trigger, now, conversationId, channel, activeContext, referents, next);
    }

    /**
     * The envelope as Markdown for the run's operating context. Only what the actor is permitted to know is ever put
     * in the envelope, so rendering it can't leak anything.
     */
    public String render() {
        StringBuilder out = new StringBuilder();
        out.append("## Context for this run\n");
        out.append("- Acting for: ").append(actor.id()).append(" (").append(actor.type().name().toLowerCase())
                .append(")");
        if (!actor.roles().isEmpty()) {
            out.append(", roles: ").append(String.join(", ", actor.roles()));
        }
        out.append("\n- Trigger: ").append(trigger.code()).append(" ").append(trigger.title()).append('\n');
        out.append("- Now: ").append(now).append('\n');
        if (conversationId != null) {
            out.append("- Conversation: ").append(conversationId).append(" on channel ").append(channel).append('\n');
        }
        if (!activeContext.isEmpty()) {
            out.append("- Active context: ").append(inline(activeContext)).append('\n');
        }
        if (!referents.isEmpty()) {
            out.append("- Recent referents (newest first):\n");
            referents.stream().limit(10).forEach(r -> out.append("  - ").append(inline(r)).append('\n'));
        }
        extra.forEach((key, value) -> out.append("- ").append(key).append(": ").append(value).append('\n'));
        out.append("\nThis context scopes the work. It never grants permission or authority: tools check those.\n");
        return out.toString();
    }

    private static String inline(Map<String, Object> map) {
        return map.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(", "));
    }
}
