package com.aiagent.gateway.document.ai;

public record AiIndexResponse(
        String document_id,
        int chunk_count
) {}