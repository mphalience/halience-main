package ai.halience.aql.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ai.halience.agent.loop.Scope;
import ai.halience.agent.loop.memory.MemoryStore;
import ai.halience.agent.loop.workspace.InMemoryWorkspace;
import ai.halience.agent.loop.workspace.WorkspaceLimits;
import ai.halience.agent.loop.tool.RegisteredTool;
import ai.halience.agent.loop.tool.ToolContext;
import ai.halience.aql.Fakes;
import ai.halience.aql.action.ActionService;
import ai.halience.aql.brain.RunRegistry;
import ai.halience.aql.config.BrainProperties;
import ai.halience.aql.context.ContextEnvelope;
import ai.halience.aql.domain.Repositories.ApprovalRepository;
import ai.halience.aql.domain.Repositories.AuditRepository;
import ai.halience.aql.domain.Repositories.EvidenceRepository;
import ai.halience.aql.domain.Repositories.KnowledgeRepository;
import ai.halience.aql.domain.Repositories.OutputRepository;
import ai.halience.aql.domain.Repositories.PrimitiveRepository;
import ai.halience.aql.governance.ApprovalService;
import ai.halience.aql.governance.AuditLog;
import ai.halience.aql.governance.AuthorityPolicy;
import ai.halience.aql.governance.PermissionService;
import ai.halience.aql.halie.ConversationService;
import ai.halience.aql.halie.NavigationService;
import ai.halience.aql.journey.JourneyService;
import ai.halience.aql.ops.HealthRegistry;
import ai.halience.aql.outcome.OutcomeService;
import ai.halience.aql.primitive.BehaviourSpec;
import ai.halience.aql.primitive.Primitive;
import ai.halience.aql.primitive.PrimitiveEditService;
import ai.halience.aql.primitive.PrimitiveLibrary;
import ai.halience.aql.primitive.PrimitiveSet;
import ai.halience.aql.primitive.StarterPrimitives;
import ai.halience.aql.signal.SignalService;
import ai.halience.aql.spec.Trigger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ToolCatalogueTest {

    static final PrimitiveSet SHIPPED = StarterPrimitives.parse(StarterPrimitives.load("classpath*:brain/**/*.md"));

    PrimitiveLibrary library;
    RunRegistry runs = new RunRegistry();
    AuditRepository audits = mock(AuditRepository.class);
    List<BrainTool> realTools;

    @BeforeEach
    void setUp() {
        Fakes.backed(audits);
        BrainProperties properties = BrainProperties.defaults();
        library = new PrimitiveLibrary(mock(PrimitiveRepository.class), properties);
        library.install(SHIPPED, Map.of());
        // Every real brain tool, built with mocked collaborators.
        EvidenceTools evidence = new EvidenceTools();
        CalculationTools calc = new CalculationTools();
        ActionTools actions = new ActionTools();
        HalieTools halie = new HalieTools();
        LearningTools learning = new LearningTools();
        AdminTools admin = new AdminTools();
        PermissionService permissions = new PermissionService();
        ActionService actionService = mock(ActionService.class);
        ApprovalService approvalService = mock(ApprovalService.class);
        OutputRepository outputs = mock(OutputRepository.class);
        HealthRegistry health = new HealthRegistry(Fakes.CLOCK);
        realTools = new ArrayList<>(List.of(
                evidence.evidenceSearch(mock(EvidenceRepository.class), permissions),
                evidence.knowledgeSearch(mock(KnowledgeRepository.class), permissions),
                evidence.artefactOpen(outputs, mock(KnowledgeRepository.class), mock(EvidenceRepository.class),
                        permissions),
                evidence.recordEvidence(mock(EvidenceRepository.class)),
                evidence.recordIntelligence(outputs, library),
                evidence.freezeSnapshot(mock(OutcomeService.class)),
                calc.calculate(), calc.score(),
                actions.proposeAction(actionService), actions.requestApproval(actionService, approvalService),
                actions.actionStatus(actionService, mock(ApprovalRepository.class), approvalService),
                actions.executeAction(actionService), actions.sendMessage(actionService),
                actions.publishContent(actionService), actions.saveDraft(outputs), actions.createArtefact(outputs),
                actions.escalate(outputs, new AuditLog(audits)),
                halie.conversationState(mock(ConversationService.class)), halie.composeCanvas(permissions),
                halie.navigate(mock(NavigationService.class)), halie.deliveryRead(List.of()),
                halie.journeyStatus(mock(JourneyService.class)),
                learning.emitSignal(mock(SignalService.class)), learning.recordEvaluation(outputs, health),
                learning.recordOutcome(mock(OutcomeService.class)), learning.healthRead(health),
                admin.primitiveView(library, mock(PrimitiveEditService.class), properties),
                admin.primitivePropose(mock(PrimitiveEditService.class), properties)));
    }

    ToolCatalogue catalogue() {
        return new ToolCatalogue(realTools, library, runs, new AuthorityPolicy(BrainProperties.defaults()),
                new AuditLog(audits), name -> null);
    }

    @Test
    void everyBrainToolPrimitiveHasAJavaHandler() {
        assertThat(catalogue().unboundBrainTools()).isEmpty();
    }

    @Test
    void everyEnabledBehaviourCanBuildItsTools() {
        ToolCatalogue catalogue = catalogue();
        for (Primitive agent : SHIPPED.agents()) {
            if (library.isEnabled(agent)) {
                BehaviourSpec spec = BehaviourSpec.of(agent);
                assertThat(catalogue.toolsFor(spec)).as(agent.name()).isNotNull();
            }
        }
    }

    @Test
    void laterPhaseToolsAreNotOffered() {
        BehaviourSpec builder = BehaviourSpec.of(SHIPPED.agent("venture_builder").orElseThrow());
        assertThat(catalogue().toolsFor(builder)).extracting(t -> t.definition().getName())
                .doesNotContain("workspace_files", "write_file");
    }

    @Test
    void unconfiguredWebSearchSaysItIsUnavailableRatherThanReturningNothing() {
        BehaviourSpec researcher = BehaviourSpec.of(SHIPPED.agent("researcher").orElseThrow());
        RegisteredTool search = catalogue().toolsFor(researcher).stream()
                .filter(t -> t.definition().getName().equals("web_search")).findFirst().orElseThrow();
        String result = search.handler().handle(Map.of("query", "x"), context());
        assertThat(result).startsWith("Unavailable");
    }

    @Test
    void theCalculatorToolReturnsVersionedLabelledNumbers() {
        BehaviourSpec affordability = BehaviourSpec.of(SHIPPED.agent("affordability").orElseThrow());
        RegisteredTool calc = catalogue().toolsFor(affordability).stream()
                .filter(t -> t.definition().getName().equals("calculate")).findFirst().orElseThrow();
        String result = calc.handler().handle(Map.of("household_income", 60000, "deposit", 30000), context());
        assertThat(result).contains("affordability-v0.1").contains("borrowing_range_low").contains("assumed");
    }

    @Test
    void primitiveToolsRefuseNonAdmins() {
        BehaviourSpec editor = BehaviourSpec.of(SHIPPED.agent("primitive_editor").orElseThrow());
        RegisteredTool view = catalogue().toolsFor(editor).stream()
                .filter(t -> t.definition().getName().equals("primitive_view")).findFirst().orElseThrow();
        assertThat(view.handler().handle(Map.of(), context())).startsWith("Refused");
    }

    /** A tool context for a run opened for Ana (not an admin), carrying the run tag as the agent loop would. */
    ToolContext context() {
        RunRegistry.RunState run = runs.open(ContextEnvelope.of(Fakes.person("ana"), Trigger.TR_01, Fakes.NOW));
        Scope scope = Scope.of("halience", "ana", "s1");
        ToolContext.RunAccess access = new ToolContext.RunAccess() {
            @Override
            public void report(ai.halience.agent.loop.MessageType type, String message) {
            }

            @Override
            public void showToModel(ai.halience.agent.loop.workspace.WorkspaceFile file) {
            }

            @Override
            public Map<String, String> tags() {
                return Map.of(RunRegistry.RUN_TAG, run.runId());
            }
        };
        return new ToolContext(UUID.randomUUID(), 0, scope, MemoryStore.NONE.scopedTo(scope),
                new InMemoryWorkspace().scopedTo(scope, WorkspaceLimits.DEFAULT), access);
    }

    @Test
    void anAgentNeverActsForMoreThanItsPrincipal() {
        assertThat(ai.halience.aql.governance.Actor.agent("halie", Fakes.person("ana")).scopes())
                .isEqualTo(Set.of(ai.halience.aql.governance.Actor.ALL_SCOPES));
    }
}
