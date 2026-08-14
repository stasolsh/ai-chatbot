package com.example.aichatbot.entity;

import com.example.aichatbot.dto.MessageRole;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "chat_messages",
        indexes = {
                @Index(
                        name = "idx_chat_message_conversation_created",
                        columnList = "conversation_id, created_at"
                )
        }
)
public class ChatMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "conversation_id",
            nullable = false
    )
    private ConversationEntity conversation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ChatMessageEntity() {

    }

    public ChatMessageEntity(ConversationEntity conversation, MessageRole role, String content) {
        this.conversation = conversation;
        this.role = role;
        this.content = content;
        this.createdAt = Instant.now();
    }

    public MessageRole getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}
