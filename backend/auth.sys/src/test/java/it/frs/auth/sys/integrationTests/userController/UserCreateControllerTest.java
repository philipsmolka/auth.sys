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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "api.secret-key=test-secret-key")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class UserCreateControllerTest {

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

    //CREATE USER "HTTP 201" - HAPPY PATH

    @Test
    void shouldCreateUserSuccessfully() throws Exception {

        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        assertTrue(adminOpt.isPresent());
        User admin = adminOpt.get();

        String token = jwtService.generateAccessToken(admin);

        String requestJson = """
            {
              "email": "example@user.com",
              "name": "example-user",
              "password": "example-password",
              "role": "ROLE_USER"
            }
            """;

        mockMvc.perform(post("/api/users")
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("example@user.com"))
                .andExpect(jsonPath("$.name").value("example-user"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.active").value(true));
    }

    //CREATE USER "HTTP 409" - RESOURCE ALREADY EXISTS

    @Test
    void shouldNotCreateUser_ResourceAlreadyExists() throws Exception {

        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        assertTrue(adminOpt.isPresent());
        User admin = adminOpt.get();

        String token = jwtService.generateAccessToken(admin);

        userRepository.save(UserObject.testUser());

        String requestJson = """
            {
              "email": "example@user.com",
              "name": "example-user",
              "password": "example-password",
              "role": "ROLE_USER"
            }
            """;

        mockMvc.perform(post("/api/users")
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict());
    }

    //CREATE USER "HTTP 403" - NO PERMISSION

    @Test
    void shouldNotCreateUser_NoPermission() throws Exception {

        userRepository.save(UserObject.testUser());

        String token = jwtService.generateAccessToken(UserObject.testUser());

        String requestJson = """
            {
              "email": "example@user.com",
              "name": "example-user",
              "password": "example-password",
              "role": "ROLE_USER"
            }
            """;

        mockMvc.perform(post("/api/users")
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    //CREATE USER "HTTP 400" - BAD REQUEST

    @Test
    void shouldNotCreateUser_InvalidData() throws Exception {
        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        User admin = adminOpt.get();
        String token = jwtService.generateAccessToken(admin);

        String requestJson = """
        {
          "email": "bad-email-format",
          "name": "example-user",
          "password": "123",
          "role": "ROLE_USER"
        }
        """;

        mockMvc.perform(post("/api/users")
                        .header("X-API-KEY", "test-secret-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    //CREATE USER "HTTP 401" - UNAUTHORIZED ( TOKEN )

    @Test
    void shouldNotCreateUser_InvalidToken() throws Exception {
        String requestJson = """
        {
          "email": "example@user.com",
          "name": "example-user",
          "password": "example-password",
          "role": "ROLE_USER"
        }
        """;

        mockMvc.perform(post("/api/users")
                        .header("X-API-KEY", "test-secret-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized());
    }

    //CREATE USER "HTTP 401" - UNAUTHORIZED ( API KEY )

    @Test
    void shouldNotCreateUser_InvalidApiKey() throws Exception {
        Optional<User> adminOpt = userRepository.findByEmail("admin@frs.sys");
        User admin = adminOpt.get();
        String token = jwtService.generateAccessToken(admin);

        String requestJson = """
        {
          "email": "example@user.com",
          "name": "example-user",
          "password": "example-password",
          "role": "ROLE_USER"
        }
        """;

        mockMvc.perform(post("/api/users")
                        .header("X-API-KEY", "wrong-key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized());
    }

}
