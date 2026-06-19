package com.example.aichatbot.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public final class ChatMemoryServiceImpl implements ChatMemoryService {

    private static final int MAX_MESSAGES = 10;

    private final ConcurrentHashMap<String, List<ChatMessage>> memory = new ConcurrentHashMap<>();

    @Override
    public List<ChatMessage> getMessages(String sessionId) {
        return memory.getOrDefault(sessionId, new ArrayList<>());
    }

    @Override
    public void addUserMessage(String sessionId, String message) {
        addMessage(sessionId, UserMessage.from(message));
    }

    @Override
    public void addAiMessage(String sessionId, String answer) {
        addMessage(sessionId, AiMessage.from(answer));
    }

    @Override
    public void clear(String sessionId) {
        memory.remove(sessionId);
    }

    private void addMessage(String sessionId, ChatMessage message) {
        List<ChatMessage> messages = memory.computeIfAbsent(sessionId, id -> new ArrayList<>());
        messages.add(message);

        while (messages.size() > MAX_MESSAGES) {
            messages.removeFirst();
        }
    }
}
