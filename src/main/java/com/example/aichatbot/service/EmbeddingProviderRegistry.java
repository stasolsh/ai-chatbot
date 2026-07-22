package com.example.aichatbot.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class EmbeddingProviderRegistry {
    private final Map<String, EmbeddingProvider> providers;

    public EmbeddingProviderRegistry(List<EmbeddingProvider> providers) {
        this.providers = providers.stream()
                .collect(Collectors.toMap(EmbeddingProvider::name, Function.identity()));
    }

    public EmbeddingProvider get(String name) {
        return Optional.ofNullable(providers.get(name))
                .orElseThrow(() -> new IllegalArgumentException("Unknown embedding provider: " + name));
    }
}
