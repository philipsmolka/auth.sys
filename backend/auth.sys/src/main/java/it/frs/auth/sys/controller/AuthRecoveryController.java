package it.frs.auth.sys.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import it.frs.auth.sys.dto.AuthRecoveryRequest;
import it.frs.auth.sys.dto.AuthRecoveryResponse;
import it.frs.auth.sys.service.AuthRecoveryService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/auth-recovery")
@RequiredArgsConstructor
public class AuthRecoveryController {

    private final AuthRecoveryService recoveryService;

    /**
     * Initiates the password recovery process by sending a confirmation email.
     */
    @PostMapping("/request-password")
    public ResponseEntity<AuthRecoveryResponse> requestPasswordReset(
            @Valid @RequestBody AuthRecoveryRequest.LostPassword request) {
        AuthRecoveryResponse response = recoveryService.requestPasswordReset(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Resets the user's password using the token from the recovery link.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<AuthRecoveryResponse> resetPassword(
            @Valid @RequestBody AuthRecoveryRequest.ResetPassword request) {
        AuthRecoveryResponse response = recoveryService.resetPassword(request);

        // Switch on the sealed response type to return appropriate HTTP status
        return switch (response) {
            case AuthRecoveryResponse.PasswordChanged success ->
                    ResponseEntity.ok(success);
            case AuthRecoveryResponse.TokenInvalid invalid ->
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(invalid);
            case AuthRecoveryResponse.ValidationFailure validation ->
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validation);
            case AuthRecoveryResponse.Failure failure ->
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(failure);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        };
    }
}