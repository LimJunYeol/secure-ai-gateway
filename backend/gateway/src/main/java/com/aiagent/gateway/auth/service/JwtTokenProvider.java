package com.aiagent.gateway.auth.service;

import com.aiagent.gateway.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    private final String secret;
    private final Duration accessTokenTtl;

    private SecretKey signingKey;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-ttl-minutes}") long accessTokenTtlMinutes
    ) {
        this.secret = secret;
        this.accessTokenTtl = Duration.ofMinutes(accessTokenTtlMinutes);
    }

    @PostConstruct
    void init() {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        log.info("JwtTokenProvider initialized (TTL: {} minutes)", accessTokenTtl.toMinutes());
    }

    /**
     * Access Token 발급.
     * Claims: sub(userId), tenantId, role, email, name
     */
    public String createAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(accessTokenTtl);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("tenantId", user.getTenantId().toString())
                .claim("role", user.getRole().name())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    /**
     * 토큰에서 Claims 추출. 검증 실패 시 예외.
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰에서 userId 추출.
     */
    public UUID getUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    /**
     * 토큰에서 tenantId 추출 (멀티테넌시 인프라에 주입할 값).
     */
    public UUID getTenantId(String token) {
        return UUID.fromString(parseClaims(token).get("tenantId", String.class));
    }

    /**
     * 만료/위조 토큰 검증.
     */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }
}