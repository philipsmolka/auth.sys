package it.frs.auth.sys.service;

import it.frs.auth.sys.dto.AuthRecoveryRequest;
import it.frs.auth.sys.dto.AuthRecoveryResponse;
import it.frs.auth.sys.model.User;
import it.frs.auth.sys.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service dedicated to handling the user password recovery process.
 * This includes initiating the password reset request and finalizing the password change.
 */
@SuppressWarnings("unused")
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthRecoveryService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /**
     * The base URL of the frontend application, used to construct password reset links.
     * Injected from application properties.
     */
    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Initiates the password reset process for a given email address.
     * <p>
     * If a user with the specified email exists, a password reset token is generated and
     * a reset link is sent to their email. For security reasons, this method always returns
     * a generic success message, regardless of whether the user was found. This prevents
     * email enumeration attacks, where an attacker could otherwise determine which emails
     * are registered in the system.
     *
     * @param request An {@link AuthRecoveryRequest.LostPassword} DTO containing the user's email.
     * @return An {@link AuthRecoveryResponse.ConfirmationSent} with a generic message,
     * confirming that the request has been processed.
     */
    public AuthRecoveryResponse requestPasswordReset(AuthRecoveryRequest.LostPassword request) {
        userRepository.findByEmail(request.email())
                .ifPresent(user -> {
                    String token = jwtService.generatePasswordResetToken(user.getEmail());
                    String resetLink = frontendUrl + "/reset-password?token=" + token;
                    emailService.sendResetPassword(request.email(), resetLink);
                    log.info("Password reset link sent for user: {}", request.email());
                });

        // Always return a generic success message to prevent email enumeration.
        return new AuthRecoveryResponse.ConfirmationSent("If an account with the provided email exists, a password reset link has been sent.");
    }

    /**
     * Resets a user's password using a valid token and a new password.
     * <p>
     * This method first validates that the new password and its confirmation match.
     * It then validates the JWT, extracts the user's email, finds the user,
     * and updates their password in the database.
     *
     * @param request An {@link AuthRecoveryRequest.ResetPassword} DTO containing the reset token
     * and the new password details.
     * @return An {@link AuthRecoveryResponse} indicating the outcome:
     * - {@link AuthRecoveryResponse.PasswordChanged} on success.
     * - {@link AuthRecoveryResponse.ValidationFailure} if passwords do not match.
     * - {@link AuthRecoveryResponse.TokenInvalid} if the token is invalid, expired,
     * or linked to a non-existent user.
     */
    public AuthRecoveryResponse resetPassword(AuthRecoveryRequest.ResetPassword request) {
        // Validate that the provided passwords match.
        if (!request.newPassword().equals(request.confirmNewPassword())) {
            return new AuthRecoveryResponse.ValidationFailure("The provided passwords do not match.", Map.of("confirmNewPassword", "Passwords must be the same."));
        }

        try {
            String email = jwtService.extractEmail(request.token());
            var userOptional = userRepository.findByEmail(email);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setPassword(passwordEncoder.encode(request.newPassword()));
                userRepository.save(user);

                log.info("Password for user {} has been successfully reset.", email);
                return new AuthRecoveryResponse.PasswordChanged("Password has been successfully reset.");
            } else {
                log.warn("Password reset failed: token is associated with a non-existent user (email: {}).", email);
                return new AuthRecoveryResponse.TokenInvalid("The token is associated with a non-existent user.");
            }
        } catch (Exception e) {
            log.error("Error during password reset. Token: {}", request.token(), e);
            return new AuthRecoveryResponse.TokenInvalid("The token is invalid or has expired.");
        }
    }
}
