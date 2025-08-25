package it.frs.auth.sys.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration class for setting up rate limiting using Bucket4j.
 * <p>
 * Defines a bucket that limits the number of requests per minute.
 * This bucket can be injected into a filter to enforce rate limiting on API endpoints.
 */
@Configuration
public class RateLimitingConfig {

    /**
     * Creates a new {@link Bucket} with a simple rate limit.
     * <p>
     * Currently set to allow 100 requests per minute.
     *
     * @return a configured {@link Bucket} instance for rate limiting
     */
    @Bean
    public Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.simple(100, Duration.ofMinutes(1));
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

}
