package it.frs.auth.sys.config;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter for limiting the number of incoming requests to the server (rate limiting).
 * <p>
 * Uses the Bucket4j library to control the number of requests allowed within a certain time window.
 * If the limit is exceeded, an HTTP 429 (Too Many Requests) status is returned.
 * <p>
 * This filter runs once per HTTP request, as it extends {@link OncePerRequestFilter}.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    /**
     * Bucket responsible for tracking and limiting requests.
     */
    private final Bucket bucket;

    /**
     * Constructor for the filter.
     *
     * @param bucket the Bucket used for rate limiting
     */
    public RateLimitingFilter(Bucket bucket) {
        this.bucket = bucket;
    }

    /**
     * Core logic of the filter.
     * <p>
     * If the bucket allows the request (tryConsume returns true), the request is
     * forwarded down the filter chain. Otherwise, HTTP status 429 is returned
     * with the message "Too many requests".
     *
     * @param request     the HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain to forward the request to
     * @throws ServletException if a filter error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("text/plain");
            response.getWriter().write("Too many requests");
        }
    }
}
