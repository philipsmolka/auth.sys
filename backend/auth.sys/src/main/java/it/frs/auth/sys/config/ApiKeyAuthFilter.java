package it.frs.auth.sys.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter for API key authentication.
 * This filter checks the presence and validity of the "X-API-KEY" header.
 * No Swagger endpoints are excluded — API key is required for all protected routes.
 */
@Slf4j
@SuppressWarnings("unused")
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final String secretKey;

    public ApiKeyAuthFilter(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestKey = request.getHeader("X-API-KEY");

        if (requestKey != null &&
                MessageDigest.isEqual(requestKey.getBytes(StandardCharsets.UTF_8),
                        secretKey.getBytes(StandardCharsets.UTF_8))) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Unauthorized Access");
        }
        log.info("API Key Filter executed on path: {}", request.getRequestURI());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }
}
