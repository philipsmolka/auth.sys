package it.frs.auth.sys.dto;

import java.util.Map;

/**
 * Defines a sealed set of possible responses for the password recovery process.
 */
public sealed interface AuthRecoveryResponse
        permits AuthRecoveryResponse.ConfirmationSent,
        AuthRecoveryResponse.PasswordChanged,
        AuthRecoveryResponse.TokenInvalid,
        AuthRecoveryResponse.ValidationFailure,
        AuthRecoveryResponse.Failure {

    /**
     * Response after successfully initiating the password recovery process.
     */
    record ConfirmationSent(String message) implements AuthRecoveryResponse {}

    /**
     * Response after successfully changing the password.
     */
    record PasswordChanged(String message) implements AuthRecoveryResponse {}

    /**
     * Specific error indicating the provided token is invalid.
     */
    record TokenInvalid(String message) implements AuthRecoveryResponse {}

    /**
     * Validation error, e.g. when the new password is too weak.
     */
    record ValidationFailure(String message, Map<String, String> fieldErrors) implements AuthRecoveryResponse {}

    /**
     * General failure in the recovery process.
     */
    record Failure(String message, String errorCode) implements AuthRecoveryResponse {}
}
