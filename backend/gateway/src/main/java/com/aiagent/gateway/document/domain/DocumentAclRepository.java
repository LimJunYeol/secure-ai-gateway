package com.aiagent.gateway.document.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentAclRepository extends JpaRepository<DocumentAcl, UUID> {
    List<DocumentAcl> findByDocumentId(UUID documentId);
    void deleteByDocumentId(UUID documentId);
}