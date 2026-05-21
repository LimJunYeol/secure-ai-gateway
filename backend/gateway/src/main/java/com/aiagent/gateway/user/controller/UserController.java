package com.aiagent.gateway.user.controller;

import com.aiagent.gateway.common.security.SecurityUtils;
import com.aiagent.gateway.common.tenant.TenantContext;
import com.aiagent.gateway.user.domain.User;
import com.aiagent.gateway.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /**
     * 현재 로그인한 사용자 정보 반환.
     * JWT에서 자동으로 TenantContext가 설정돼 있어서, 조회 시 자동으로 테넌트 격리됨.
     */
    @GetMapping("/me")
    @Transactional(readOnly = true)
    public MeResponse me() {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다"));

        return new MeResponse(
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );
    }

    public record MeResponse(
            UUID userId,
            UUID tenantId,
            String email,
            String name,
            String role
    ) {}
}