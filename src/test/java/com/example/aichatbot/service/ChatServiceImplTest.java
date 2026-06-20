package com.example.aichatbot.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    private static final String SESSION_ID = "SESSIONID";
    private static final String MESSAGE = "MESSAGE";
    private static final String CONTEXT = "Context";
    private static final String AI_ANSWER = "Ai message";

    private ChatServiceImpl chatServiceImpl;

    @Mock
    private ChatModel model;

    @Mock
    private ChatMemoryServiceImpl memoryService;

    @Mock
    private DocumentSearchServiceImpl documentSearchService;

    @BeforeEach
    void setUp() {
        chatServiceImpl = new ChatServiceImpl(model, memoryService, documentSearchService);
    }

    @Test
    void shouldChatWithDocumentContextAndSaveMessagesToMemory() {
        List<ChatMessage> messages = new ArrayList<>();

        ChatResponse chatResponse = ChatResponse.builder()
                .aiMessage(AiMessage.from(AI_ANSWER))
                .build();

        when(documentSearchService.findRelevantContext(MESSAGE)).thenReturn(CONTEXT);
        when(memoryService.getMessages(SESSION_ID)).thenReturn(messages);
        when(model.chat(anyList())).thenReturn(chatResponse);

        String result = chatServiceImpl.chat(SESSION_ID, MESSAGE);

        assertEquals(AI_ANSWER, result);

        ArgumentCaptor<List<ChatMessage>> messagesCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(model).chat(messagesCaptor.capture());

        List<ChatMessage> sentMessages = messagesCaptor.getValue();

        assertEquals(2, sentMessages.size());
        assertInstanceOf(SystemMessage.class, sentMessages.get(0));
        assertInstanceOf(UserMessage.class, sentMessages.get(1));



        verify(memoryService).addUserMessage(SESSION_ID, MESSAGE);
        verify(memoryService).addAiMessage(SESSION_ID, AI_ANSWER);
    }

    @Test
    void shouldChatWithoutDocumentContextWhenContextIsBlank() {
        List<ChatMessage> messages = new ArrayList<>();

        ChatResponse chatResponse = ChatResponse.builder()
                .aiMessage(AiMessage.from(AI_ANSWER))
                .build();

        when(documentSearchService.findRelevantContext(MESSAGE)).thenReturn("");
        when(memoryService.getMessages(SESSION_ID)).thenReturn(messages);
        when(model.chat(anyList())).thenReturn(chatResponse);

        String result = chatServiceImpl.chat(SESSION_ID, MESSAGE);

        assertEquals(AI_ANSWER, result);

        ArgumentCaptor<List<ChatMessage>> messagesCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(model).chat(messagesCaptor.capture());

        List<ChatMessage> sentMessages = messagesCaptor.getValue();

        assertEquals(1, sentMessages.size());
        assertInstanceOf(UserMessage.class, sentMessages.getFirst());

        verify(memoryService).addUserMessage(SESSION_ID, MESSAGE);
        verify(memoryService).addAiMessage(SESSION_ID, AI_ANSWER);
    }

    @Test
    void shouldClearMemory() {
        chatServiceImpl.clearMemory(SESSION_ID);

        verify(memoryService).clear(SESSION_ID);
    }
}
