package com.example.aichatbot.dto;

public record DocumentSource(
        String citation,
        String documentId,
        String sourceName,
        int chunkNumber,
        String excerpt,
        double score
) {
}
