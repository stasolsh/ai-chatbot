package com.example.aichatbot.service;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public final class ChatServiceImpl implements ChatService {
    private static final String DOCUMENT_CONTEXT = """
            Use document context when it is relevant to the user question.
            If the question is about previous conversation, user name, preferences, or chat history,
            use conversation memory instead.
            
            Document context:
            %s
            """;
    private final ChatModel model;
    private final ChatMemoryService memoryService;
    private final DocumentSearchService documentSearchService;

    public ChatServiceImpl(ChatModel model, ChatMemoryService memoryService, DocumentSearchService documentSearchService) {
        this.model = model;
        this.memoryService = memoryService;
        this.documentSearchService = documentSearchService;
    }

    @Override
    public String chat(String sessionId, String message) {
        String context = documentSearchService.findRelevantContext(message);
        List<ChatMessage> messages = memoryService.getMessages(sessionId);
        if (!context.isBlank()) {
            messages.add(SystemMessage.from(DOCUMENT_CONTEXT.formatted(context)));
        }
        messages.add(UserMessage.from(message));
        ChatResponse response = model.chat(messages);
        String answer = response.aiMessage().text();
        memoryService.addUserMessage(sessionId, message);
        memoryService.addAiMessage(sessionId, answer);
        return answer;
    }

    @Override
    public void clearMemory(String sessionId) {
        memoryService.clear(sessionId);
    }
}
