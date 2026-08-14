package com.example.aichatbot.service;

import com.example.aichatbot.dto.MessageRole;
import com.example.aichatbot.entity.ChatMessageEntity;
import com.example.aichatbot.entity.ConversationEntity;
import com.example.aichatbot.repository.ChatMessageRepository;
import com.example.aichatbot.repository.ConversationRepository;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatMemoryServiceImplTest {

    private static final String SESSION_ID = "SESSIONID";
    private static final String USER_MESSAGE = "Hello";
    private static final String AI_ANSWER = "Hello! How can I help?";

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    private ChatMemoryServiceImpl service;

    @BeforeEach
    public void setUp() {
        service = new ChatMemoryServiceImpl(
                conversationRepository,
                chatMessageRepository
        );
    }

    @Test
    public void shouldReturnEmptyMessagesWhenConversationDoesNotExist() {
        when(chatMessageRepository
                .findTop10ByConversationSessionIdOrderByCreatedAtDesc(SESSION_ID))
                .thenReturn(List.of());

        List<ChatMessage> result = service.getMessages(SESSION_ID);

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnMessagesInChronologicalOrder() {
        ConversationEntity conversation =
                new ConversationEntity(SESSION_ID);

        ChatMessageEntity userMessage =
                new ChatMessageEntity(
                        conversation,
                        MessageRole.USER,
                        USER_MESSAGE
                );

        ChatMessageEntity aiMessage =
                new ChatMessageEntity(
                        conversation,
                        MessageRole.AI,
                        AI_ANSWER
                );

        // Repository returns newest first
        when(chatMessageRepository
                .findTop10ByConversationSessionIdOrderByCreatedAtDesc(SESSION_ID))
                .thenReturn(List.of(aiMessage, userMessage));

        List<ChatMessage> result = service.getMessages(SESSION_ID);

        assertThat(result).hasSize(2);

        assertThat(result.get(0))
                .isInstanceOf(UserMessage.class);

        assertThat(result.get(1))
                .isInstanceOf(AiMessage.class);
    }

    @Test
    public void shouldAddUserMessageToExistingConversation() {
        ConversationEntity conversation =
                new ConversationEntity(SESSION_ID);

        when(conversationRepository.findBySessionId(SESSION_ID))
                .thenReturn(Optional.of(conversation));

        service.addUserMessage(SESSION_ID, USER_MESSAGE);

        verify(chatMessageRepository)
                .save(argThat(message ->
                        message.getRole() == MessageRole.USER
                                && message.getContent().equals(USER_MESSAGE)
                ));

        verify(conversationRepository, never())
                .save(any());
    }

    @Test
    public void shouldAddAiMessageToExistingConversation() {
        ConversationEntity conversation =
                new ConversationEntity(SESSION_ID);

        when(conversationRepository.findBySessionId(SESSION_ID))
                .thenReturn(Optional.of(conversation));

        service.addAiMessage(SESSION_ID, AI_ANSWER);

        verify(chatMessageRepository)
                .save(argThat(message ->
                        message.getRole() == MessageRole.AI
                                && message.getContent().equals(AI_ANSWER)
                ));
    }

    @Test
    public void shouldCreateConversationWhenAddingFirstMessage() {
        ConversationEntity conversation =
                new ConversationEntity(SESSION_ID);

        when(conversationRepository.findBySessionId(SESSION_ID))
                .thenReturn(Optional.empty());

        when(conversationRepository.save(any(ConversationEntity.class)))
                .thenReturn(conversation);

        service.addUserMessage(SESSION_ID, USER_MESSAGE);

        verify(conversationRepository)
                .save(argThat(entity ->
                        SESSION_ID.equals(entity.getSessionId())
                ));

        verify(chatMessageRepository)
                .save(argThat(message ->
                        message.getRole() == MessageRole.USER
                                && USER_MESSAGE.equals(message.getContent())
                ));
    }

    @Test
    public void shouldClearConversation() {
        service.clear(SESSION_ID);

        verify(conversationRepository)
                .deleteBySessionId(SESSION_ID);
    }
}
