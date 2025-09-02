package it.frs.auth.sys.unitTests;

import it.frs.auth.sys.model.User;
import it.frs.auth.sys.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp(){
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "my-secret-my-secret-my-secret-my-secret");
        ReflectionTestUtils.setField(jwtService, "accessTokenValidity", 1000L * 60 * 60);
        ReflectionTestUtils.setField(jwtService, "refreshTokenValidity", 1000L * 60 * 60 * 24);
        ReflectionTestUtils.setField(jwtService, "passwordResetTokenValidity", 1000L * 60 * 10);
        ReflectionTestUtils.setField(jwtService, "hour", 1000L * 60 * 60);
        jwtService.init(); // inicjalizacja signingKey
    }

    @Test
    void generateAccessToken_ShouldContainEmailAndRole(){

        String userEmail = "example@test.com";
        String userRole = "ROLE_USER";

        User mockUser = new User();
        mockUser.setEmail(userEmail);
        mockUser.setRoles(List.of(userRole));

        String token = jwtService.generateAccessToken(mockUser);
        String email = jwtService.extractEmail(token);

        assertEquals(userEmail, jwtService.extractEmail(token));

    }

    @Test
    void generatePasswordResetToken_ShouldContainEmail(){

        String userEmail = "example@test.com";

        User mockUser = new User();
        mockUser.setEmail(userEmail);

        String token = jwtService.generatePasswordResetToken(userEmail);
        String email = jwtService.extractEmail(token);

        assertEquals(userEmail, jwtService.extractEmail(token));

    }

    @Test
    void isTokenValid_ValidToken_ShouldReturnTrue(){

        String userEmail = "example@test.com";
        String userRole = "ROLE_USER";

        User mockUser = new User();
        mockUser.setEmail(userEmail);
        mockUser.setRoles(List.of(userRole));

        String token = jwtService.generateAccessToken(mockUser);

        assertTrue(jwtService.isTokenValid(token, mockUser));

    }

    @Test
    void isTokenValid_WrongUser_ShouldReturnFalse(){

        String userEmail = "example@test.com";
        String userRole = "ROLE_USER";

        User mockUser = new User();
        mockUser.setEmail(userEmail);
        mockUser.setRoles(List.of(userRole));

        String otherUserEmail = "example@test.com";
        String otherUserRole = "ROLE_USER";

        User otherMockUser = new User();
        otherMockUser.setEmail(userEmail);
        otherMockUser.setRoles(List.of(userRole));

        String token = jwtService.generateAccessToken(mockUser);

        assertTrue(jwtService.isTokenValid(token, otherMockUser));

    }

}
