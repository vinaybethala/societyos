package com.societyos.security;

import com.societyos.config.JwtConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private JwtConfigProperties jwtConfig;

    @BeforeEach
    void setUp() {
        jwtConfig = new JwtConfigProperties();
        jwtConfig.setSecret("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        jwtConfig.setExpirationMs(3600000L); // 1 hour
        jwtConfig.setIssuer("SocietyOS-Test");

        jwtService = new JwtService(jwtConfig);
    }

    @Test
    @DisplayName("Should generate valid JWT token with proper claims")
    void shouldGenerateValidToken() {
        UserPrincipal principal = UserPrincipal.builder()
                .id(100L)
                .username("resident1")
                .email("resident1@example.com")
                .authorities(Set.of(new SimpleGrantedAuthority("ROLE_RESIDENT")))
                .enabled(true)
                .build();

        String token = jwtService.generateToken(principal);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("resident1");
        assertThat(jwtService.extractUserId(token)).isEqualTo(100L);

        List<String> roles = jwtService.extractRoles(token);
        assertThat(roles).containsExactly("ROLE_RESIDENT");
        assertThat(jwtService.isTokenValid(token, principal)).isTrue();
    }

    @Test
    @DisplayName("Should detect expired token accurately")
    void shouldDetectExpiredToken() {
        jwtConfig.setExpirationMs(-1000L); // Expired 1 second ago
        JwtService expiredJwtService = new JwtService(jwtConfig);

        UserPrincipal principal = UserPrincipal.builder()
                .id(1L)
                .username("expiredUser")
                .email("expired@example.com")
                .authorities(Set.of(new SimpleGrantedAuthority("ROLE_RESIDENT")))
                .enabled(true)
                .build();

        String token = expiredJwtService.generateToken(principal);

        org.junit.jupiter.api.Assertions.assertThrows(
                io.jsonwebtoken.ExpiredJwtException.class,
                () -> jwtService.isTokenValid(token, principal)
        );
    }
}

