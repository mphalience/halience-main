package ai.halience.aql.action;

import ai.halience.aql.domain.ActionRecord;

/**
 * An actuator that carries out an approved action in a connected system: an email service, a publishing channel, a
 * CRM, a ticketing system. Products depend on this contract, not on a provider's response (replaceable integration
 * boundary). A connector only ever receives actions the runtime has already authorised.
 */
public interface Connector {

    /** The channel this connector serves, e.g. {@code email}, {@code publish}, {@code api}. */
    String channel();

    /**
     * Carries out the action. Returns a receipt to record; throws on failure. Must be idempotent on the action's
     * {@code idempotencyKey}, since a recovered run may retry.
     */
    String execute(ActionRecord action) throws Exception;
}
