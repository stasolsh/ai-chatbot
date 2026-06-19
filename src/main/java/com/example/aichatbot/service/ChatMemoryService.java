package com.example.aichatbot.service;

import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

public sealed interface ChatMemoryService permits ChatMemoryServiceImpl {
    List<ChatMessage> getMessages(String sessionId);

    void addUserMessage(String sessionId, String message);

    void addAiMessage(String sessionId, String answer);

    void clear(String sessionId);
}
