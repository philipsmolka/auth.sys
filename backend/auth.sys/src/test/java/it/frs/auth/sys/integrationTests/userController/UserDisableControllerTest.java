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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "api.secret-key=test-secret-key")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class UserDisableControllerTest {

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

    //DISABLE USER "HTTP 200" - HAPPY PATH

    @Test
    void shouldDisableUserSuccessfully() throws Exception {

        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        assertTrue(adminOpt.isPresent());
        User admin = adminOpt.get();

        String token = jwtService.generateAccessToken(admin);

        User userToDisable = userRepository.save(UserObject.testUser());


        mockMvc.perform(patch("/api/users/{id}/disable", userToDisable.getId())
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));


        assertFalse(userToDisable.isEnabled());
    }

    //DISABLE USER "HTTP 409" - USER ALREADY DISABLED

    @Test
    void shouldNotDisableUser_Conflict() throws Exception {

        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        assertTrue(adminOpt.isPresent());
        User admin = adminOpt.get();

        String token = jwtService.generateAccessToken(admin);

        User userToDisable = UserObject.testUser();
        userToDisable.setActive(false);
        userRepository.save(userToDisable);

        mockMvc.perform(patch("/api/users/{id}/disable", userToDisable.getId())
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

    }

    //DISABLE USER "HTTP 404" - USER NOT FOUND

    @Test
    void shouldNotDisableUser_ResourceNotFound() throws Exception {

        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        assertTrue(adminOpt.isPresent());
        User admin = adminOpt.get();

        String token = jwtService.generateAccessToken(admin);

        mockMvc.perform(patch("/api/users/{id}/disable", 69)
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with ID 69 not found."))
                .andExpect(jsonPath("$.status").value(404));

    }

    //DISABLE USER "HTTP 403" - NO PERMISSION

    @Test
    void shouldNotDisableUser_NoPermission() throws Exception {

        User userToDisable = userRepository.save(UserObject.testUser());

        User user = userRepository.save(UserObject.testUserFactory("example@user-without-permission.com", "example-user-without-permission"));

        String token = jwtService.generateAccessToken(user);

        mockMvc.perform(patch("/api/users/{id}/disable", userToDisable.getId())
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

    }

    //DISABLE USER "HTTP 401" - UNAUTHORIZED ( TOKEN )

    @Test
    void shouldNotDisableUser_InvalidToken() throws Exception {

        User userToDisable = userRepository.save(UserObject.testUser());

        mockMvc.perform(patch("/api/users/{id}/disable", userToDisable.getId())
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    //DISABLE USER "HTTP 401" - UNAUTHORIZED ( API KEY )

    @Test
    void shouldNotDisableUser_InvalidApiKey() throws Exception {
        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        User admin = adminOpt.get();
        String token = jwtService.generateAccessToken(admin);

        User userToDisable = userRepository.save(UserObject.testUser());

        mockMvc.perform(patch("/api/users/{id}/disable", userToDisable.getId())
                        .header("X-API-KEY", "wrong-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

}
