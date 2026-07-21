package com.example.aichatbot.controller;
import com.example.aichatbot.service.ChatServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc
public class ChatControllerStreamTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatServiceImpl chatService;

    @Test
    public void shouldStreamChatResponse() throws Exception {
        SseEmitter emitter = new SseEmitter();

        when(chatService.stream("user1", "Hello"))
                .thenReturn(emitter);

        MvcResult result = mockMvc.perform(get("/api/chat/stream")
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
