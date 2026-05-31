package com.smartcampus.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.logging.Logger;

public final class JwtUtil {

    private static final Logger LOGGER = Logger.getLogger(JwtUtil.class.getName());
    private static final long EXPIRY_MS = 3_600_000L; // 1 hour
    private static final String ISSUER = "smart-campus-api";
    private static final String DEV_FALLBACK_SECRET = "dev-only-change-me-in-production-32chars+";

    private static final SecretKey SECRET_KEY = buildKey();

    private JwtUtil() {}

    private static SecretKey buildKey() {
        String raw = System.getenv("JWT_SECRET");

        if (raw == null || raw.isBlank()) {
            raw = System.getProperty("jwt.secret");
        }

        if (raw == null || raw.isBlank()) {
            LOGGER.warning("JWT_SECRET not set — using dev fallback. Do not deploy this.");
            raw = DEV_FALLBACK_SECRET;
        }

        if (raw.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters.");
        }

        return Keys.hmacShaKeyFor(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(username)
                .issuedAt(new Date(now))
                .expiration(new Date(now + EXPIRY_MS))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static Claims validateToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}