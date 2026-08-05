package com.example.aichatbot.service;

import com.example.aichatbot.dto.DocumentSearchResult;

public sealed interface DocumentSearchService permits  DocumentSearchServiceImpl {
    String findRelevantContext(String question);
    DocumentSearchResult search(String question);
}
