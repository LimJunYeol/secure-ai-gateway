package com.aiagent.gateway.common.tenant;

import java.util.UUID;

/**
 * 현재 요청 스레드의 tenant_id를 들고 있는 컨텍스트.
 * - 요청 들어올 때 Filter에서 set()
 * - 응답 끝날 때 Filter에서 clear() (메모리 누수 방지 + 스레드 풀 재사용 시 오염 방지)
 * - 비즈니스 로직 어디서든 get()으로 조회
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
        // 인스턴스화 금지
    }

    public static void set(UUID tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
        CURRENT_TENANT.set(tenantId);
    }

    public static UUID get() {
        UUID tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new IllegalStateException(
                    "TenantContext is not set. Did the request go through TenantContextFilter?"
            );
        }
        return tenantId;
    }

    /**
     * Filter나 테스트 외부에서는 호출 금지.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }

    /**
     * 컨텍스트가 설정되어 있는지 확인. 인증 전 또는 시스템 작업용.
     */
    public static boolean isSet() {
        return CURRENT_TENANT.get() != null;
    }
}