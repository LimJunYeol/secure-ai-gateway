package com.aiagent.gateway.document.ai;

public record AiQueryRequest(
        String tenant_id,
        String query,
        int limit
) {}