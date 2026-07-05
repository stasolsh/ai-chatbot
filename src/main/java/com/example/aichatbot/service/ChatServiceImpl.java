package com.example.aichatbot.service;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
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
    private final StreamingChatModel streamingChatModel;

    public ChatServiceImpl(ChatModel model, ChatMemoryService memoryService, DocumentSearchService documentSearchService, StreamingChatModel streamingChatModel) {
        this.model = model;
        this.memoryService = memoryService;
        this.documentSearchService = documentSearchService;
        this.streamingChatModel = streamingChatModel;
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

    public SseEmitter stream(String sessionId, String message) {
        SseEmitter emitter = new SseEmitter();

        StringBuilder answer = new StringBuilder();
        List<ChatMessage> messages = memoryService.getMessages(sessionId);
        streamingChatModel.chat(messages, new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String token) {
                answer.append(token);
                try {
                    emitter.send(token);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                memoryService.addUserMessage(sessionId, message);
                memoryService.addAiMessage(sessionId, answer.toString());
                emitter.complete();
            }

            @Override
            public void onError(Throwable error) {
                emitter.completeWithError(error);
            }
        });

        return emitter;
    }

    @Override
    public void clearMemory(String sessionId) {
        memoryService.clear(sessionId);
    }
}
