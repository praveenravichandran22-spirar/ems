package com.minifullstack.ems.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.ExpiredJwtException;

import static org.assertj.core.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    private static final String SECRET =
            "3f8a2b1c9d4e7f0a5b6c2d3e8f1a4b7c9d2e5f0a3b6c8d1e4f7a0b2c5d8e1f4";
    private static final long EXPIRATION_MS = 300_000L;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtils, "expirationMs", EXPIRATION_MS);
    }

    private UserDetails user(String email, String role) {
        return User.builder().username(email).password("pass").authorities(role).build();
    }

    @Test
    void generateToken_returnsNonBlankToken() {
        String token = jwtUtils.generateToken(user("a@b.com", "ROLE_ADMIN"));
        assertThat(token).isNotBlank();
    }

    @Test
    void extractUsername_returnsEmailFromToken() {
        UserDetails ud = user("user@example.com", "ROLE_USER");
        String token = jwtUtils.generateToken(ud);
        assertThat(jwtUtils.extractUsername(token)).isEqualTo("user@example.com");
    }

    @Test
    void isTokenValid_returnsTrueForMatchingUserAndFreshToken() {
        UserDetails ud = user("admin@test.com", "ROLE_ADMIN");
        String token = jwtUtils.generateToken(ud);
        assertThat(jwtUtils.isTokenValid(token, ud)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalseWhenUsernameDiffers() {
        UserDetails owner = user("owner@test.com", "ROLE_USER");
        UserDetails other = user("other@test.com", "ROLE_USER");
        String token = jwtUtils.generateToken(owner);
        assertThat(jwtUtils.isTokenValid(token, other)).isFalse();
    }

    @Test
    void isTokenValid_throwsExpiredJwtException_whenTokenExpired() {
        // JJWT throws ExpiredJwtException during parseSignedClaims — isTokenValid never returns false
        ReflectionTestUtils.setField(jwtUtils, "expirationMs", -1_000L);
        UserDetails ud = user("exp@test.com", "ROLE_USER");
        String token = jwtUtils.generateToken(ud);
        assertThatThrownBy(() -> jwtUtils.isTokenValid(token, ud))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void extractClaim_returnsRoleClaim() {
        UserDetails ud = user("reviewer@test.com", "ROLE_REVIEWER");
        String token = jwtUtils.generateToken(ud);
        String role = jwtUtils.extractClaim(token, claims -> claims.get("role", String.class));
        assertThat(role).isEqualTo("ROLE_REVIEWER");
    }

    @Test
    void generateToken_differentUsersProduceDifferentTokens() {
        String t1 = jwtUtils.generateToken(user("a@a.com", "ROLE_USER"));
        String t2 = jwtUtils.generateToken(user("b@b.com", "ROLE_USER"));
        assertThat(t1).isNotEqualTo(t2);
    }
}