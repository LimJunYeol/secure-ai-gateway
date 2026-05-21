package com.aiagent.gateway.auth.filter;

import com.aiagent.gateway.auth.service.JwtTokenProvider;
import com.aiagent.gateway.common.tenant.TenantContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 매 요청마다 JWT를 검증하고, 유효하면:
 * 1. TenantContext에 tenantId 주입 (멀티테넌시 격리 활성화)
 * 2. SecurityContext에 인증 정보 설정
 *
 * 요청 종료 시 TenantContext를 반드시 정리 (스레드 풀 오염 방지).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        try {
            if (token != null && jwtTokenProvider.isValid(token)) {
                Claims claims = jwtTokenProvider.parseClaims(token);

                UUID userId = UUID.fromString(claims.getSubject());
                UUID tenantId = UUID.fromString(claims.get("tenantId", String.class));
                String role = claims.get("role", String.class);

                // 1. 멀티테넌시 격리 활성화
                TenantContext.set(tenantId);

                // 2. Spring Security 인증 정보 설정
                var authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Authenticated: userId={}, tenantId={}, role={}", userId, tenantId, role);
            }

            filterChain.doFilter(request, response);

        } finally {
            // 3. 스레드 풀 재사용 대비 — 반드시 정리
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}