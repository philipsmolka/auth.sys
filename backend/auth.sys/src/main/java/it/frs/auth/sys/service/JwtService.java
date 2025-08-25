package it.frs.auth.sys.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import it.frs.auth.sys.model.User;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service responsible for creating, parsing, and validating JSON Web Tokens (JWT).
 * It handles the generation of access, refresh, and other special-purpose tokens.
 */
@Service
@Slf4j
@SuppressWarnings("unused")
public class JwtService {

    /**
     * A secret key used for signing and verifying JWTs, injected from application properties.
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Validity duration for an access token, in milliseconds, injected from application properties.
     */
    @Value("${jwt.expiration.access-token}")
    private long accessTokenValidity;

    /**
     * Validity duration for a refresh token, in milliseconds, injected from application properties.
     */
    @Value("${jwt.expiration.refresh-token}")
    private long refreshTokenValidity;

    /**
     * Validity duration for a password reset token, in milliseconds, injected from application properties.
     */
    @Value("${jwt.expiration.password-reset-token}")
    private long passwordResetTokenValidity;

    /**
     * A generic time value, typically for short-lived tokens, injected from application properties.
     */
    @Value("${jwt.expiration.time}")
    private long hour;

    /**
     * The cryptographic key used for signing JWTs, generated from the secretKey.
     */
    private Key signingKey;

    /**
     * Initializes the service by creating the signing key from the configured secret.
     * This method is automatically called after the service is constructed and dependencies are injected.
     */
    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        log.info("JWT Key has been initialized");
    }

    // --- Public Methods ---

    /**
     * Generates a JWT access token for a given user.
     * The token includes the user's role as a custom claim.
     *
     * @param user The user for whom the token is generated.
     * @return A signed JWT access token as a String.
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRoles());
        return createToken(claims, user.getEmail(), accessTokenValidity);
    }

    /**
     * Generates a JWT refresh token for a given user.
     * This token has a longer validity period and does not contain extra claims.
     *
     * @param user The user for whom the token is generated.
     * @return A signed JWT refresh token as a String.
     */
    public String generateRefreshToken(User user) {
        return createToken(new HashMap<>(), user.getEmail(), refreshTokenValidity);
    }

    /**
     * Generates a short-lived, single-purpose token for password reset requests.
     *
     * @param email The email of the user requesting a password reset.
     * @return A signed JWT password reset token.
     */
    public String generatePasswordResetToken(String email) {
        return createToken(new HashMap<>(), email, passwordResetTokenValidity);
    }

    /**
     * Generates a short-lived token for account confirmation purposes.
     *
     * @param email The email of the user whose account is to be confirmed.
     * @return A signed JWT account confirmation token.
     */
    public String generateAccountConfirmationToken(String email) {
        return createToken(new HashMap<>(), email, hour);
    }

    /**
     * Validates a given token by checking its expiration and if the email in the token
     * matches the email of the provided user.
     *
     * @param token The JWT to validate.
     * @param user  The user to validate against.
     * @return {@code true} if the token is valid and belongs to the user, {@code false} otherwise.
     */
    public boolean isTokenValid(String token, User user) {
        final String username = extractEmail(token);
        return (username.equals(user.getEmail())) && !isTokenExpired(token);
    }

    /**
     * Extracts the email (subject) from the JWT claims.
     *
     * @param token The JWT from which to extract the email.
     * @return The email address stored as the subject of the token.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // --- Private Methods ---

    /**
     * A generic function to extract a specific claim from a JWT.
     *
     * @param token          The JWT to parse.
     * @param claimsResolver A function that takes the claims and returns a specific value of type T.
     * @param <T>            The type of the claim to be returned.
     * @return The extracted claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * The core token creation method. Builds and signs a JWT with the given parameters.
     *
     * @param claims           Custom claims to include in the token's payload.
     * @param subject          The subject of the token (typically the user's email).
     * @param validityInMillis The token's validity period in milliseconds.
     * @return A compact, URL-safe, signed JWT string.
     */
    private String createToken(Map<String, Object> claims, String subject, long validityInMillis) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + validityInMillis))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Checks if a token has expired by comparing its expiration date with the current time.
     *
     * @param token The JWT to check.
     * @return {@code true} if the token is expired, {@code false} otherwise.
     * Returns {@code true} if the expiration date cannot be parsed, treating it as invalid.
     */
    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.warn("Error while checking token expiration date: {}", e.getMessage());
            return true; // If the date cannot be read, we consider the token invalid/expired.
        }
    }

    /**
     * Extracts the expiration date from a token's claims.
     *
     * @param token The JWT to parse.
     * @return The expiration {@link Date} of the token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parses the JWT and returns all of its claims. This method will throw various
     * JWT-specific exceptions (e.g., ExpiredJwtException, SignatureException) if the token
     * is invalid, which should be handled by a global exception handler.
     *
     * @param token The JWT string to parse.
     * @return The {@link Claims} object containing all data from the token's payload.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}