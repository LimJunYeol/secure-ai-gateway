package com.aiagent.gateway.auth.service;

import com.aiagent.gateway.auth.dto.LoginRequest;
import com.aiagent.gateway.auth.dto.SignupRequest;
import com.aiagent.gateway.auth.dto.TokenResponse;
import com.aiagent.gateway.common.tenant.TenantContext;
import com.aiagent.gateway.tenant.domain.Tenant;
import com.aiagent.gateway.tenant.domain.TenantRepository;
import com.aiagent.gateway.user.domain.User;
import com.aiagent.gateway.user.domain.UserRepository;
import com.aiagent.gateway.user.domain.UserRole;
import com.aiagent.gateway.user.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회사 등록 + 첫 Admin 사용자 생성.
     * TenantContext 없이 호출됨 (가입 시점엔 토큰이 없음).
     */
    @Transactional
    public TokenResponse signup(SignupRequest request) {
        // 1. 도메인 중복 검사
        if (tenantRepository.existsByDomain(request.domain())) {
            throw new IllegalArgumentException("이미 사용 중인 도메인입니다: " + request.domain());
        }

        // 2. Tenant 생성
        Tenant tenant = tenantRepository.save(
                Tenant.builder()
                        .name(request.companyName())
                        .domain(request.domain())
                        .build()
        );
        log.info("Tenant created: id={}, domain={}", tenant.getId(), tenant.getDomain());

        // 3. TenantContext 임시 설정 (User 저장에 필요)
        TenantContext.set(tenant.getId());
        try {
            User admin = userRepository.save(
                    User.builder()
                            .email(request.adminEmail())
                            .passwordHash(passwordEncoder.encode(request.adminPassword()))
                            .name(request.adminName())
                            .role(UserRole.ADMIN)
                            .build()
            );
            log.info("First admin created: id={}, email={}", admin.getId(), admin.getEmail());

            return issueToken(admin);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 로그인. 도메인 + 이메일 + 비밀번호 검증.
     */
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        // 1. 도메인으로 테넌트 찾기
        Tenant tenant = tenantRepository.findByDomain(request.domain())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도메인입니다"));

        // 2. TenantContext 설정 (이메일 검색에 필요)
        TenantContext.set(tenant.getId());
        try {
            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다"));

            // 3. 비밀번호 검증
            if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다");
            }

            // 4. 상태 검증
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new IllegalStateException("비활성화된 계정입니다");
            }

            return issueToken(user);
        } finally {
            TenantContext.clear();
        }
    }

    private TokenResponse issueToken(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user);
        long expiresInSeconds = 60 * 60; // 1시간 (TTL과 맞춰야 함, 추후 상수화)
        return TokenResponse.bearer(accessToken, expiresInSeconds);
    }
}