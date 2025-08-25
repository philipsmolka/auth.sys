package it.frs.auth.sys.exception;

/**
 * Exception thrown when an operation is not permitted due to business rules
 * or security restrictions.
 * <p>
 * Common use cases include attempts to modify or delete administrator accounts
 * or accessing resources without proper authorization.
 * </p>
 *
 * <p>
 * This exception extends {@link RuntimeException} and is typically handled globally
 * to return a 403 Forbidden HTTP status.
 * </p>
 *
 * @see RuntimeException
 */
public class OperationForbiddenException extends RuntimeException {

    /**
     * Constructs a new {@code OperationForbiddenException} with the specified detail message.
     *
     * @param message the detail message explaining why the operation is forbidden.
     */
    public OperationForbiddenException(String message) {
        super(message);
    }
}
