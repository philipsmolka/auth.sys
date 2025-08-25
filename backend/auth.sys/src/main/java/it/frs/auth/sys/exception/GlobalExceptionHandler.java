package it.frs.auth.sys.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Global exception handler for REST controllers.
 * <p>
 * This class handles application-specific exceptions and maps them to appropriate
 * HTTP status codes with structured error responses.
 * </p>
 *
 * <p>
 * Handled exceptions:
 * <ul>
 *   <li>{@link ResourceNotFoundException} → 404 Not Found</li>
 *   <li>{@link OperationForbiddenException} → 403 Forbidden</li>
 *   <li>{@link ResourceAlreadyExistsException} → 409 Conflict</li>
 *   <li>{@link InvalidInputException} → 400 Bad Request</li>
 * </ul>
 * </p>
 *
 * <p>
 * All responses are returned as JSON with the following structure:
 * <pre>{@code
 * {
 *   "error": "Error message here"
 * }
 * }</pre>
 * </p>
 *
 * @see ResourceNotFoundException
 * @see OperationForbiddenException
 * @see ResourceAlreadyExistsException
 * @see InvalidInputException
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@link ResourceNotFoundException} and returns a 404 Not Found response.
     *
     * @param ex the thrown exception.
     * @return a JSON response with an error message and HTTP 404 status.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(ResourceNotFoundException ex) {
        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    /**
     * Handles {@link OperationForbiddenException} and returns a 403 Forbidden response.
     *
     * @param ex the thrown exception.
     * @return a JSON response with an error message and HTTP 403 status.
     */
    @ExceptionHandler(OperationForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleOperationForbidden(OperationForbiddenException ex) {
        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    /**
     * Handles {@link ResourceAlreadyExistsException} and returns a 409 Conflict response.
     *
     * @param ex the thrown exception.
     * @return a JSON response with an error message and HTTP 409 status.
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.CONFLICT);
    }

    /**
     * Handles {@link InvalidInputException} and returns a 400 Bad Request response.
     *
     * @param ex the thrown exception.
     * @return a JSON response with an error message and HTTP 400 status.
     */
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<Map<String, String>> handleInvalidInput(InvalidInputException ex) {
        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
