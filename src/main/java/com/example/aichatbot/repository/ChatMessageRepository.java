package com.example.aichatbot.repository;

import com.example.aichatbot.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Integer> {
    List<ChatMessageEntity>
    findTop10ByConversationUserIdAndConversationSessionIdOrderByCreatedAtDesc(
            String userId,
            String sessionId
    );
}
