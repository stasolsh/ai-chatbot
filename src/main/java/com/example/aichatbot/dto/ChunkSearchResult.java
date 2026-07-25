package com.example.aichatbot.dto;

public record ChunkSearchResult(
        String id,
        String content,
        double score
) {
}
