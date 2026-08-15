package com.example.aichatbot.controller;

import com.example.aichatbot.dto.ChatApiResponse;
import com.example.aichatbot.dto.ChatRequest;
import com.example.aichatbot.dto.ChatResult;
import com.example.aichatbot.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatApiResponse chat(
            Authentication authentication,
            @Valid @RequestBody ChatRequest request
    ) {
        ChatResult result = chatService.chatResult(
                authentication.getName(),
                request.sessionId(),
                request.message()
        );

        return new ChatApiResponse(
                result.answer(),
                result.sources()
        );
    }

    @DeleteMapping("/{sessionId}")
    public void clearMemory(Authentication authentication, @PathVariable String sessionId) {
        chatService.clearMemory(authentication.getName(), sessionId);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            Authentication authentication,
            @RequestParam String sessionId,
            @RequestParam String message) {

        return chatService.stream(authentication.getName(), sessionId, message);
    }
}
