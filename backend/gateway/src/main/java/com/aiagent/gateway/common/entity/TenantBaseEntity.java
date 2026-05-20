package com.aiagent.gateway.common.entity;

import com.aiagent.gateway.common.tenant.TenantAware;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import jakarta.persistence.EntityListeners;
import com.aiagent.gateway.common.tenant.TenantEntityListener;

import java.util.UUID;

/**
 * 테넌트 격리가 적용되는 엔티티의 베이스 클래스.
 * - tenant_id 컬럼 자동 포함
 * - Hibernate Filter "tenantFilter"가 활성화되면 자동으로 WHERE 추가
 * - 저장 전 TenantEntityListener가 TenantContext에서 자동 주입
 */
@MappedSuperclass
@EntityListeners(TenantEntityListener.class)
@FilterDef(
        name = "tenantFilter",
        parameters = @ParamDef(name = "tenantId", type = UUID.class)
)
@Filter(
        name = "tenantFilter",
        condition = "tenant_id = :tenantId"
)
@Getter
@Setter
public abstract class TenantBaseEntity extends BaseEntity implements TenantAware {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;
}