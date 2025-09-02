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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "api.secret-key=test-secret-key")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class UserEnableControllerTest {

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

    //ENABLE USER "HTTP 200" - HAPPY PATH

    @Test
    void shouldEnableUserSuccessfully() throws Exception {

        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        assertTrue(adminOpt.isPresent());
        User admin = adminOpt.get();

        String token = jwtService.generateAccessToken(admin);

        User userToEnable = userRepository.save(UserObject.testUser());
        userToEnable.setActive(false);


        mockMvc.perform(patch("/api/users/{id}/enable", userToEnable.getId())
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));


        assertTrue(userToEnable.isEnabled());
    }

    //ENABLE USER "HTTP 409" - USER ALREADY ENABLED

    @Test
    void shouldNotEnableUser_Conflict() throws Exception {

        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        assertTrue(adminOpt.isPresent());
        User admin = adminOpt.get();

        String token = jwtService.generateAccessToken(admin);

        User userToEnable = UserObject.testUser();
        userToEnable.setActive(true);
        userRepository.save(userToEnable);

        mockMvc.perform(patch("/api/users/{id}/enable", userToEnable.getId())
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

    }

    //ENABLE USER "HTTP 404" - USER NOT FOUND

    @Test
    void shouldNotEnableUser_ResourceNotFound() throws Exception {

        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        assertTrue(adminOpt.isPresent());
        User admin = adminOpt.get();

        String token = jwtService.generateAccessToken(admin);

        mockMvc.perform(patch("/api/users/{id}/enable", 69)
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with ID 69 not found."))
                .andExpect(jsonPath("$.status").value(404));

    }

    //ENABLE USER "HTTP 403" - NO PERMISSION

    @Test
    void shouldNotEnableUser_NoPermission() throws Exception {

        User userToEnable = userRepository.save(UserObject.testUser());
        userToEnable.setActive(false);

        User user = userRepository.save(UserObject.testUserFactory("example@user-without-permission.com", "example-user-without-permission"));

        String token = jwtService.generateAccessToken(user);

        mockMvc.perform(patch("/api/users/{id}/enable", userToEnable.getId())
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

    }

    //ENABLE USER "HTTP 401" - UNAUTHORIZED ( TOKEN )

    @Test
    void shouldNotEnableUser_InvalidToken() throws Exception {

        User userToEnable = userRepository.save(UserObject.testUser());
        userToEnable.setActive(false);

        mockMvc.perform(patch("/api/users/{id}/enable", userToEnable.getId())
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    //ENABLE USER "HTTP 401" - UNAUTHORIZED ( API KEY )

    @Test
    void shouldNotEnableUser_InvalidApiKey() throws Exception {
        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        User admin = adminOpt.get();
        String token = jwtService.generateAccessToken(admin);

        User userToEnable = userRepository.save(UserObject.testUser());
        userToEnable.setActive(false);

        mockMvc.perform(patch("/api/users/{id}/enable", userToEnable.getId())
                        .header("X-API-KEY", "wrong-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

}
