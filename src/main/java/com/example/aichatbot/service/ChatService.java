package com.example.aichatbot.service;

import com.example.aichatbot.dto.ChatResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public sealed interface ChatService permits ChatServiceImpl {
    String chat(String sessionId, String message);
    void clearMemory(String sessionId);
    SseEmitter stream(String sessionId, String message);
    ChatResult chatResult(String sessionId, String message);
}
