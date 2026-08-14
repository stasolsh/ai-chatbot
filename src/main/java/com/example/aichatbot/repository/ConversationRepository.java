package com.example.aichatbot.repository;

import com.example.aichatbot.entity.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<ConversationEntity, Long> {
    Optional<ConversationEntity> findBySessionId(String sessionId);
    void deleteBySessionId(String sessionId);
}
