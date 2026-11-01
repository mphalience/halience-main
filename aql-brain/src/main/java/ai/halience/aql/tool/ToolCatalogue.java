package ai.halience.aql.tool;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import ai.halience.agent.loop.tool.RegisteredTool;
import ai.halience.agent.loop.tool.ToolContext;
import ai.halience.agent.loop.tool.ToolHandler;
import ai.halience.agent.loop.tool.ToolInputException;
import ai.halience.agent.loop.tool.api.ApiTools;
import ai.halience.agent.loop.tool.builtin.WorkspaceTools;
import ai.halience.agent.loop.tool.web.BraveSearch;
import ai.halience.agent.loop.tool.web.SearchProvider;
import ai.halience.agent.loop.tool.web.TavilySearch;
import ai.halience.agent.loop.tool.web.WebTools;
import ai.halience.aql.brain.RunRegistry;
import ai.halience.aql.governance.Actor;
import ai.halience.aql.governance.AuditLog;
import ai.halience.aql.governance.AuthorityPolicy;
import ai.halience.aql.primitive.BehaviourSpec;
import ai.halience.aql.primitive.Primitive;
import ai.halience.aql.primitive.PrimitiveLibrary;
import ai.halience.aql.primitive.PrimitiveType;
import ai.halience.aql.primitive.ToolManifest;
import ai.halience.llm.router.model.ToolDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Builds an agent's tools from its behaviour's {@code tools:} list. Each tool primitive is bound to its actuator (a
 * {@link BrainTool}, a built-in agent-loop tool, or a generic HTTP read) and every call is wrapped in the gate:
 * <ul>
 *   <li>a tool in a disabled phase is not offered at all;</li>
 *   <li>the calling behaviour's authority must cover the tool's (checked at validation and again per call);</li>
 *   <li>a built-in tool that would write to another system and needs approval is refused: writes go through a
 *       proposed, approved action instead (brain execute tools check the approval themselves);</li>
 *   <li>denials and every execute-level call are audited.</li>
 * </ul>
 */
@Slf4j
@Component
public class ToolCatalogue {

    private final Map<String, BrainTool> brainTools;
    private final PrimitiveLibrary library;
    private final RunRegistry runs;
    private final AuthorityPolicy policy;
    private final AuditLog audit;
    private final Function<String, String> env;

    @org.springframework.beans.factory.annotation.Autowired
    public ToolCatalogue(List<BrainTool> brainTools, PrimitiveLibrary library, RunRegistry runs, AuthorityPolicy policy,
            AuditLog audit) {
        this(brainTools, library, runs, policy, audit, System::getenv);
    }

    ToolCatalogue(List<BrainTool> brainTools, PrimitiveLibrary library, RunRegistry runs, AuthorityPolicy policy,
            AuditLog audit, Function<String, String> env) {
        this.brainTools = brainTools.stream().collect(Collectors.toMap(BrainTool::name, Function.identity()));
        this.library = library;
        this.runs = runs;
        this.policy = policy;
        this.audit = audit;
        this.env = env;
    }

    public Set<String> brainToolNames() {
        return new TreeSet<>(brainTools.keySet());
    }

    /** Brain tool primitives with no Java handler: a startup error. */
    public List<String> unboundBrainTools() {
        return library.current().ofType(PrimitiveType.TOOL).stream().map(ToolManifest::of)
                .filter(m -> m.kind() == ToolManifest.Kind.BRAIN && !brainTools.containsKey(m.name()))
                .map(ToolManifest::name).toList();
    }

    public List<RegisteredTool> toolsFor(BehaviourSpec behaviour) {
        List<RegisteredTool> tools = new ArrayList<>();
        for (String name : behaviour.tools()) {
            Optional<Primitive> primitive = library.current().named(PrimitiveType.TOOL, name);
            if (primitive.isEmpty() || !library.isEnabled(primitive.get())) {
                continue;
            }
            ToolManifest manifest = ToolManifest.of(primitive.get());
            for (RegisteredTool base : actuators(manifest)) {
                tools.add(gated(base, manifest, behaviour));
            }
        }
        return tools;
    }

    private List<RegisteredTool> actuators(ToolManifest manifest) {
        return switch (manifest.kind()) {
            case BRAIN -> {
                BrainTool tool = brainTools.get(manifest.name());
                if (tool == null) {
                    throw new IllegalStateException("Tool primitive " + manifest.name() + " has no Java handler");
                }
                Map<String, Object> schema = new LinkedHashMap<>();
                schema.put("type", "object");
                schema.put("properties", tool.properties());
                schema.put("required", tool.required());
                ToolDefinition definition = ToolDefinition.builder().name(manifest.name())
                        .description(manifest.description()).parameters(schema).build();
                // The real handler is attached by gated(), which needs the call context.
                yield List.of(new RegisteredTool(definition, (args, ctx) -> {
                    throw new IllegalStateException("ungated brain tool");
                }));
            }
            case BUILTIN -> builtin(manifest);
            case HTTP -> List.of(httpRead(manifest));
        };
    }

    private List<RegisteredTool> builtin(ToolManifest manifest) {
        return switch (manifest.name()) {
            case "web_fetch" -> List.of(WebTools.fetch());
            case "web_search" -> searchProvider().map(p -> List.of(WebTools.search(p)))
                    .orElseGet(() -> List.of(unavailable(manifest, "No web search provider is configured "
                            + "(set HALIENCE_TAVILY_API_KEY or HALIENCE_BRAVE_API_KEY)")));
            case "api_request" -> List.of(ApiTools.request(List.of()));
            case "workspace_files" -> WorkspaceTools.all();
            default -> throw new IllegalStateException("Unknown builtin tool " + manifest.name());
        };
    }

