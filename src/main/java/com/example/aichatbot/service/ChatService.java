package com.example.aichatbot.service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ChatService {

    private final ChatModel model;
    private final ChatMemoryService memoryService;

    public ChatService(ChatModel model, ChatMemoryService memoryService) {
        this.model = model;
        this.memoryService = memoryService;
    }

    public String chat(String sessionId, String message) {
        memoryService.addUserMessage(sessionId, message);
        ChatResponse response = model.chat(new ArrayList<>(memoryService.getMessages(sessionId)));
        String answer = response.aiMessage().text();
        memoryService.addAiMessage(sessionId, answer);
        return answer;
    }

    public void clearMemory(String sessionId) {
        memoryService.clear(sessionId);
    }
}
