package it.frs.auth.sys.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

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
 *   "status": 400,
 *   "message": "Validation failed",
 *   "errors": {
 *       "field1": "Error message for field1",
 *       "field2": "Error message for field2"
 *   }
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
     * @return a JSON response with status and message.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, Object> responseBody = Map.of(
                "status", HttpStatus.NOT_FOUND.value(),
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles {@link OperationForbiddenException} and returns a 403 Forbidden response.
     *
     * @param ex the thrown exception.
     * @return a JSON response with status and message.
     */
    @ExceptionHandler(OperationForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleOperationForbidden(OperationForbiddenException ex) {
        Map<String, Object> responseBody = Map.of(
                "status", HttpStatus.FORBIDDEN.value(),
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(responseBody, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles {@link ResourceAlreadyExistsException} and returns a 409 Conflict response.
     *
     * @param ex the thrown exception.
     * @return a JSON response with status and message.
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
        Map<String, Object> responseBody = Map.of(
                "status", HttpStatus.CONFLICT.value(),
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(responseBody, HttpStatus.CONFLICT);
    }

    /**
     * Handles {@link InvalidInputException} and returns a 400 Bad Request response.
     *
     * @param ex the thrown exception.
     * @return a JSON response with status and message.
     */
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidInput(InvalidInputException ex) {
        Map<String, Object> responseBody = Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link MethodArgumentNotValidException} and returns a 400 Bad Request response.
     *
     * <p>
     * This exception is thrown when a {@code @Valid} annotated request body fails validation,
     * e.g., when fields annotated with {@code @NotBlank}, {@code @Email}, or {@code @Size}
     * do not meet the specified constraints.
     * </p>
     *
     * @param ex the thrown exception.
     * @return a JSON response with status, message, and field-specific error messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage()
                ));

        Map<String, Object> responseBody = Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "message", "Validation failed",
                "errors", fieldErrors
        );

        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }
}
