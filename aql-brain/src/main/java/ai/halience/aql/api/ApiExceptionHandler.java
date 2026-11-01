package ai.halience.aql.api;

import java.util.Map;

import ai.halience.aql.governance.GovernanceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps governance refusals to HTTP statuses, with the (safe) reason in the body. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(GovernanceException.class)
    ResponseEntity<Map<String, String>> governance(GovernanceException e) {
        HttpStatus status = switch (e.reason()) {
            case PERMISSION, AUTHORITY, APPROVAL -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case DISABLED -> HttpStatus.CONFLICT;
            case INVALID -> HttpStatus.UNPROCESSABLE_CONTENT;
        };
        return ResponseEntity.status(status).body(Map.of("reason", e.reason().name(), "message", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> invalid(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("reason", "INVALID", "message", String.valueOf(e.getMessage())));
    }
}
