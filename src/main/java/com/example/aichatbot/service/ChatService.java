package com.example.aichatbot.service;

public sealed interface ChatService permits ChatServiceImpl {
    String chat(String sessionId, String message);
    void clearMemory(String sessionId);
}
