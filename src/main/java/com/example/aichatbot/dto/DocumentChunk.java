package com.example.aichatbot.dto;

public record DocumentChunk(
        String documentId,
        int chunkNumber,
        String content
) {
}