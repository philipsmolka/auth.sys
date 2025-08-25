package it.frs.auth.sys.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import it.frs.auth.sys.dto.AuthRequest;
import it.frs.auth.sys.dto.AuthResponse;
import it.frs.auth.sys.model.User; // Assuming User implements UserDetails
import it.frs.auth.sys.service.AuthService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Authenticates a user using email and password.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest.Login request) {
        AuthResponse response = authService.authenticate(request);

        // Use pattern matching for sealed interface responses
        return switch (response) {
            case AuthResponse.Success success -> ResponseEntity.ok(success);
            case AuthResponse.Failure failure -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(failure);
        };
    }

    /**
     * Retrieves the currently logged-in user's details from the token.
     * Spring Security automatically processes the token and injects the User object.
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserDto> getCurrentUser(@AuthenticationPrincipal User user) {
        // @AuthenticationPrincipal injects the user object extracted from the JWT token
        if (user == null) {
            // This should not happen if the endpoint is secured, but it's a safety check
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var userDto = new AuthResponse.UserDto(user.getEmail(), user.getRoles());
        return ResponseEntity.ok(userDto);
    }

    /**
     * Refreshes the access token using a refresh token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody AuthRequest.RefreshToken request) {
        AuthResponse response = authService.refreshToken(request);

        return switch (response) {
            case AuthResponse.Success success -> ResponseEntity.ok(success);
            case AuthResponse.Failure failure -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(failure);
        };
    }
}
