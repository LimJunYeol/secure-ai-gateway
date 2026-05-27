package com.aiagent.gateway.document.service;

import com.aiagent.gateway.common.security.SecurityUtils;
import com.aiagent.gateway.document.domain.*;
import com.aiagent.gateway.document.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aiagent.gateway.document.ai.AiServiceClient;
import com.aiagent.gateway.document.ai.AiIndexRequest;
import com.aiagent.gateway.document.ai.AiIndexResponse;
import java.util.Base64;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // docx
            "text/plain",
            "text/markdown"
    );
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB

    private final DocumentRepository documentRepository;
    private final DocumentAclRepository aclRepository;
    private final FileStorage fileStorage;
    private final AiServiceClient aiServiceClient;

    @Transactional
    public Document upload(MultipartFile file, List<UUID> allowedGroupIds) {
        validate(file);

        UUID uploaderId = SecurityUtils.getCurrentUserId();

        // 1. 메타데이터 저장
        Document document = documentRepository.save(
                Document.builder()
                        .uploaderId(uploaderId)
                        .filename(file.getOriginalFilename())
                        .storagePath("temp")
                        .mimeType(file.getContentType())
                        .fileSize(file.getSize())
                        .build()
        );

        // 2. 실제 파일 저장
        String key = document.getTenantId() + "/" + document.getId() + "_" + file.getOriginalFilename();
        fileStorage.store(file, key);
        document.assignStoragePath(key);

        // 3. ACL 저장
        if (allowedGroupIds != null) {
            for (UUID groupId : allowedGroupIds) {
                aclRepository.save(
                        DocumentAcl.builder()
                                .documentId(document.getId())
                                .groupId(groupId)
                                .build()
                );
            }
        }

        // 4. AI 서비스로 인덱싱 요청
        indexDocument(document, file);

        log.info("Document uploaded & indexed: id={}, filename={}",
                document.getId(), document.getFilename());

        return document;
    }
    /**
     * Python AI 서비스에 인덱싱 요청하고 결과로 상태 갱신.
     */
    private void indexDocument(Document document, MultipartFile file) {
        document.markIndexing();
        try {
            byte[] bytes = file.getBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);

            AiIndexRequest request = new AiIndexRequest(
                    document.getTenantId().toString(),
                    document.getId().toString(),
                    base64,
                    document.getMimeType()
            );

            AiIndexResponse response = aiServiceClient.index(request);
            document.markIndexed(response.chunk_count());
            log.info("Indexed document {}: {} chunks", document.getId(), response.chunk_count());

        } catch (Exception e) {
            log.error("Indexing failed for document {}", document.getId(), e);
            document.markFailed(e.getMessage());
            // 인덱싱 실패해도 업로드 자체는 성공으로 둠 (재시도 가능하게)
        }
    }

    @Transactional(readOnly = true)
    public List<Document> listMyDocuments() {
        // Hibernate Filter가 자동으로 tenant_id 격리
        return documentRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Document getDocument(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다"));
    }

    @Transactional
    public void delete(UUID documentId) {
        Document document = getDocument(documentId);

        // Qdrant 벡터 삭제 (실패해도 문서 삭제는 진행)
        try {
            aiServiceClient.delete(
                    document.getTenantId().toString(),
                    documentId.toString()
            );
        } catch (Exception e) {
            log.warn("Failed to delete vectors for document {}", documentId, e);
        }

        aclRepository.deleteByDocumentId(documentId);
        fileStorage.delete(document.getStoragePath());
        documentRepository.delete(document);
        log.info("Document deleted: id={}", documentId);
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 20MB를 초과할 수 없습니다");
        }
        if (!ALLOWED_MIME_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException(
                    "지원하지 않는 파일 형식입니다: " + file.getContentType());
        }
    }
}