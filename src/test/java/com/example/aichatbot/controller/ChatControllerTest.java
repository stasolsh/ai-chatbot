package com.example.aichatbot.controller;

import com.example.aichatbot.dto.ChatRequest;
import com.example.aichatbot.dto.ChatResponse;
import com.example.aichatbot.service.ChatServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ChatController.class)
@AutoConfigureJsonTesters
public class ChatControllerTest {
    private static final String CHECK_RESULT = "Any possible answer.";
    private static final ChatRequest CHAT_REQUEST = new ChatRequest("stas", "My name");
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ChatServiceImpl chatServiceImpl;
    @Autowired
    private JacksonTester<ChatRequest> chatRequestJacksonTester;
    @Autowired
    private JacksonTester<ChatResponse> checkEventJacksonList;

    @Test
    public void shouldVerifyChatEndpoint() throws Exception {
        when(chatServiceImpl.chat(anyString(), anyString())).thenReturn(CHECK_RESULT);
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/chat")
                                .content(chatRequestJacksonTester.write(CHAT_REQUEST).getJson())
                                .contentType(APPLICATION_JSON)
                                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ChatResponse resultEvents = checkEventJacksonList.parseObject(mvcResult.getResponse().getContentAsString());

        assertEquals(CHECK_RESULT.toString(), resultEvents.answer());
    }

    @Test
    public void shouldVerifyClearMemory() throws Exception {
        String sessionId = "stas";
        mockMvc.perform(delete("/api/chat/" +  sessionId))
                .andExpect(status().isOk());
        verify(chatServiceImpl).clearMemory(sessionId);

    }
}
