package com.example.aichatbot.service;

import dev.langchain4j.data.message.ChatMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
public class ChatMemoryServiceImplTest {
    public static final String SESSION_ID = "SESSIONID";
    public static final String ANSWER = "ANSWER";
    @InjectMocks
    private ChatMemoryServiceImpl service;

    @Test
    public void verifyGetEmptyMessages() {
        List<ChatMessage> messages = service.getMessages(SESSION_ID);
        assertThat(messages).isEmpty();
    }

    @Test
    public void verifyGetMessagesWithSessionId() {
        service.addAiMessage(SESSION_ID, ANSWER);
        List<ChatMessage> messages = service.getMessages(SESSION_ID);
        assertThat(messages).hasSize(1);
    }

    @Test
    public void verifyClearMessages() {
        service.addAiMessage(SESSION_ID, ANSWER);
        service.clear(SESSION_ID);
        List<ChatMessage> messages = service.getMessages(SESSION_ID);
        assertThat(messages).isEmpty();
    }

    @Test
    public void verifyAddUserMessage() {
        service.addUserMessage(SESSION_ID, ANSWER);
        List<ChatMessage> messages = service.getMessages(SESSION_ID);
        assertThat(messages).hasSize(1);
    }

    @Test
    public void verifyAddMessageExtraSize() {
        List.of("message1", "message2", "message3",
                "message4", "message5", "message6",
                "message7", "message8", "message9",
                "message10", "message11"
                ).forEach(message -> service.addUserMessage(SESSION_ID, message));
        List<ChatMessage> messages = service.getMessages(SESSION_ID);
        assertThat(messages).hasSize(10);
    }
}
