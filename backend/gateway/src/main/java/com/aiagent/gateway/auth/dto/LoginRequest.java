package com.aiagent.gateway.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Email
        String email,

        @NotBlank
        String password,

        @NotBlank
        String domain  // 어느 회사 소속인지
) {}