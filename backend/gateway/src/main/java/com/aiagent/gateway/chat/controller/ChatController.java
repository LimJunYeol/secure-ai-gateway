package com.aiagent.gateway.chat.controller;

import com.aiagent.gateway.chat.service.ChatService;
import com.aiagent.gateway.document.ai.AiQueryResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/ask")
    public AiQueryResponse ask(@RequestBody AskRequest request) {
        int limit = request.limit() != null ? request.limit() : 5;
        return chatService.ask(request.query(), limit);
    }

    public record AskRequest(
            @NotBlank String query,
            Integer limit
    ) {}
}