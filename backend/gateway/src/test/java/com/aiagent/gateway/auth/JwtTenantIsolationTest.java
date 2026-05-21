package com.aiagent.gateway.auth;

import com.aiagent.gateway.auth.dto.SignupRequest;
import com.aiagent.gateway.auth.dto.TokenResponse;
import com.aiagent.gateway.auth.service.AuthService;
import com.aiagent.gateway.common.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JwtTenantIsolationTest {

    @Autowired private AuthService authService;
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
        jdbcTemplate.execute("TRUNCATE TABLE users, tenants RESTART IDENTITY CASCADE");
    }

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("JWT로 /me 호출 시 본인 정보가 나온다")
    void meReturnsOwnInfo() {
        TokenResponse token = authService.signup(new SignupRequest(
                "Acme", "acme", "admin@acme.com", "password123", "Alice"
        ));
        TenantContext.clear(); // signup이 set한 거 정리

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token.accessToken());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/users/me", HttpMethod.GET,
                new HttpEntity<>(headers), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("admin@acme.com");
        assertThat(response.getBody()).contains("ADMIN");
    }

    @Test
    @DisplayName("토큰 없이 /me 호출 시 거부된다")
    void meWithoutTokenIsRejected() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/users/me", HttpMethod.GET,
                HttpEntity.EMPTY, String.class
        );

        assertThat(response.getStatusCode())
                .isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("무효한 토큰은 거부된다")
    void invalidTokenIsRejected() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("garbage.token.here");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/users/me", HttpMethod.GET,
                new HttpEntity<>(headers), String.class
        );

        assertThat(response.getStatusCode())
                .isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }
}