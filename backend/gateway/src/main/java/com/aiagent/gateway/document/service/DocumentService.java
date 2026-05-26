package com.aiagent.gateway.document.service;

import com.aiagent.gateway.common.security.SecurityUtils;
import com.aiagent.gateway.document.domain.*;
import com.aiagent.gateway.document.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    @Transactional
    public Document upload(MultipartFile file, List<UUID> allowedGroupIds) {
        validate(file);

        UUID uploaderId = SecurityUtils.getCurrentUserId();

        // 1. 메타데이터 먼저 저장 (id 확보)
        Document document = documentRepository.save(
                Document.builder()
                        .uploaderId(uploaderId)
                        .filename(file.getOriginalFilename())
                        .storagePath("temp")  // 곧 갱신
                        .mimeType(file.getContentType())
                        .fileSize(file.getSize())
                        .build()
        );

        // 2. 실제 파일 저장 (key = documentId 기반)
        String key = document.getTenantId() + "/" + document.getId() + "_" + file.getOriginalFilename();
        fileStorage.store(file, key);

        // 3. storagePath 갱신 (더티 체킹으로 자동 UPDATE)
        // Document에 setter 없으니 재빌드 대신 메소드 필요 → 간단히 새 메소드 추가 권장
        // 여기선 일단 별도 메소드로 처리 (아래 STEP에서 Document에 추가)
        document.assignStoragePath(key);

        // 4. 접근 권한(ACL) 저장
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

        log.info("Document uploaded: id={}, filename={}, uploader={}",
                document.getId(), document.getFilename(), uploaderId);

        return document;
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