package com.example.aichatbot.dto;

import java.util.List;

public record ChatApiResponse(
        String answer,
        List<DocumentSource> sources
) {
}