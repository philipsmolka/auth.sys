package it.frs.auth.sys.exception;

/**
 * Exception thrown when a requested resource is not found in the system.
 * <p>
 * Typically used in service or repository layers when querying for an entity
 * that does not exist, such as a user, role, or other domain object.
 * </p>
 *
 * <p>
 * This exception extends {@link RuntimeException} and is usually handled globally
 * by an exception handler (e.g., {@code @ControllerAdvice}) to return a 404 Not Found
 * HTTP response.
 * </p>
 *
 * @see RuntimeException
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code ResourceNotFoundException} with the specified detail message.
     *
     * @param message the detail message explaining which resource was not found.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
