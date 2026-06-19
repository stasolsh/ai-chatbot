package com.example.aichatbot.service;

public sealed interface DocumentSearchService permits  DocumentSearchServiceImpl {
    String findRelevantContext(String question);
}
