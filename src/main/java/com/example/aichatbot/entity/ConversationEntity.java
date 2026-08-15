package com.example.aichatbot.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversation",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_conversation_session_id",
                columnNames = {
                        "user_id",
                        "session_id"
                }
        )
)
public class ConversationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, updatable = false)
    private String sessionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @OneToMany(
            mappedBy = "conversation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ChatMessageEntity> messages = new ArrayList<>();

    protected ConversationEntity() {
    }

    public ConversationEntity(String sessionId) {
        this.sessionId = sessionId;
        this.createdAt = Instant.now();
    }

    public ConversationEntity(String sessionId, Instant createdAt) {
        this.sessionId = sessionId;
        this.createdAt = createdAt;
    }

    public ConversationEntity(
            String userId,
            String sessionId
    ) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getUserId() {
        return userId;
    }
}
