package com.aiagent.gateway.chat.service;

import com.aiagent.gateway.common.tenant.TenantContext;
import com.aiagent.gateway.document.ai.AiQueryRequest;
import com.aiagent.gateway.document.ai.AiQueryResponse;
import com.aiagent.gateway.document.ai.AiServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final AiServiceClient aiServiceClient;

    public AiQueryResponse ask(String query, int limit) {
        String tenantId = TenantContext.get().toString();

        AiQueryResponse response = aiServiceClient.query(
                new AiQueryRequest(tenantId, query, limit)
        );

        log.info("Chat query answered: '{}' -> {} citations",
                query, response.citations().size());

        return response;
    }
}