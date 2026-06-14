package com.example.aichatbot.dto;

public record DocumentUploadResponse(
        String fileName,
        long size,
        int characters,
        String preview
) {
}
