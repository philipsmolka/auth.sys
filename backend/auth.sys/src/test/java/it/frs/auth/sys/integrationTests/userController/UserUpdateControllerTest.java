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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "api.secret-key=test-secret-key")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class UserUpdateControllerTest {

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

    //UPDATE USER "HTTP 201" - HAPPY PATH

    @Test
    void shouldUpdateUserSuccessfully() throws Exception {

        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        assertTrue(adminOpt.isPresent());
        User admin = adminOpt.get();

        String token = jwtService.generateAccessToken(admin);

        User userToUpdate = userRepository.save(UserObject.testUser());

        String requestJson = """
            {
              "email": "example@user-updated.com",
              "name": "example-user-updated",
              "role": "ROLE_MODERATOR"
            }
            """;

        mockMvc.perform(put("/api/users/{id}", userToUpdate.getId())
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("example@user-updated.com"))
                .andExpect(jsonPath("$.roles").value("ROLE_MODERATOR"))
                .andExpect(jsonPath("$.name").value("example-user-updated"));

    }

    //DISABLE USER "HTTP 404" - USER NOT FOUND

    @Test
    void shouldNotUpdateUser_ResourceNotFound() throws Exception {

        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        assertTrue(adminOpt.isPresent());
        User admin = adminOpt.get();

        String token = jwtService.generateAccessToken(admin);

        User userToUpdate = userRepository.save(UserObject.testUser());

        String requestJson = """
            {
              "email": "example@user-updated.com",
              "name": "example-user-updated",
              "role": "ROLE_USER"
            }
            """;

        mockMvc.perform(put("/api/users/{id}", 69)
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound());

    }

    //DISABLE USER "HTTP 403" - NO PERMISSION

    @Test
    void shouldNotUpdateUser_NoPermission() throws Exception {

        User user = userRepository.save(UserObject.testUserFactory("example@user-without-permission.com", "example-user-without-permission"));

        String token = jwtService.generateAccessToken(user);

        User userToUpdate = userRepository.save(UserObject.testUser());

        String requestJson = """
            {
              "email": "example@user-updated.com",
              "name": "example-user-updated",
              "role": "ROLE_USER"
            }
            """;

        mockMvc.perform(put("/api/users/{id}", 69)
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());

    }

    //UPDATE USER "HTTP 400" - BAD REQUEST ( NAME )

    @Test
    void shouldNotUpdateUser_InvalidName() throws Exception {

        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        assertTrue(adminOpt.isPresent());
        User admin = adminOpt.get();

        String token = jwtService.generateAccessToken(admin);

        User userToUpdate = userRepository.save(UserObject.testUser());

        String requestJson = """
            {
              "email": "example@user-updated.com",
              "name": "",
              "role": "ROLE_USER"
            }
            """;

        mockMvc.perform(put("/api/users/{id}", userToUpdate.getId())
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

    }

    //UPDATE USER "HTTP 400" - BAD REQUEST ( EMAIL )

    @Test
    void shouldNotUpdateUser_InvalidEmail() throws Exception {

        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        assertTrue(adminOpt.isPresent());
        User admin = adminOpt.get();

        String token = jwtService.generateAccessToken(admin);

        User userToUpdate = userRepository.save(UserObject.testUser());

        String requestJson = """
            {
              "email": "example-user-updated-com",
              "name": "example-user-updated",
              "role": "ROLE_USER"
            }
            """;

        mockMvc.perform(put("/api/users/{id}", userToUpdate.getId())
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

    }

    //UPDATE USER "HTTP 400" - BAD REQUEST ( ROLE )

    @Test
    void shouldNotUpdateUser_InvalidPassword() throws Exception {

        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        assertTrue(adminOpt.isPresent());
        User admin = adminOpt.get();

        String token = jwtService.generateAccessToken(admin);

        User userToUpdate = userRepository.save(UserObject.testUser());

        String requestJson = """
            {
              "email": "example@user-updated.com",
              "name": "example-user-updated",
              "role": "ROLE_ADMIN"
            }
            """;

        mockMvc.perform(put("/api/users/{id}", userToUpdate.getId())
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

    }

    //UPDATE USER "HTTP 401" - UNAUTHORIZED ( TOKEN )

    //CHANGE USER PASSWORD "HTTP 401" - UNAUTHORIZED ( API KEY )




}
