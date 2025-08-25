package it.frs.auth.sys.init;

import it.frs.auth.sys.exception.ResourceAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import it.frs.auth.sys.model.User;
import it.frs.auth.sys.repository.UserRepository;

import java.util.List;

/**
 * Initializes the administrator account at application startup.
 * <p>
 * This class runs automatically when the Spring Boot application starts and ensures
 * that a default admin account exists. If it does not exist, it is created with
 * a predefined email and password.
 * </p>
 *
 * <p>
 * <strong>Admin account details (default):</strong><br>
 * Email: {@code admin@frs.sys}<br>
 * Password: {@code Admin!@#} (encoded before saving)<br>
 * Role: {@code ROLE_ADMIN}
 * </p>
 *
 * <p>
 * If the admin account already exists, the application will throw a {@link RuntimeException}.
 * </p>
 *
 * @see org.springframework.boot.CommandLineRunner
 * @see it.frs.auth.sys.model.User
 * @see it.frs.auth.sys.repository.UserRepository
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Executes the logic to initialize an admin account at application startup.
     *
     * @param args command-line arguments passed to the application.
     * @throws RuntimeException if the admin account already exists.
     */
    @Override
    public void run(String... args) {
        String adminEmail = "admin@frs.sys";

        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("Admin!@#"));
            admin.setRoles(List.of("ROLE_ADMIN"));
            admin.setActive(true);
            userRepository.save(admin);
            log.info("Admin account has been initialized.");
        } else {
            log.info("Admin account has not been initialized.");
        }
    }
}
