package it.frs.auth.sys.integrationTests.userController;

import it.frs.auth.sys.model.User;
import it.frs.auth.sys.repository.UserRepository;
import it.frs.auth.sys.service.JwtService;
import it.frs.auth.sys.utils.UserObject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "api.secret-key=test-secret-key")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class UserChangePasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @BeforeAll
    void init() {

    }

    @BeforeEach
    void setUp() {

    }

    //CHANGE USER PASSWORD "HTTP 201" - HAPPY PATH

    @Test
    void shouldChangePasswordUserSuccessfully() throws Exception {

        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        assertTrue(adminOpt.isPresent());
        User admin = adminOpt.get();

        String token = jwtService.generateAccessToken(admin);

        User userToChangePassword = userRepository.save(UserObject.testUser());

        String requestJson = """
        {
            "newPassword": "test-password"
        }
        """;

        mockMvc.perform(patch("/api/users/{id}/password", userToChangePassword.getId())
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated successfully."));

    }

    //CHANGE USER PASSWORD "HTTP 404" - USER NOT FOUND

    @Test
    void shouldNotChangePasswordUser_ResourceNotFound() throws Exception {

        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        assertTrue(adminOpt.isPresent());
        User admin = adminOpt.get();

        String token = jwtService.generateAccessToken(admin);

        String requestJson = """
        {
            "newPassword": "test-password"
        }
        """;

        mockMvc.perform(patch("/api/users/{id}/password", 69)
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with ID 69 not found."))
                .andExpect(jsonPath("$.status").value(404));

    }

    //CHANGE USER PASSWORD "HTTP 403" - NO PERMISSION

    @Test
    void shouldNotChangePasswordUser_NoPermission() throws Exception {

        User user = userRepository.save(UserObject.testUserFactory("example@user-without-permission.com", "example-user-without-permission"));

        String token = jwtService.generateAccessToken(user);

        User userToChangePassword = userRepository.save(UserObject.testUser());

        String requestJson = """
        {
            "newPassword": "test-password"
        }
        """;

        mockMvc.perform(patch("/api/users/{id}/password", userToChangePassword.getId())
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());

    }

    //CHANGE USER PASSWORD "HTTP 401" - UNAUTHORIZED ( TOKEN )

    @Test
    void shouldNotChangePasswordUser_InvalidToken() throws Exception {

        User userToChangePassword = userRepository.save(UserObject.testUser());

        String requestJson = """
        {
            "newPassword": "test-password"
        }
        """;

        mockMvc.perform(patch("/api/users/{id}/password", userToChangePassword.getId())
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized());
    }

    //CHANGE USER PASSWORD "HTTP 401" - UNAUTHORIZED ( API KEY )

    @Test
    void shouldNotChangePasswordUser_InvalidApiKey() throws Exception {
        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        User admin = adminOpt.get();
        String token = jwtService.generateAccessToken(admin);

        String requestJson = """
        {
            "newPassword": "test-password"
        }
        """;

        User userToChangePassword = userRepository.save(UserObject.testUser());

        mockMvc.perform(patch("/api/users/{id}/password", userToChangePassword.getId())
                        .header("X-API-KEY", "wrong-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized());
    }

    //CHANGE USER PASSWORD "HTTP 400" - BAD REQUEST

    @Test
    void shouldNotChangePasswordUser_WhenPasswordTooShort() throws Exception {
        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        User admin = adminOpt.get();
        String token = jwtService.generateAccessToken(admin);

        User userToChangePassword = userRepository.save(UserObject.testUser());

        String requestJson = """
        {
            "newPassword": "blank"
        }
        """;

        mockMvc.perform(patch("/api/users/{id}/password", userToChangePassword.getId())
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400));
    }



}
