package it.frs.auth.sys.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for Cross-Origin Resource Sharing (CORS).
 * <p>
 * Defines global CORS settings for the application, allowing requests from the specified origins
 * and HTTP methods.<br> This is important for enabling frontend applications (e.g., React on localhost:3000)
 * to communicate with this backend API.
 * <p>
 * Applies the CORS configuration to all endpoints under the path "/api/**".
 */
@Configuration
public class CorsConfig {

    /**
     * Provides a {@link WebMvcConfigurer} bean that configures CORS mappings.
     *
     * @return a {@link WebMvcConfigurer} with CORS settings applied
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowCredentials(true);
            }
        };
    }
}
