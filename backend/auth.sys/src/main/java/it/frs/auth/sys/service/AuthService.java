package it.frs.auth.sys.service;

import it.frs.auth.sys.dto.AuthRequest;
import it.frs.auth.sys.dto.AuthResponse;
import it.frs.auth.sys.model.User;
import it.frs.auth.sys.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * Service responsible for orchestrating user authentication and token management.
 * It handles the primary login process and the refreshing of access tokens.
 *
 * @see AuthenticationManager
 * @see JwtService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    /**
     * Authenticates a user with the provided credentials (email and password).
     * <p>
     * This method uses Spring Security's {@link AuthenticationManager} to validate the credentials.
     * It is designed to not throw exceptions for predictable login failures (e.g., wrong password).
     * Instead, it returns a structured {@link AuthResponse} indicating either success or failure.
     *
     * @param request An {@link AuthRequest.Login} DTO containing the user's email and password.
     * @return An {@link AuthResponse} which will be an instance of {@link AuthResponse.Success}
     * containing JWTs and user data on successful authentication, or an instance of
     * {@link AuthResponse.Failure} with an error message if authentication fails.
     */
    public AuthResponse authenticate(AuthRequest.Login request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new IllegalStateException("User not found after successful authentication: " + request.email()));

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            var userDto = new AuthResponse.UserDto(user.getEmail(), user.getRoles());

            log.info("Successfully authenticated user: {}", request.email());
            return new AuthResponse.Success(accessToken, refreshToken, userDto);

        } catch (AuthenticationException e) {
            log.warn("Failed authentication attempt for user: {}", request.email(), e);
            return new AuthResponse.Failure("Invalid email or password.", "INVALID_CREDENTIALS");
        }
    }

    /**
     * Refreshes a user's access token using a valid refresh token.
     * <p>
     * This method validates the provided refresh token. If it's valid and corresponds to an existing user,
     * a new access token is generated and returned. The original refresh token is returned alongside the new
     * access token; token rotation is not implemented in this flow.
     *
     * @param request An {@link AuthRequest.RefreshToken} DTO containing the refresh token.
     * @return An {@link AuthResponse}. On success, it is an {@link AuthResponse.Success} with a new
     * access token. On failure, it is an {@link AuthResponse.Failure} indicating why the refresh failed.
     */
    public AuthResponse refreshToken(AuthRequest.RefreshToken request) {
        final String refreshToken = request.refreshToken();
        final String userEmail;

        try {
            userEmail = jwtService.extractEmail(refreshToken);
        } catch (Exception e) {
            log.warn("Invalid refresh token provided.", e);
            return new AuthResponse.Failure("Invalid or expired refresh token.", "INVALID_REFRESH_TOKEN");
        }

        User user = this.userRepository.findByEmail(userEmail).orElse(null);

        if (user != null) {
            String newAccessToken = jwtService.generateAccessToken(user);
            var userDto = new AuthResponse.UserDto(user.getEmail(), user.getRoles());
            log.info("Successfully refreshed token for user: {}", userEmail);
            return new AuthResponse.Success(newAccessToken, refreshToken, userDto);
        }

        log.warn("Refresh token processing failed for user email: {}", userEmail);
        return new AuthResponse.Failure("Failed to refresh token.", "REFRESH_TOKEN_FAILURE");
    }
}