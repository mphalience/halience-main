package ai.halience.aql.action;

import ai.halience.aql.domain.ActionRecord;
import lombok.extern.slf4j.Slf4j;

/**
 * The fallback connector when no real one is configured for a channel: it records the approved action in the outbox
 * for a person to carry out by hand, and says so honestly in its receipt. It never pretends the message went out.
 */
@Slf4j
public class OutboxConnector implements Connector {

    private final String channel;

    public OutboxConnector(String channel) {
        this.channel = channel;
    }

    @Override
    public String channel() {
        return channel;
    }

    @Override
    public String execute(ActionRecord action) {
        log.info("Outbox: {} action {} to {} queued for manual completion", channel, action.getId(), action.getTarget());
        return "queued_for_manual_completion: no " + channel + " connector is configured, so the approved action is in "
                + "the outbox for a person to carry out";
    }
}
