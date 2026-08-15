package com.example.aichatbot.service;

import com.example.aichatbot.dto.ChatResult;
import com.example.aichatbot.dto.DocumentSearchResult;
import com.example.aichatbot.dto.StreamErrorEvent;
import com.example.aichatbot.dto.StreamTokenEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public final class ChatServiceImpl implements ChatService {
    private static final String DOCUMENT_CONTEXT = """
            Answer using the conversation and document context.
            
            When information comes from a document, cite the corresponding source
            using its exact marker, for example [S1] or [S2].
            
            Do not invent citation markers.
            Do not cite a source that does not support the statement.
            When the documents do not contain the answer, clearly say so.
            
            Document context:
            %s
            """;
    private final ObjectMapper objectMapper;
    private final ChatModel model;
    private final ChatMemoryService memoryService;
    private final DocumentSearchService documentSearchService;
    private final StreamingChatModel streamingChatModel;

    public ChatServiceImpl(ObjectMapper objectMapper, ChatModel model, ChatMemoryService memoryService, DocumentSearchService documentSearchService, StreamingChatModel streamingChatModel) {
        this.objectMapper = objectMapper;
        this.model = model;
        this.memoryService = memoryService;
        this.documentSearchService = documentSearchService;
        this.streamingChatModel = streamingChatModel;
    }

    @Override
    public String chat(String userId, String sessionId, String message) {
        String context = documentSearchService.findRelevantContext(message);
        List<ChatMessage> messages = memoryService.getMessages(userId, sessionId);
        if (!context.isBlank()) {
            messages.add(SystemMessage.from(DOCUMENT_CONTEXT.formatted(context)));
        }
        messages.add(UserMessage.from(message));
        ChatResponse response = model.chat(messages);
        String answer = response.aiMessage().text();
        memoryService.addUserMessage(userId, sessionId, message);
        memoryService.addAiMessage(userId, sessionId, answer);
        return answer;
    }

    @Override
    public SseEmitter stream(String userId, String sessionId, String message) {
        SseEmitter emitter = new SseEmitter(120_000L);

        DocumentSearchResult searchResult =
                documentSearchService.search(message);

        List<ChatMessage> messages =
                new ArrayList<>(memoryService.getMessages(userId, sessionId));

        if (!searchResult.context().isBlank()) {
            messages.add(SystemMessage.from(
                    DOCUMENT_CONTEXT.formatted(searchResult.context())
            ));
        }

        messages.add(UserMessage.from(message));

        StringBuilder answer = new StringBuilder();

        streamingChatModel.chat(
                messages,
                new StreamingChatResponseHandler() {

                    @Override
                    public void onPartialResponse(String token) {
                        answer.append(token);
                        sendEvent(
                                emitter,
                                "token",
                                new StreamTokenEvent(token)
                        );
                    }

                    @Override
                    public void onCompleteResponse(
                            ChatResponse response
                    ) {
                        memoryService.addUserMessage(userId, sessionId, message);
                        memoryService.addAiMessage(
                                userId,
                                sessionId,
                                answer.toString()
                        );

                        sendEvent(
                                emitter,
                                "sources",
                                searchResult.sources()
                        );

                        sendEvent(emitter, "done", Map.of());
                        emitter.complete();
                    }

                    @Override
                    public void onError(Throwable error) {
                        sendEvent(
                                emitter,
                                "error",
                                new StreamErrorEvent(error.getMessage())
                        );

                        emitter.completeWithError(error);
                    }
                }
        );

        return emitter;
    }

    @Override
    public ChatResult chatResult(String userId, String sessionId, String message) {
        DocumentSearchResult searchResult =
                documentSearchService.search(message);

        List<ChatMessage> messages =
                new ArrayList<>(memoryService.getMessages(userId, sessionId));

        if (!searchResult.context().isBlank()) {
            messages.add(SystemMessage.from(
                    DOCUMENT_CONTEXT.formatted(searchResult.context())
            ));
        }

        messages.add(UserMessage.from(message));

        ChatResponse response =
                model.chat(messages);

        String answer = response.aiMessage().text();

        memoryService.addUserMessage(userId, sessionId, message);
        memoryService.addAiMessage(userId, sessionId, answer);

        return new ChatResult(answer, searchResult.sources());
    }

    @Override
    public void clearMemory(String userId, String sessionId) {
        memoryService.clear(userId, sessionId);
    }

    private void sendEvent(
            SseEmitter emitter,
            String eventName,
            Object data
    ) {
        try {
            emitter.send(
                    SseEmitter.event()
                            .name(eventName)
                            .data(objectMapper.writeValueAsString(data))
            );
        } catch (IOException exception) {
            emitter.completeWithError(exception);
        }
    }
}
