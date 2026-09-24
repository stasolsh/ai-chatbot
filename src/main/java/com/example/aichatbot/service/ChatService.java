package com.example.aichatbot.service;

import com.example.aichatbot.dto.ChatResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ChatService {

    String chat(
            String userId,
            String sessionId,
            String message
    );

    void clearMemory(
            String userId,
            String sessionId
    );

    SseEmitter stream(
            String userId,
            String sessionId,
            String message
    );

    ChatResult chatResult(
            String userId,
            String sessionId,
            String message
    );
}
