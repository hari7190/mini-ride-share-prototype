package com.zamorincorp.rideshare.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtProviderTest {

    private static final UUID USER_ID =
            UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

    private static final String JWT_SECRET =
            "test-secret-key-must-be-at-least-32-bytes-long";

    private final JwtProvider jwtProvider = new JwtProvider();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtProvider, "jwtSecret", JWT_SECRET);
        ReflectionTestUtils.setField(jwtProvider, "jwtExpiration", 3600000L);
    }

    @Test
    void generateToken_includesUserIdAsSubject() {
        String token = jwtProvider.generateToken(USER_ID);

        assertNotNull(token);
        assertFalse(token.isBlank());

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        assertEquals(USER_ID.toString(), claims.getSubject());
    }

}
