package it.frs.auth.sys.exception;

/**
 * Exception thrown when the input provided by the client is invalid
 * or fails validation rules defined by the business logic.
 * <p>
 * Common scenarios include malformed or missing fields, invalid enum values,
 * or attempts to use disallowed values (e.g., unsupported roles).
 * </p>
 *
 * <p>
 * This exception extends {@link RuntimeException} and is typically handled globally
 * to return a 400 Bad Request HTTP status.
 * </p>
 *
 * @see RuntimeException
 */
public class InvalidInputException extends RuntimeException {

    /**
     * Constructs a new {@code InvalidInputException} with the specified detail message.
     *
     * @param message the detail message describing the validation failure.
     */
    public InvalidInputException(String message) {
        super(message);
    }
}
