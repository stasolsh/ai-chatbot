package com.example.aichatbot.dto;

import java.util.List;

public record DocumentSearchResult(
        String context,
        List<DocumentSource> sources
) {
    public static DocumentSearchResult empty() {
        return new DocumentSearchResult("", List.of());
    }
}
