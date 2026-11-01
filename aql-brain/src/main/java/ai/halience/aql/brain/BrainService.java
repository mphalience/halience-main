package ai.halience.aql.brain;

import ai.halience.aql.config.BrainProperties;
import ai.halience.aql.governance.Actor;
import ai.halience.aql.governance.GovernanceException;
import org.springframework.stereotype.Service;

/**
 * The brain's single entry point for work: users (through Halie or a product), routines, journeys and events all come
 * in here. Initiator and trigger are attributes of the request, not separate pipelines. When no behaviour is named, the
 * intent classifier chooses one first. Admin behaviours run only for Halience admins.
 */
@Service
public class BrainService {

    private final BehaviourRunner runner;
    private final IntentClassifier classifier;
    private final BrainAgents agents;
    private final BrainProperties properties;

    public BrainService(BehaviourRunner runner, IntentClassifier classifier, BrainAgents agents,
            BrainProperties properties) {
        this.runner = runner;
        this.classifier = classifier;
        this.agents = agents;
        this.properties = properties;
    }

    public BrainResult run(BrainRequest request) {
        Actor actor = request.context().actor();
        boolean admin = actor.isPerson() && actor.hasRole(properties.brain().adminRole());
        BrainRequest resolved = request;
        if (request.behaviour() == null || request.behaviour().isBlank()) {
            IntentClassifier.Classification c = classifier.classify(request.prompt(), request.context(), admin);
            resolved = new BrainRequest(c.behaviour(), request.prompt(),
                    request.context().withExtra("Classified as", c.promptClasses().stream()
                            .map(pc -> pc.code() + " " + pc.title()).toList() + " (" + c.source() + ")"),
                    request.consequence() != null ? request.consequence() : c.consequence(), request.answerSchema());
        }
        if (!admin && BrainAgents.ADMIN_FOLDER.equals(agents.requireEnabled(resolved.behaviour()).folder())) {
            throw new GovernanceException(GovernanceException.Reason.PERMISSION,
                    "That capability is for Halience admins");
        }
        return runner.run(resolved);
    }
}
