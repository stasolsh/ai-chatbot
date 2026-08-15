package com.example.aichatbot.controller;

import com.example.aichatbot.service.ChatServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ChatControllerStreamTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatServiceImpl chatService;

    @Test
    public void shouldStreamChatResponse() throws Exception {
        SseEmitter emitter = new SseEmitter();

        when(chatService.stream("userId1", "user1", "Hello"))
                .thenReturn(emitter);

        MvcResult result = mockMvc.perform(get("/api/chat/stream")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("userId1")
                                        .claim("roles", List.of("USER"))
                                )
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_USER")
                                )
                        )
                        .param("sessionId", "user1")
                        .param("message", "Hello")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();

        emitter.send("Hi");
        emitter.complete();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hi")));
    }
}
