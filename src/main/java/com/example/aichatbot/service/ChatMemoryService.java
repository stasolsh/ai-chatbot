package com.example.aichatbot.service;

import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

public interface ChatMemoryService {

    List<ChatMessage> getMessages(
            String userId,
            String sessionId
    );

    void addUserMessage(
            String userId,
            String sessionId,
            String message
    );

    void addAiMessage(
            String userId,
            String sessionId,
            String answer
    );

    void clear(
            String userId,
            String sessionId
    );
}
