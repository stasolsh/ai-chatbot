package com.example.aichatbot.service;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private final ChatModel model;
    private final ChatMemoryService memoryService;
    private final DocumentSearchService documentSearchService;

    public ChatService(ChatModel model, ChatMemoryService memoryService, DocumentSearchService documentSearchService) {
        this.model = model;
        this.memoryService = memoryService;
        this.documentSearchService = documentSearchService;
    }

    public String chat(String sessionId, String message) {
        String context = documentSearchService.findRelevantContext(message);
        String ragPrompt = buildRagPrompt(message, context);
        memoryService.addUserMessage(sessionId, ragPrompt);
        List<ChatMessage> messages = new ArrayList<>(memoryService.getMessages(sessionId));
        ChatResponse response = model.chat(messages);
        String answer = response.aiMessage().text();
        memoryService.addAiMessage(sessionId, answer);
        return answer;
    }

    private String buildRagPrompt(String question, String context) {
        return """
                Use the following document context to answer the question.
                If the answer is not present in the context, say that you do not know from the uploaded documents.

                Context:
                %s

                Question:
                %s
                """.formatted(context, question);
    }

    public void clearMemory(String sessionId) {
        memoryService.clear(sessionId);
    }
}
