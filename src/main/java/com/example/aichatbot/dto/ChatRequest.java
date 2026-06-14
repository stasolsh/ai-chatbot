package com.example.aichatbot.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank String message
) {
}
