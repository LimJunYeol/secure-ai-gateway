package com.aiagent.gateway.common.tenant;

import java.util.UUID;

/**
 * 테넌트별로 격리되어야 하는 엔티티가 구현하는 인터페이스.
 * 구현하면:
 * - Hibernate Filter가 자동으로 WHERE tenant_id = ? 추가
 * - 저장 시점에 TenantContext에서 tenant_id 자동 주입 (TenantEntityListener)
 */
public interface TenantAware {
    UUID getTenantId();
    void setTenantId(UUID tenantId);
}