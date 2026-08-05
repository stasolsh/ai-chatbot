package com.example.aichatbot.dto;

public record SearchResult(
        String id,
        String documentId,
        String sourceName,
        int chunkNumber,
        String content,
        double score
) {
}
