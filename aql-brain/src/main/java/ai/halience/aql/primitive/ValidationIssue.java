package ai.halience.aql.primitive;

/** One problem found in a primitive. Errors stop a primitive set going live; warnings are reported only. */
public record ValidationIssue(Severity severity, String key, String message) {

    public enum Severity { ERROR, WARNING }

    public static ValidationIssue error(String key, String message) {
        return new ValidationIssue(Severity.ERROR, key, message);
    }

    public static ValidationIssue warning(String key, String message) {
        return new ValidationIssue(Severity.WARNING, key, message);
    }

    public boolean isError() {
        return severity == Severity.ERROR;
    }

    @Override
    public String toString() {
        return severity + " " + key + ": " + message;
    }
}
