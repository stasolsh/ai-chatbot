package com.example.aichatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank @Size(max = 100) String sessionId,
        @NotBlank @Size(max = 10_000) String message
) {
}
