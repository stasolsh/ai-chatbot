package com.example.aichatbot.service;

import com.example.aichatbot.dto.StoredChunk;
import com.example.aichatbot.repository.ChunkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocumentSearchServiceImplTest {
    private static final String CONTENT = "Hello";
    private static final StoredChunk CHUNK_1 = new StoredChunk("1", "1", 1, CONTENT, new float[]{1, 1});
    private static final List<StoredChunk> CHUNK_LIST = List.of(CHUNK_1);
    private DocumentSearchService documentSearchService;
    @Mock
    private EmbeddingServiceImpl embeddingService;
    @Mock
    private ChunkRepository chunkRepository;

    @BeforeEach
    void setUp() {
        documentSearchService = new DocumentSearchServiceImpl(embeddingService, chunkRepository);
    }

    @Test
    void shouldFindDocumentByID() throws IOException {
        when(embeddingService.embed(anyString())).thenReturn(new float[]{1f, 2f, 3f, 4f});
        when(chunkRepository.search(any(float[].class), anyInt())).thenReturn(CHUNK_LIST);

        String relevantContext = documentSearchService.findRelevantContext(CONTENT);

        assertNotNull(relevantContext);
        assertEquals(CONTENT, relevantContext);
    }

    @Test
    public void shouldThrowExceptionWhenProcessorNotExist() throws IOException {
        when(embeddingService.embed(anyString())).thenReturn(new float[]{1f, 2f, 3f, 4f});
        when(chunkRepository.search(any(float[].class), anyInt())).thenThrow(new IOException());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> documentSearchService.findRelevantContext(CONTENT)
        );

        assertTrue(exception.getMessage().contains("Could not search document chunks"));
    }
}
