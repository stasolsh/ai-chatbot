package com.example.aichatbot.controller;

import com.example.aichatbot.dto.ChatRequest;
import com.example.aichatbot.dto.ChatResponse;
import com.example.aichatbot.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        String answer = chatService.chat(request.sessionId(), request.message());
        return new ChatResponse(answer);
    }

    @DeleteMapping("/{sessionId}")
    public void clearMemory(@PathVariable String sessionId) {
        chatService.clearMemory(sessionId);
    }
}
