package it.frs.auth.sys.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Defines a sealed set of possible responses from the authentication process.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // Ignores null fields in the JSON response
public sealed interface AuthResponse permits AuthResponse.Success, AuthResponse.Failure {

    /**
     * Represents a successful authentication response, containing tokens and user data.
     */
    record Success(
            String accessToken,
            String refreshToken,
            UserDto user
    ) implements AuthResponse {}

    /**
     * Represents an authentication failure response, containing an error message and code.
     */
    record Failure(
            String message,
            String errorCode
    ) implements AuthResponse {}

    /**
     * DTO carrying user data in the authentication response.
     */
    record UserDto(String email, List<String> roles) {}
}
