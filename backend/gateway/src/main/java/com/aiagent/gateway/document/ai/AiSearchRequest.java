package com.aiagent.gateway.document.ai;

public record AiSearchRequest(
        String tenant_id,
        String query,
        int limit
) {}