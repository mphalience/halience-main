package ai.halience.aql.llm;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import ai.halience.aql.config.BrainProperties;
import ai.halience.llm.router.config.FallbackPolicy;
import ai.halience.llm.router.config.RouteEntry;
import ai.halience.llm.router.config.RouterConfig;
import ai.halience.llm.router.provider.Provider;

/**
 * Turns a behaviour's {@link Thinking} level into a {@link RouterConfig}: the configured models for that level, in
 * order, followed by the router's own choice at the same depth if all of them are unavailable. Models whose provider
 * has no credentials here are dropped, so a deployment with only one provider's key still works.
 */
public final class ThinkingRoutes {

    private final Map<Thinking, List<RouteEntry>> routes = new EnumMap<>(Thinking.class);

    public ThinkingRoutes(BrainProperties.Llm config, Predicate<Provider> available) {
        for (Thinking level : Thinking.values()) {
            List<RouteEntry> entries = new ArrayList<>();
            for (String model : config.thinking().getOrDefault(level, List.of())) {
                RouteEntry entry = parse(model);
                if (available.test(entry.getProvider()) && !entries.contains(entry)) {
                    entries.add(entry);
                }
            }
            routes.put(level, List.copyOf(entries));
        }
    }

    /** {@code provider/model} or {@code provider}. The model may itself contain slashes (OpenRouter ids). */
    public static RouteEntry parse(String value) {
        String clean = value.strip();
        int slash = clean.indexOf('/');
        String providerId = slash < 0 ? clean : clean.substring(0, slash);
        Provider provider;
        try {
            provider = Provider.fromId(providerId.toLowerCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown provider '" + providerId + "' in thinking route '" + value + "'");
        }
        return slash < 0 ? RouteEntry.of(provider) : RouteEntry.of(provider, clean.substring(slash + 1));
    }

    /** The models tried for {@code level}, in order (after dropping unavailable providers). */
    public List<RouteEntry> models(Thinking level) {
        return routes.get(level);
    }

    public RouterConfig routerConfig(Thinking level) {
        List<RouteEntry> models = routes.get(level);
        RouterConfig.RouterConfigBuilder builder = RouterConfig.builder()
                .thinkingLevel(level.routerLevel())
                .fallback(FallbackPolicy.SAME_DEPTH)
                .sendThinkingLevel(true);
        if (!models.isEmpty()) {
            builder.route(models);
        }
        return builder.build();
    }
}
