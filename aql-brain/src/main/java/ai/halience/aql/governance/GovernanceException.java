package ai.halience.aql.governance;

/**
 * A deterministic control said no: missing permission, authority or approval, a disabled capability, an invalid
 * state transition. The message is safe to show the caller and to the model.
 */
public class GovernanceException extends RuntimeException {

    public enum Reason { PERMISSION, AUTHORITY, APPROVAL, DISABLED, INVALID, NOT_FOUND }

    private final Reason reason;

    public GovernanceException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}
