package com.example.aichatbot.controller;

import com.example.aichatbot.dto.ChatRequest;
import com.example.aichatbot.dto.ChatResponse;
import com.example.aichatbot.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService service;

    public ChatController(ChatService service) {
        this.service = service;
    }

    @PostMapping
    public ChatResponse chat(
            @Valid @RequestBody ChatRequest request) {

        String answer = service.chat(request.message());

        return new ChatResponse(answer);
    }
}
