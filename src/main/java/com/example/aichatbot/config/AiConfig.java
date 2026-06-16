package com.example.aichatbot.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${elasticsearch.hostname}")
    private String elasticUrl;

    @Value("${elasticsearch.port}")
    private int elasticPort;

    @Bean
    public ChatModel chatModel(
            @Value("${ollama.base-url}") String baseUrl,
            @Value("${ollama.model}") String model) {

        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(model)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel(
            @Value("${ollama.base-url}") String baseUrl) {

        return OllamaEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .modelName("nomic-embed-text")
                .build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {

        RestClient restClient =
                RestClient.builder(
                                new HttpHost(elasticUrl, elasticPort))
                        .build();

        ElasticsearchTransport transport =
                new RestClientTransport(
                        restClient,
                        new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }
}
