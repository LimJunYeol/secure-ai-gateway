package com.aiagent.gateway.document.ai;

public record AiIndexRequest(
        String tenant_id,
        String document_id,
        String file_base64,
        String mime_type
) {}