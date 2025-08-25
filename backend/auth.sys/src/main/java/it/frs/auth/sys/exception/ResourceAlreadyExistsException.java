package it.frs.auth.sys.exception;

/**
 * Exception thrown when attempting to create or register a resource
 * that already exists in the system.
 * <p>
 * Commonly used to indicate violations of uniqueness constraints,
 * such as duplicate email addresses, usernames, or other unique fields.
 * </p>
 *
 * <p>
 * This exception extends {@link RuntimeException} and is typically handled globally
 * (e.g., with {@code @ControllerAdvice}) to return a 409 Conflict HTTP status.
 * </p>
 *
 * @see RuntimeException
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new {@code ResourceAlreadyExistsException} with the specified detail message.
     *
     * @param message the detail message describing the conflict.
     */
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
