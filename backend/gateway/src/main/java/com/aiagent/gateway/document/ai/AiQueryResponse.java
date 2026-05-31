package com.aiagent.gateway.document.ai;

import java.util.List;

public record AiQueryResponse(
        String answer,
        List<Citation> citations
) {
    public record Citation(
            String document_id,
            int chunk_index,
            String content,
            double score
    ) {}
}