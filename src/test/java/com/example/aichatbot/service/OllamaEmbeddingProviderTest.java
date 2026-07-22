package com.example.aichatbot.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OllamaEmbeddingProviderTest {

    private static final String TEXT = "Hello World";
    private static final float[] VECTOR = {1.0f, 2.0f, 3.0f};

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private Response<Embedding> response;

    @Mock
    private Embedding embedding;

    private EmbeddingProvider provider;

    @BeforeEach
    void setUp() {
        provider = new OllamaEmbeddingProvider(embeddingModel);
    }

    @Test
    void shouldReturnEmbeddingVector() {
        when(embeddingModel.embed(TEXT)).thenReturn(response);
        when(response.content()).thenReturn(embedding);
        when(embedding.vector()).thenReturn(VECTOR);

        float[] result = provider.embed(TEXT);

        assertArrayEquals(VECTOR, result);

        verify(embeddingModel).embed(TEXT);
        verify(response).content();
        verify(embedding).vector();
    }
}
