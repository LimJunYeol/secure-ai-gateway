package com.aiagent.gateway.common.tenant;

import jakarta.persistence.PrePersist;
import lombok.extern.slf4j.Slf4j;

/**
 * 엔티티 저장 직전에 TenantContext에서 tenant_id를 자동 주입.
 * - 개발자가 entity.setTenantId() 까먹어도 안전
 * - 이미 세팅돼 있으면 덮어쓰지 않음 (테스트나 시스템 작업 대응)
 */
@Slf4j
public class TenantEntityListener {

    @PrePersist
    public void setTenantOnPersist(Object entity) {
        if (!(entity instanceof TenantAware tenantAware)) {
            return;
        }

        if (tenantAware.getTenantId() != null) {
            // 이미 명시적으로 세팅됐으면 그대로 둠
            return;
        }

        if (!TenantContext.isSet()) {
            log.warn("Saving TenantAware entity without TenantContext: {}",
                    entity.getClass().getSimpleName());
            return;
        }

        tenantAware.setTenantId(TenantContext.get());
    }
}