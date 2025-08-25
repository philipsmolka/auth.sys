package it.frs.auth.sys.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that logs incoming HTTP requests and their execution details.
 * <p>
 * Uses {@link WebClient} to perform a geolocation lookup for the client's IP address.
 * Adds contextual information (IP, username, and geolocation) to the MDC for enriched logging.
 * Measures the request duration and logs it after completion.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingInterceptor implements HandlerInterceptor {

    private final WebClient webClient;

    private static final String REQUEST_START_TIME = "requestStartTime";

    /**
     * Called before the actual handler is executed.
     * <p>
     * Logs the request start time, client IP, authenticated username, and geolocation.
     *
     * @param request  the incoming {@link HttpServletRequest}
     * @param response the outgoing {@link HttpServletResponse}
     * @param handler  chosen handler to execute
     * @return {@code true} to continue processing the request
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {

        long startTime = System.currentTimeMillis();
        request.setAttribute(REQUEST_START_TIME, startTime);

        String ipAddress = getClientIp(request);
        String username = getUsername();
        String geo;

        try {
            geo = webClient.get()
                    .uri("http://ip-api.com/json/" + ipAddress)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // synchronous call
        } catch (Exception error) {
            geo = "UNKNOWN";
        }

        MDC.put("clientIP", ipAddress);
        MDC.put("user", username);
        MDC.put("geo", geo);

        log.info("Request START: {} {} from IP: {} by user: {} (geo: {})",
                request.getMethod(),
                request.getRequestURI(),
                ipAddress,
                username,
                geo);

        return true;
    }

    /**
     * Called after the handler is executed.
     * <p>
     * Logs the request end time, HTTP status, and duration.
     * Clears the MDC context.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @param handler  the handler that was executed
     * @param ex       any exception thrown during request processing, or {@code null}
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {

        long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Request END: {} {} resulted in status {} and took {}ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration);

        MDC.clear();
    }

    /**
     * Extracts the client IP address from the request.
     * <p>
     * Checks the "X-FORWARDED-FOR" header first, then falls back to the remote address.
     *
     * @param request the incoming {@link HttpServletRequest}
     * @return the client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getHeader("X-FORWARDED-FOR");
        return (remoteAddr == null || remoteAddr.isBlank())
                ? request.getRemoteAddr()
                : remoteAddr;
    }

    /**
     * Retrieves the username of the currently authenticated user.
     *
     * @return the username, or "ANONYMOUS/SERVER" if unauthenticated
     */
    private String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return "ANONYMOUS/SERVER";
    }
}
