package com.aiagent.gateway.document.controller;

import com.aiagent.gateway.document.domain.Document;
import com.aiagent.gateway.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "groupIds", required = false) List<UUID> groupIds
    ) {
        Document doc = documentService.upload(file, groupIds);
        return DocumentResponse.from(doc);
    }

    @GetMapping
    public List<DocumentResponse> list() {
        return documentService.listMyDocuments().stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public DocumentResponse get(@PathVariable UUID id) {
        return DocumentResponse.from(documentService.getDocument(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        documentService.delete(id);
    }

    public record DocumentResponse(
            UUID id,
            String filename,
            String mimeType,
            long fileSize,
            String status,
            Integer chunkCount,
            Instant createdAt
    ) {
        static DocumentResponse from(Document doc) {
            return new DocumentResponse(
                    doc.getId(),
                    doc.getFilename(),
                    doc.getMimeType(),
                    doc.getFileSize(),
                    doc.getStatus().name(),
                    doc.getChunkCount(),
                    doc.getCreatedAt()
            );
        }
    }
}