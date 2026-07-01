package com.example.aichatbot.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import java.util.function.Function;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ElasticsearchIndexInitializerTest {

    private static final float[] EMBEDDING = new float[768];

    private ElasticsearchIndexInitializer initializer;

    @Mock
    private ElasticsearchClient client;

    @Mock
    private ElasticsearchIndicesClient indicesClient;

    @Mock
    private EmbeddingServiceImpl embeddingService;

    @BeforeEach
    public void setUp() {
        initializer = new ElasticsearchIndexInitializer(client, embeddingService);
        when(client.indices()).thenReturn(indicesClient);
    }

    @Test
    public void shouldNotCreateIndexWhenItAlreadyExists() throws IOException {
        when(indicesClient.exists(any(Function.class)))
                .thenReturn(new BooleanResponse(true));

        initializer.initialize();

        verify(indicesClient).exists(any(Function.class));
        verify(indicesClient, never()).create(any(Function.class));
        verifyNoInteractions(embeddingService);
    }

    @Test
    public void shouldCreateIndexWhenItDoesNotExist() throws IOException {
        when(indicesClient.exists(any(Function.class)))
                .thenReturn(new BooleanResponse(false));

        when(embeddingService.embed("text"))
                .thenReturn(EMBEDDING);

        initializer.initialize();

        verify(indicesClient).exists(any(Function.class));
        verify(embeddingService).embed("text");
        verify(indicesClient).create(any(Function.class));
    }
}
