package com.aiagent.gateway.document.domain;

import com.aiagent.gateway.common.entity.TenantBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(
        name = "document_acls",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_document_acl",
                columnNames = {"document_id", "group_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentAcl extends TenantBaseEntity {

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Builder
    public DocumentAcl(UUID documentId, UUID groupId) {
        this.documentId = documentId;
        this.groupId = groupId;
    }
}