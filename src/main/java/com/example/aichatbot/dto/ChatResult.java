package com.example.aichatbot.dto;

import java.util.List;

public record ChatResult(
        String answer,
        List<DocumentSource> sources
) {
}
