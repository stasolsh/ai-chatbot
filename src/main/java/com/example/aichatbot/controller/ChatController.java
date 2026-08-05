package com.example.aichatbot.controller;

import com.example.aichatbot.dto.ChatApiResponse;
import com.example.aichatbot.dto.ChatRequest;
import com.example.aichatbot.dto.ChatResult;
import com.example.aichatbot.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
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
            @Valid @RequestBody ChatRequest request
    ) {
        ChatResult result = chatService.chatResult(
                request.sessionId(),
                request.message()
        );

        return new ChatApiResponse(
                result.answer(),
                result.sources()
        );
    }

    @DeleteMapping("/{sessionId}")
    public void clearMemory(@PathVariable String sessionId) {
        chatService.clearMemory(sessionId);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @RequestParam String sessionId,
            @RequestParam String message) {

        return chatService.stream(sessionId, message);
    }
}
