package com.aiagent.gateway.tenant.domain;

import com.aiagent.gateway.common.entity.TenantBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "test_notes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TestNote extends TenantBaseEntity {

    @Column(nullable = false, length = 200)
    private String content;

    @Builder
    public TestNote(String content) {
        this.content = content;
    }
}