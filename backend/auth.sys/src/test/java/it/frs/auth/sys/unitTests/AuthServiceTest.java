package it.frs.auth.sys.unitTests;

import it.frs.auth.sys.dto.AuthRequest;
import it.frs.auth.sys.dto.AuthResponse;
import it.frs.auth.sys.model.User;
import it.frs.auth.sys.repository.UserRepository;
import it.frs.auth.sys.service.AuthService;
import it.frs.auth.sys.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(authenticationManager, userRepository, jwtService);
    }

    @Test
    void authenticate_successfulLogin_returnSuccessResponse(){

        String email = "test@example.com";
        String password = "password";

        AuthRequest.Login request = new AuthRequest.Login(email, password);
        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setRoles(List.of("ROLE_USER"));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(jwtService.generateAccessToken(mockUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(mockUser)).thenReturn("refresh-token");

        AuthResponse response = authService.authenticate(request);

        assertTrue(response instanceof AuthResponse.Success);
        AuthResponse.Success success = (AuthResponse.Success) response;
        assertEquals("access-token", success.accessToken());
        assertEquals("refresh-token", success.refreshToken());
        assertEquals(email, success.user().email());
        assertTrue(success.user().roles().contains("ROLE_USER"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

    }

    @Test
    void authenticate_invalidCredentials_returnsFailureResponse() {
        AuthRequest.Login request = new AuthRequest.Login("wrong@example.com", "wrongpassword");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        AuthResponse response = authService.authenticate(request);

        assertTrue(response instanceof AuthResponse.Failure);
        AuthResponse.Failure failure = (AuthResponse.Failure) response;
        assertEquals("INVALID_CREDENTIALS", failure.errorCode());
    }

    @Test
    void refreshToken_validToken_returnsNewAccessToken() {
        AuthRequest.RefreshToken request = new AuthRequest.RefreshToken("valid-refresh-token");
        User user = new User();
        user.setEmail("test@example.com");
        user.setRoles(List.of("ROLE_USER"));

        when(jwtService.extractEmail("valid-refresh-token")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");

        AuthResponse response = authService.refreshToken(request);

        assertTrue(response instanceof AuthResponse.Success);
        AuthResponse.Success success = (AuthResponse.Success) response;
        assertEquals("new-access-token", success.accessToken());
        assertEquals("valid-refresh-token", success.refreshToken());
        assertEquals("test@example.com", success.user().email());
    }

    @Test
    void refreshToken_invalidToken_returnsFailure() {
        AuthRequest.RefreshToken request = new AuthRequest.RefreshToken("invalid-token");

        when(jwtService.extractEmail("invalid-token")).thenThrow(new RuntimeException("Invalid token"));

        AuthResponse response = authService.refreshToken(request);

        assertTrue(response instanceof AuthResponse.Failure);
        AuthResponse.Failure failure = (AuthResponse.Failure) response;
        assertEquals("INVALID_REFRESH_TOKEN", failure.errorCode());
    }

    @Test
    void refreshToken_userNotFound_returnsFailure() {
        AuthRequest.RefreshToken request = new AuthRequest.RefreshToken("valid-token");

        when(jwtService.extractEmail("valid-token")).thenReturn("missing@example.com");
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        AuthResponse response = authService.refreshToken(request);

        assertTrue(response instanceof AuthResponse.Failure);
        AuthResponse.Failure failure = (AuthResponse.Failure) response;
        assertEquals("REFRESH_TOKEN_FAILURE", failure.errorCode());
    }

}
