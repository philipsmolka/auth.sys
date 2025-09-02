package it.frs.auth.sys.unitTests;

import it.frs.auth.sys.dto.AuthRecoveryRequest;
import it.frs.auth.sys.dto.AuthRecoveryResponse;
import it.frs.auth.sys.model.User;
import it.frs.auth.sys.repository.UserRepository;
import it.frs.auth.sys.service.AccountRecoveryService;
import it.frs.auth.sys.service.EmailService;
import it.frs.auth.sys.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AccountRecoveryTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountRecoveryService accountRecoveryService;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        String frontendUrl = "http://localhost:3000";
    }

    @Test
    void requestPasswordReset_UserExists_ShouldSendEmail(){

        String userEmail = "example@test.com";

        User mockUser = new User();
        mockUser.setEmail(userEmail);

        AuthRecoveryRequest.LostPassword request = new AuthRecoveryRequest.LostPassword(userEmail);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));
        when(jwtService.generatePasswordResetToken(userEmail)).thenReturn("recovery-token");

        AuthRecoveryResponse response = accountRecoveryService.requestPasswordReset(request);

        verify(emailService, times(1)).sendResetPassword(eq(userEmail), contains("recovery-token"));
        assertTrue(response instanceof AuthRecoveryResponse.ConfirmationSent);

    }

    @Test
    void resetPassword_PasswordDoNotMatch_ShouldReturnValidationFailure(){

        AuthRecoveryRequest.ResetPassword request = new AuthRecoveryRequest.ResetPassword("recovery-token","password", "not-matched-password");

        AuthRecoveryResponse response = accountRecoveryService.resetPassword(request);

        assertTrue(response instanceof AuthRecoveryResponse.ValidationFailure);

    }

    @Test
    void resetPassword_ValidTokenAndUser_ShouldResetPassword(){
        String userEmail = "example@test.com";
        String newPassword = "new-password";

        User mockUser = new User();
        mockUser.setEmail(userEmail);

        when(jwtService.extractEmail("validToken")).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode(newPassword)).thenReturn("new-encoded-password");

        AuthRecoveryRequest.ResetPassword request = new AuthRecoveryRequest.ResetPassword(
                "validToken",
                newPassword,
                newPassword
        );

        AuthRecoveryResponse response = accountRecoveryService.resetPassword(request);

        verify(userRepository, times(1)).save(mockUser);
        assertEquals("new-encoded-password", mockUser.getPassword());
        assertTrue(response instanceof AuthRecoveryResponse.PasswordChanged);


    }

    @Test
    void resetPassword_TokenInvalid_ShouldReturnTokenInvalid(){

        when(jwtService.extractEmail("invalid-token")).thenThrow(new RuntimeException("Invalid token"));

        AuthRecoveryRequest.ResetPassword request = new AuthRecoveryRequest.ResetPassword(
                "invalid-token",
                "new-password",
                "new-password"
        );

        AuthRecoveryResponse response = accountRecoveryService.resetPassword(request);

        assertTrue(response instanceof AuthRecoveryResponse.TokenInvalid);

    }

    @Test
    void resetPassword_UserNotFound_ShouldReturnTokenInvalid(){

        String userEmail = "example@test.com";
        String userToken = "token";
        String userPassword = "new-password";

        when(jwtService.extractEmail(userToken)).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        AuthRecoveryRequest.ResetPassword request = new AuthRecoveryRequest.ResetPassword(
                userToken,
                userPassword,
                userPassword
        );

        AuthRecoveryResponse response = accountRecoveryService.resetPassword(request);

        assertTrue(response instanceof AuthRecoveryResponse.TokenInvalid);

    }

}
