package it.frs.auth.sys.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Defines a closed set of possible responses for user-related operations in the admin panel.
 * <p>
 * This sealed interface ensures consistency and predictability of API responses
 * by restricting implementations to a fixed set of response types.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed interface UserResponse permits
        UserResponse.UserDetails,
        UserResponse.SuccessMessage,
        UserResponse.UserNotFound,
        UserResponse.EmailAlreadyExists,
        UserResponse.ValidationFailure,
        UserResponse.Failure {

    /**
     * Successful response containing full user details.
     * Typically returned after user creation, editing, role changes,
     * or when fetching details for a single user.
     *
     * @param id     the unique identifier of the user
     * @param email  the user's email address
     * @param name   the user's display name
     * @param roles  the list of roles assigned to the user
     * @param active the active status of the user
     */
    record UserDetails(
            Long id,
            String email,
            String name,
            List<String> roles,
            boolean active
    ) implements UserResponse {}

    /**
     * Generic success response with a simple message.
     * Suitable for operations such as user deletion, password changes, or other
     * actions where only a success acknowledgment is needed.
     *
     * @param message descriptive success message
     */
    record SuccessMessage(String message) implements UserResponse {}

    // --- Specific, frequent error responses ---

    /**
     * Error response indicating that a user with the specified identifier (ID, email, etc.)
     * was not found in the system.
     *
     * @param message detailed error message explaining the user was not found
     */
    record UserNotFound(String message) implements UserResponse {}

    /**
     * Error response indicating that the provided email address
     * is already in use by another user, violating uniqueness constraints.
     *
     * @param message detailed error message about email duplication
     */
    record EmailAlreadyExists(String message) implements UserResponse {}

    // --- Generic error responses ---

    /**
     * Response representing validation failure on input data,
     * such as invalid format, missing required fields, or value constraints violations.
     *
     * @param message     general message describing the validation failure
     * @param fieldErrors map containing field-specific error messages (field name → error description)
     */
    record ValidationFailure(String message, Map<String, String> fieldErrors) implements UserResponse {}

    /**
     * General failure response used for server errors or business logic errors
     * that do not fall into more specific error categories.
     *
     * @param message   descriptive error message
     * @param errorCode optional error code identifying the failure reason
     */
    record Failure(String message, String errorCode) implements UserResponse {}

}
