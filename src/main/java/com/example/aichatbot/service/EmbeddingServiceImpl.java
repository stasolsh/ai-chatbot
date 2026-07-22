package com.example.aichatbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public final class EmbeddingServiceImpl implements EmbeddingService {

    private final EmbeddingProvider provider;

    public EmbeddingServiceImpl(EmbeddingProviderRegistry registry,
                                @Value("${embedding.provider}") String providerName) {
        this.provider = registry.get(providerName);
    }


    @Override
    public float[] embed(String text) {
        return provider.embed(text);
    }
}
