package com.example.aichatbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmbeddingProviderRegistryTest {
    private static final String OLLAMA = "ollama";
    private final List<EmbeddingProvider> providers = new ArrayList<>();
    private EmbeddingProviderRegistry registry;
    @Mock
    private OllamaEmbeddingProvider provider;

    @BeforeEach
    void setUp() {
        when(provider.name()).thenReturn(OLLAMA);
        providers.add(provider);
        registry = new EmbeddingProviderRegistry(providers);
    }

    @Test
    public void testGetEmbeddingProvider() {
        EmbeddingProvider result = registry.get(OLLAMA);
        assertNotNull(result);
        assertEquals(OLLAMA, result.name());
    }
    @Test
    public void shouldThrowExceptionWhenProcessorNotExist(){
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> registry.get(null)
        );
        assertTrue(exception.getMessage().contains("Unknown embedding provider:"));

    }
}