    private Optional<SearchProvider> searchProvider() {
        String tavily = env.apply("HALIENCE_TAVILY_API_KEY");
        if (tavily != null && !tavily.isBlank()) {
            return Optional.of(new TavilySearch(tavily));
        }
        String brave = env.apply("HALIENCE_BRAVE_API_KEY");
        if (brave != null && !brave.isBlank()) {
            return Optional.of(new BraveSearch(brave));
        }
        return Optional.empty();
    }

    private static RegisteredTool unavailable(ToolManifest manifest, String why) {
        ToolDefinition definition = ToolDefinition.builder().name(manifest.name()).description(manifest.description())
                .parameters(Map.of("type", "object", "properties", Map.of("query", Map.of("type", "string"))))
                .build();
        return new RegisteredTool(definition, (args, ctx) -> "Unavailable: " + why
                + ". Say that this source couldn't be reached; don't present the gap as an answer.");
    }

    /** A generic read-only HTTP tool: GET {base}/{path} with a bearer token from the environment. */
    private RegisteredTool httpRead(ToolManifest manifest) {
        ToolDefinition definition = ToolDefinition.builder().name(manifest.name()).description(manifest.description())
                .parameters(Map.of("type", "object", "properties",
                        Map.of("path", Map.of("type", "string", "description", "Path under the API, e.g. accounts")),
                        "required", List.of("path")))
                .build();
        return new RegisteredTool(definition, (args, ctx) -> {
            if (!"GET".equals(manifest.method())) {
                return "Refused: " + manifest.name() + " is configured for " + manifest.method()
                        + "; only reads are supported without an approved action.";
            }
            String base = env.apply(manifest.baseUrlEnv());
            if (base == null || base.isBlank()) {
                return "Unavailable: " + manifest.baseUrlEnv() + " is not set, so this source can't be reached.";
            }
            String path = String.valueOf(args.get("path")).replaceFirst("^/+", "");
            if (path.contains("..")) {
                throw new ToolInputException("path can't contain '..'");
            }
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base.replaceFirst("/+$", "") + "/" + path))
                    .timeout(Duration.ofSeconds(30)).GET();
            String token = manifest.authEnv() == null ? null : env.apply(manifest.authEnv());
            if (token != null && !token.isBlank()) {
                request.header("Authorization", "Bearer " + token);
            }
            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
                String body = response.body();
                return "HTTP " + response.statusCode() + "\n" + (body.length() > 20_000 ? body.substring(0, 20_000)
                        + "\n[truncated]" : body);
            } catch (Exception e) {
                return "Failed: " + manifest.name() + " couldn't be reached (" + e.getMessage() + ").";
            }
        }, Duration.ofSeconds(45), true);
    }

    private RegisteredTool gated(RegisteredTool base, ToolManifest manifest, BehaviourSpec behaviour) {
        BrainTool brain = manifest.kind() == ToolManifest.Kind.BRAIN ? brainTools.get(manifest.name()) : null;
        ToolDefinition definition = base.definition();
        String guidance = manifest.guidance() == null || manifest.guidance().isBlank() ? ""
                : "\n\n" + manifest.guidance();
        ToolDefinition described = ToolDefinition.builder().name(definition.getName())
                .description(definition.getDescription() + guidance).parameters(definition.getParameters()).build();
        ToolHandler handler = (args, ctx) -> {
            RunRegistry.RunState run = runs.find(ctx.tags()).orElse(null);
            Actor principal = run == null ? Actor.system("unknown-run") : run.context().actor();
            Actor agent = Actor.agent(behaviour.name(), principal);
            if (!behaviour.authority().covers(manifest.authority())) {
                audit.record(agent, "tool_denied", manifest.name(), "denied", Map.of("reason", "authority"));
                return "Refused: " + behaviour.name() + " doesn't have the authority to use " + manifest.name() + ".";
            }
            if (brain == null && policy.requiresApproval(manifest) && base.writes(args, ctx)) {
                audit.record(agent, "tool_denied", manifest.name(), "denied", Map.of("reason", "approval"));
                return "Refused: this call would change another system, which needs an approved action. Propose the "
                        + "action with propose_action and request approval instead.";
            }
            String result = brain != null
                    ? brain.call(args, new BrainCall(behaviour.name(), behaviour.authority(), manifest, principal, agent,
                            run, ctx))
                    : base.handler().handle(args, ctx);
            if (manifest.authority().isExecution()) {
                audit.record(agent, "tool_called", manifest.name(), "completed",
                        Map.of("behaviour", behaviour.name()));
            }
            return result;
        };
        return new RegisteredTool(described, handler, base.timeout(), base.untrustedOutput(), base.requiresWorkspace(),
                base.definitionWithoutWorkspace() == null ? null : ToolDefinition.builder()
                        .name(base.definitionWithoutWorkspace().getName())
                        .description(base.definitionWithoutWorkspace().getDescription() + guidance)
                        .parameters(base.definitionWithoutWorkspace().getParameters()).build(),
                base.writeCheck());
    }

    /** For tests and diagnostics: invoke a gated tool directly. */
    static String invoke(RegisteredTool tool, Map<String, Object> args, ToolContext ctx) {
        return tool.handler().handle(args, ctx);
    }
}
