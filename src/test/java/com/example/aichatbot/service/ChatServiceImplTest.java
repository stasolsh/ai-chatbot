package com.example.aichatbot.service;

import com.example.aichatbot.dto.ChatResult;
import com.example.aichatbot.dto.DocumentSearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    private static final String SESSION_ID = "SESSIONID";
    private static final String USER_ID = "USER_ID";
    private static final String MESSAGE = "MESSAGE";
    private static final String CONTEXT = "Context";
    private static final String AI_ANSWER = "Ai message";
    public static final DocumentSearchResult DOCUMENT_SEARCH_RESULT = new DocumentSearchResult("Context", List.of());
    private static final DocumentSearchResult EMPTY_SEARCH_RESULT =
            new DocumentSearchResult("", List.of());
    private ChatService chatServiceImpl;

    @Mock
    private ChatModel model;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ChatMemoryServiceImpl memoryService;

    @Mock
    private DocumentSearchServiceImpl documentSearchService;

    @Mock
    private StreamingChatModel streamingChatModel;

    @BeforeEach
    void setUp() {
        chatServiceImpl = new ChatServiceImpl(objectMapper, model, memoryService, documentSearchService, streamingChatModel);
    }

    @Test
    void shouldChatWithDocumentContextAndSaveMessagesToMemory() {
        List<ChatMessage> messages = new ArrayList<>();

        ChatResponse chatResponse = ChatResponse.builder()
                .aiMessage(AiMessage.from(AI_ANSWER))
                .build();

        when(documentSearchService.findRelevantContext(MESSAGE)).thenReturn(CONTEXT);
        when(memoryService.getMessages(USER_ID, SESSION_ID)).thenReturn(messages);
        when(model.chat(anyList())).thenReturn(chatResponse);

        String result = chatServiceImpl.chat(USER_ID, SESSION_ID, MESSAGE);

        assertEquals(AI_ANSWER, result);

        ArgumentCaptor<List<ChatMessage>> messagesCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(model).chat(messagesCaptor.capture());

        List<ChatMessage> sentMessages = messagesCaptor.getValue();

        assertEquals(2, sentMessages.size());
        assertInstanceOf(SystemMessage.class, sentMessages.get(0));
        assertInstanceOf(UserMessage.class, sentMessages.get(1));

        verify(memoryService).addUserMessage(USER_ID, SESSION_ID, MESSAGE);
        verify(memoryService).addAiMessage(USER_ID, SESSION_ID, AI_ANSWER);
    }

    @Test
    void shouldChatWithoutDocumentContextWhenContextIsBlank() {
        List<ChatMessage> messages = new ArrayList<>();

        ChatResponse chatResponse = ChatResponse.builder()
                .aiMessage(AiMessage.from(AI_ANSWER))
                .build();

        when(documentSearchService.findRelevantContext(MESSAGE)).thenReturn("");
        when(memoryService.getMessages(USER_ID, SESSION_ID)).thenReturn(messages);
        when(model.chat(anyList())).thenReturn(chatResponse);

        String result = chatServiceImpl.chat(USER_ID, SESSION_ID, MESSAGE);

        assertEquals(AI_ANSWER, result);

        ArgumentCaptor<List<ChatMessage>> messagesCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(model).chat(messagesCaptor.capture());

        List<ChatMessage> sentMessages = messagesCaptor.getValue();

        assertEquals(1, sentMessages.size());
        assertInstanceOf(UserMessage.class, sentMessages.getFirst());

        verify(memoryService).addUserMessage(USER_ID, SESSION_ID, MESSAGE);
        verify(memoryService).addAiMessage(USER_ID, SESSION_ID, AI_ANSWER);
    }

    @Test
    void shouldStreamResponseAndSaveCompleteAnswerToMemory() {
        List<ChatMessage> messages = new ArrayList<>();

        when(memoryService.getMessages(USER_ID, SESSION_ID)).thenReturn(messages);
        when(documentSearchService.search(anyString())).thenReturn(DOCUMENT_SEARCH_RESULT);
        SseEmitter emitter = chatServiceImpl.stream(USER_ID, SESSION_ID, MESSAGE);

        assertNotNull(emitter);

        ArgumentCaptor<List<ChatMessage>> messagesCaptor =
                ArgumentCaptor.forClass(List.class);

        ArgumentCaptor<StreamingChatResponseHandler> handlerCaptor =
                ArgumentCaptor.forClass(StreamingChatResponseHandler.class);

        verify(streamingChatModel).chat(
                messagesCaptor.capture(),
                handlerCaptor.capture()
        );


        StreamingChatResponseHandler handler = handlerCaptor.getValue();

        handler.onPartialResponse("Hello");
        handler.onPartialResponse(" ");
        handler.onPartialResponse("World");

        ChatResponse response = ChatResponse.builder()
                .aiMessage(AiMessage.from("Hello World"))
                .build();

        handler.onCompleteResponse(response);

        verify(memoryService).addUserMessage(USER_ID, SESSION_ID, MESSAGE);
        verify(memoryService).addAiMessage(USER_ID, SESSION_ID, "Hello World");
    }

    @Test
    void shouldCompleteEmitterWithErrorWhenStreamingFails() {
        List<ChatMessage> messages = new ArrayList<>();
        RuntimeException streamingError =
                new RuntimeException("Streaming failed");

        when(memoryService.getMessages(USER_ID, SESSION_ID)).thenReturn(messages);
        when(documentSearchService.search(MESSAGE)).thenReturn(DOCUMENT_SEARCH_RESULT);

        SseEmitter emitter = chatServiceImpl.stream(USER_ID, SESSION_ID, MESSAGE);

        assertNotNull(emitter);

        ArgumentCaptor<StreamingChatResponseHandler> handlerCaptor =
                ArgumentCaptor.forClass(StreamingChatResponseHandler.class);

        verify(streamingChatModel).chat(
                anyList(),
                handlerCaptor.capture()
        );

        handlerCaptor.getValue().onError(streamingError);

        verify(memoryService, never())
                .addUserMessage(anyString(), anyString(), anyString());

        verify(memoryService, never())
                .addAiMessage(anyString(), anyString(), anyString());
    }

    @Test
    void shouldSaveEmptyAnswerWhenStreamingCompletesWithoutTokens() {
        List<ChatMessage> messages = new ArrayList<>();

        when(memoryService.getMessages(USER_ID, SESSION_ID)).thenReturn(messages);
        when(documentSearchService.search(MESSAGE))
                .thenReturn(EMPTY_SEARCH_RESULT);
        chatServiceImpl.stream(USER_ID, SESSION_ID, MESSAGE);

        ArgumentCaptor<StreamingChatResponseHandler> handlerCaptor =
                ArgumentCaptor.forClass(StreamingChatResponseHandler.class);

        verify(streamingChatModel).chat(
                anyList(),
                handlerCaptor.capture()
        );

        handlerCaptor.getValue().onCompleteResponse(
                ChatResponse.builder()
                        .aiMessage(AiMessage.from(""))
                        .build()
        );
        verify(memoryService).addUserMessage(USER_ID, SESSION_ID, MESSAGE);
        verify(memoryService).addAiMessage(USER_ID, SESSION_ID, "");
    }


    @Test
    void shouldReturnChatResult() {
        List<ChatMessage> messages = new ArrayList<>();
        when(documentSearchService.search(MESSAGE)).thenReturn(DOCUMENT_SEARCH_RESULT);
        when(memoryService.getMessages(USER_ID, SESSION_ID)).thenReturn(messages);
        when(model.chat(anyList())).thenReturn(ChatResponse.builder()
                .aiMessage(AiMessage.from(AI_ANSWER))
                .build());

        ChatResult chatResult = chatServiceImpl.chatResult(USER_ID, SESSION_ID, MESSAGE);
        verify(memoryService).addUserMessage(USER_ID, SESSION_ID, MESSAGE);

        assertEquals(AI_ANSWER, chatResult.answer());
    }

    @Test
    void shouldClearMemory() {
        chatServiceImpl.clearMemory(USER_ID, SESSION_ID);
        verify(memoryService).clear(USER_ID, SESSION_ID);
    }
}
