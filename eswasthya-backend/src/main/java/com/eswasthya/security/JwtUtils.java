package com.eswasthya.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT token utility using jjwt 0.12.x fluent API.
 *
 * <p>Handles token generation, username extraction, and validation.</p>
 */
@Component
@Slf4j
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    // Key

    private SecretKey signingKey() {
        // Pad secret to at least 32 bytes (256 bits) required for HmacSHA256
        byte[] rawBytes = jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] keyBytes = java.util.Arrays.copyOf(rawBytes, Math.max(rawBytes.length, 32));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Generate

    /**
     * Generate a signed JWT for the given username.
     *
     * @param username principal subject
     * @return compact JWT string
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey())
                .compact();
    }

    // Extract

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }

    // Validate

    /**
     * Returns true if the token is valid and matches the given UserDetails.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = getUsernameFromToken(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (SignatureException e) {
            log.warn("JWT: invalid signature");
        } catch (MalformedJwtException e) {
            log.warn("JWT: malformed token");
        } catch (ExpiredJwtException e) {
            log.warn("JWT: token expired");
        } catch (UnsupportedJwtException e) {
            log.warn("JWT: unsupported token");
        } catch (IllegalArgumentException e) {
            log.warn("JWT: empty claims string");
        }
        return false;
    }

    // Internal

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }
}
