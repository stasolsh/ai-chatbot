package com.example.aichatbot.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatModel chatModel(
            @Value("${ollama.base-url}") String baseUrl,
            @Value("${ollama.model}") String model) {

        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(model)
                .build();
    }
}
