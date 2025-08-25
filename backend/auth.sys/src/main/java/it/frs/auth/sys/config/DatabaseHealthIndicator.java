package it.frs.auth.sys.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import it.frs.auth.sys.repository.UserRepository;

/**
 * Health indicator for the database.
 * <p>
 * Checks the health of the application's PostgreSQL database by attempting to count the number of users.
 * Returns {@link Health#up()} if the database connection is successful, or {@link Health#down()} if an exception occurs.
 * <p>
 * This component is automatically used by Spring Boot Actuator to provide the /actuator/health endpoint.
 * <p>
 * Example endpoints provided by Spring Boot Actuator:
 * <ul>
 *     <li>Health check: <code>GET /actuator/health</code></li>
 *     <li>Metrics: <code>GET /actuator/metrics</code></li>
 *     <li>Application info: <code>GET /actuator/info</code></li>
 *     <li>Environment info: <code>GET /actuator/env</code></li>
 *     <li>Swagger API documentation: <code>GET /swagger-ui.html</code></li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final UserRepository userRepository;

    /**
     * Checks the health of the database.
     *
     * @return a {@link Health} object representing the database status
     */
    @Override
    public Health health() {
        try {
            // Check if we can connect to the database and count users
            long userCount = userRepository.count();

            return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("users", userCount)
                    .withDetail("message", "Database connection is healthy")
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("error", e.getMessage())
                    .withDetail("message", "Database connection failed")
                    .build();
        }
    }
}
