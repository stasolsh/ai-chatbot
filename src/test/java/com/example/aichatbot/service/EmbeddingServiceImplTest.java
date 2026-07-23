package com.example.aichatbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmbeddingServiceImplTest {
    private static final String TEXT = "Hello World";
    private static final float[] VECTOR = {1.0f, 2.0f, 3.0f};
    private static final String OLLAMA = "ollama";

    @Mock
    private EmbeddingProvider provider;
    @Mock
    private EmbeddingProviderRegistry embeddingProviderRegistry;

    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        when(embeddingProviderRegistry.get(OLLAMA)).thenReturn(provider);
        embeddingService = new EmbeddingServiceImpl(embeddingProviderRegistry, OLLAMA);
    }

    @Test
    void shouldReturnEmbeddingVector() {
        when(provider.embed(TEXT)).thenReturn(VECTOR);

        float[] result = embeddingService.embed(TEXT);

        assertArrayEquals(VECTOR, result);
        verify(provider).embed(TEXT);
        verifyNoMoreInteractions(provider);
    }

}
