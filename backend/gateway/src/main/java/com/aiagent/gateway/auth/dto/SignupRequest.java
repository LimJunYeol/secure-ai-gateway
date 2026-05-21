package com.aiagent.gateway.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "회사명은 필수입니다")
        @Size(max = 100)
        String companyName,

        @NotBlank(message = "도메인은 필수입니다")
        @Pattern(regexp = "^[a-z][a-z0-9-]{2,49}$",
                message = "도메인은 소문자 시작, 영문/숫자/하이픈만 가능 (3-50자)")
        String domain,

        @NotBlank @Email
        String adminEmail,

        @NotBlank
        @Size(min = 8, max = 100, message = "비밀번호는 8자 이상")
        String adminPassword,

        @NotBlank
        @Size(max = 100)
        String adminName
) {}