package com.example.aichatbot.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatMemoryService {

    private static final int MAX_MESSAGES = 10;

    private final ConcurrentHashMap<String, List<ChatMessage>> memory = new ConcurrentHashMap<>();

    public List<ChatMessage> getMessages(String sessionId) {
        return memory.getOrDefault(sessionId, new ArrayList<>());
    }

    public void addUserMessage(String sessionId, String message) {
        addMessage(sessionId, UserMessage.from(message));
    }

    public void addAiMessage(String sessionId, String answer) {
        addMessage(sessionId, AiMessage.from(answer));
    }

    private void addMessage(String sessionId, ChatMessage message) {
        List<ChatMessage> messages = memory.computeIfAbsent(sessionId, id -> new ArrayList<>());
        messages.add(message);

        while (messages.size() > MAX_MESSAGES) {
            messages.removeFirst();
        }
    }

    public void clear(String sessionId) {
        memory.remove(sessionId);
    }
}
