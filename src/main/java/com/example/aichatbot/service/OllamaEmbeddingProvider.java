package com.example.aichatbot.service;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
public class OllamaEmbeddingProvider implements EmbeddingProvider {
    private final EmbeddingModel embeddingModel;

    public OllamaEmbeddingProvider(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public String name() {
        return "ollama";
    }

    @Override
    public float[] embed(String text) {
        return embeddingModel
                .embed(text)
                .content()
                .vector();
    }
}
