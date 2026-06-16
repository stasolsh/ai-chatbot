package com.example.aichatbot.dto;

public record StoredChunk(
        String id,
        String documentId,
        int chunkNumber,
        String content,
        float[] embedding
) {
}
