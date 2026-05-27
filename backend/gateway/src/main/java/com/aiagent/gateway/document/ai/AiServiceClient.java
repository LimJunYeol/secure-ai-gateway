package com.aiagent.gateway.document.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiServiceClient {

    private final RestClient aiServiceRestClient;

    /**
     * Python AI 서비스에 문서 인덱싱 요청.
     */
    public AiIndexResponse index(AiIndexRequest request) {
        log.info("Calling AI service /index for document {}", request.document_id());
        return aiServiceRestClient.post()
                .uri("/index")
                .body(request)
                .retrieve()
                .body(AiIndexResponse.class);
    }

    /**
     * Python AI 서비스에 검색 요청.
     */
    public AiSearchResponse search(AiSearchRequest request) {
        log.info("Calling AI service /search: query='{}'", request.query());
        return aiServiceRestClient.post()
                .uri("/search")
                .body(request)
                .retrieve()
                .body(AiSearchResponse.class);
    }

    public void delete(String tenantId, String documentId) {
        log.info("Calling AI service /delete for document {}", documentId);
        aiServiceRestClient.post()
                .uri("/delete")
                .body(new DeleteRequest(tenantId, documentId))
                .retrieve()
                .toBodilessEntity();
    }

    public record DeleteRequest(String tenant_id, String document_id) {}
}