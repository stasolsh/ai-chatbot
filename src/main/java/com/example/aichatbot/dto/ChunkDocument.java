package com.example.aichatbot.dto;

import java.util.List;

public record ChunkDocument(
        String documentId,
        int chunkIndex,
        String content,
        List<Float> embedding
) {
}
