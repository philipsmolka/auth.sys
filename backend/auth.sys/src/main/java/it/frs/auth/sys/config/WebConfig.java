package it.frs.auth.sys.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Web MVC configuration class.
 * <p>
 * Registers custom interceptors for handling requests.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    /**
     * Logging interceptor that will be applied to all incoming HTTP requests.
     */
    private final LoggingInterceptor loggingInterceptor;

    /**
     * Adds custom interceptors to the application's interceptor registry.
     * <p>
     * Currently, it registers {@link #loggingInterceptor} to apply to all request paths.
     *
     * @param registry the {@link InterceptorRegistry} to which interceptors can be added
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Add the interceptor to apply to all paths
        registry.addInterceptor(loggingInterceptor);
    }
}
