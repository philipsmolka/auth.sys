package it.frs.auth.sys.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Defines a sealed set of possible requests for the password recovery process.
 */
public sealed interface AuthRecoveryRequest
        permits AuthRecoveryRequest.LostPassword, AuthRecoveryRequest.ResetPassword {

    /**
     * Request to initiate the password recovery process.
     * Requires the email associated with the account.
     */
    record LostPassword(
            @NotBlank(message = "Email cannot be blank")
            @Email(message = "Invalid email format")
            String email
    ) implements AuthRecoveryRequest {}

    /**
     * Request to set a new password using a token from the recovery link.
     */
    record ResetPassword(
            @NotBlank(message = "Token cannot be blank")
            String token,

            @NotBlank(message = "New password cannot be blank")
            @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
            String newPassword,

            @NotBlank(message = "Password confirmation cannot be blank")
            String confirmNewPassword
    ) implements AuthRecoveryRequest {}
}
