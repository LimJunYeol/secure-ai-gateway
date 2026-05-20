package com.aiagent.gateway.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 개발 단계 임시 보안 설정.
 * 목요일에 JWT 인증 필터 추가하면서 본격 설정으로 교체 예정.
 * 지금은 멀티테넌시 격리 동작 검증 목적으로 모든 엔드포인트 허용.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // REST API는 CSRF 보호 불필요 (토큰 기반 인증 사용 예정)
                .csrf(csrf -> csrf.disable())

                // JWT 기반이라 세션 안 씀 (Stateless)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 기본 폼 로그인, HTTP Basic 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 개발 단계: 모든 요청 허용 (목요일에 변경)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}