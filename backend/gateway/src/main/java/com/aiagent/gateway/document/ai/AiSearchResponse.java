package com.aiagent.gateway.document.ai;

import java.util.List;

public record AiSearchResponse(
        List<Hit> hits
) {
    public record Hit(
            String document_id,
            int chunk_index,
            String content,
            double score
    ) {}
}