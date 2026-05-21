package com.aiagent.gateway.auth;

import com.aiagent.gateway.auth.dto.LoginRequest;
import com.aiagent.gateway.auth.dto.SignupRequest;
import com.aiagent.gateway.auth.dto.TokenResponse;
import com.aiagent.gateway.auth.service.AuthService;
import com.aiagent.gateway.auth.service.JwtTokenProvider;
import com.aiagent.gateway.common.tenant.TenantContext;
import com.aiagent.gateway.tenant.domain.TenantRepository;
import com.aiagent.gateway.user.domain.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class AuthIntegrationTest {

    @Autowired private AuthService authService;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private UserRepository userRepository;
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
    @DisplayName("회사 가입 → JWT 발급되고 토큰에 tenantId/role이 들어있다")
    void signupCreatesTenantAndAdmin() {
        SignupRequest req = new SignupRequest(
                "Acme", "acme", "admin@acme.com", "password123", "Alice"
        );

        TokenResponse response = authService.signup(req);

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");

        // JWT 검증
        assertThat(jwtTokenProvider.isValid(response.accessToken())).isTrue();
        var claims = jwtTokenProvider.parseClaims(response.accessToken());
        assertThat(claims.get("role")).isEqualTo("ADMIN");
        assertThat(claims.get("email")).isEqualTo("admin@acme.com");
        assertThat(claims.get("tenantId")).isNotNull();
    }

    @Test
    @DisplayName("같은 도메인으로 두 번 가입 시도시 실패")
    void duplicateDomainFails() {
        SignupRequest req = new SignupRequest(
                "Acme", "acme", "admin@acme.com", "password123", "Alice"
        );
        authService.signup(req);

        SignupRequest dupe = new SignupRequest(
                "Other Co", "acme", "other@other.com", "password123", "Bob"
        );

        assertThatThrownBy(() -> authService.signup(dupe))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 사용 중인 도메인");
    }

    @Test
    @DisplayName("로그인 성공 시 토큰 발급")
    void loginSucceeds() {
        authService.signup(new SignupRequest(
                "Acme", "acme", "admin@acme.com", "password123", "Alice"
        ));

        TokenResponse response = authService.login(new LoginRequest(
                "admin@acme.com", "password123", "acme"
        ));

        assertThat(response.accessToken()).isNotBlank();
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 실패")
    void wrongPasswordFails() {
        authService.signup(new SignupRequest(
                "Acme", "acme", "admin@acme.com", "password123", "Alice"
        ));

        assertThatThrownBy(() -> authService.login(new LoginRequest(
                "admin@acme.com", "wrong-password", "acme"
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이메일 또는 비밀번호");
    }

    @Test
    @DisplayName("다른 회사 같은 이메일은 충돌 안 함 (멀티테넌시)")
    void sameEmailDifferentTenantsCoexist() {
        authService.signup(new SignupRequest(
                "Acme", "acme", "admin@example.com", "password123", "Alice"
        ));
        authService.signup(new SignupRequest(
                "Other", "other", "admin@example.com", "password123", "Bob"
        ));

        // 둘 다 가입 성공 = 충돌 없음
        assertThat(tenantRepository.findAll()).hasSize(2);
        assertThat(userRepository.findAll()).hasSize(2);
    }
}