package com.aiagent.gateway.document.domain;

import com.aiagent.gateway.common.entity.TenantBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document extends TenantBaseEntity {

    @Column(name = "uploader_id", nullable = false)
    private UUID uploaderId;

    @Column(nullable = false, length = 255)
    private String filename;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentStatus status;

    @Column(name = "chunk_count")
    private Integer chunkCount;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Builder
    public Document(UUID uploaderId, String filename, String storagePath,
                    String mimeType, long fileSize) {
        this.uploaderId = uploaderId;
        this.filename = filename;
        this.storagePath = storagePath;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.status = DocumentStatus.PENDING;
    }

    public void markIndexing() {
        this.status = DocumentStatus.INDEXING;
    }

    public void markIndexed(int chunkCount) {
        this.status = DocumentStatus.INDEXED;
        this.chunkCount = chunkCount;
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = DocumentStatus.FAILED;
        this.errorMessage = errorMessage != null && errorMessage.length() > 1000
                ? errorMessage.substring(0, 1000)
                : errorMessage;
    }

    public void assignStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }
}