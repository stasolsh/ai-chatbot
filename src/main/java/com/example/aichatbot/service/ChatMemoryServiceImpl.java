package com.example.aichatbot.service;

import com.example.aichatbot.dto.MessageRole;
import com.example.aichatbot.entity.ChatMessageEntity;
import com.example.aichatbot.entity.ConversationEntity;
import com.example.aichatbot.repository.ChatMessageRepository;
import com.example.aichatbot.repository.ConversationRepository;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public final class ChatMemoryServiceImpl implements ChatMemoryService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatMemoryServiceImpl(ConversationRepository conversationRepository, ChatMessageRepository chatMessageRepository) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    @Transactional
    public List<ChatMessage> getMessages(String sessionId) {
        return chatMessageRepository.findTop10ByConversationSessionIdOrderByCreatedAtDesc(sessionId)
                .reversed()
                .stream()
                .map(this::toChatMessage)
                .toList();
    }

    @Override
    @Transactional
    public void addUserMessage(String sessionId, String message) {
        saveMessage(sessionId, MessageRole.USER, message);
    }

    @Override
    @Transactional
    public void addAiMessage(String sessionId, String answer) {
        saveMessage(sessionId, MessageRole.AI, answer);
    }

    @Override
    @Transactional
    public void clear(String sessionId) {

        conversationRepository.deleteBySessionId(sessionId);
    }

    private void saveMessage(String sessionId, MessageRole role, String content) {
        ConversationEntity conversationEntity = conversationRepository
                .findBySessionId(sessionId)
                .orElseGet(() -> conversationRepository.save(new ConversationEntity(sessionId)));
        chatMessageRepository.save(new ChatMessageEntity(conversationEntity, role, content));
    }

    private ChatMessage toChatMessage(ChatMessageEntity entity) {
        return switch (entity.getRole()) {
            case USER -> UserMessage.from(entity.getContent());
            case AI -> AiMessage.from(entity.getContent());
        };
    }
}
