package it.frs.auth.sys.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Defines a sealed set of possible authentication-related requests.
 * Currently includes only login and token refresh, but can be easily extended.
 */
public sealed interface AuthRequest permits AuthRequest.Login, AuthRequest.RefreshToken {

    /**
     * Login request using email and password.
     * Fields are annotated with validation constraints.
     */
    record Login(
            @NotBlank(message = "Email cannot be blank")
            @Email(message = "Invalid email format")
            String email,

            @NotBlank(message = "Password cannot be blank")
            @Size(min = 8, message = "Password must be at least 8 characters long")
            String password
    ) implements AuthRequest {}

    /**
     * Request to refresh the access token using a valid refresh token.
     */
    record RefreshToken(String refreshToken) implements AuthRequest {}

    // In the future, additional authentication-related requests can be added here.
}
