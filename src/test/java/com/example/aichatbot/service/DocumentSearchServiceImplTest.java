package com.example.aichatbot.service;

import com.example.aichatbot.dto.DocumentSearchResult;
import com.example.aichatbot.dto.SearchResult;
import com.example.aichatbot.dto.StoredChunk;
import com.example.aichatbot.repository.ElasticsearchChunkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocumentSearchServiceImplTest {
    private static final String CONTENT = "Hello";
    private static final int CHUNK_NUMBER = 1;
    public static final String DOCUMENT_ID = "1";
    private static final StoredChunk CHUNK_1 = new StoredChunk(DOCUMENT_ID, DOCUMENT_ID, "source name", CHUNK_NUMBER, CONTENT, new float[]{CHUNK_NUMBER, CHUNK_NUMBER});
    private static final List<StoredChunk> CHUNK_LIST = List.of(CHUNK_1);
    private static final double SCORE = 10.0;
    private static final List<SearchResult> SEARCH_RESULTS = List.of(new SearchResult("id", DOCUMENT_ID,
            "sourceName", CHUNK_NUMBER, CONTENT, SCORE));
    private DocumentSearchService documentSearchService;
    @Mock
    private EmbeddingServiceImpl embeddingService;
    @Mock
    private ElasticsearchChunkRepository elasticsearchChunkRepository;
    @Mock
    private ReciprocalRankFusion reciprocalRankFusion;

    @BeforeEach
    void setUp() {
        documentSearchService = new DocumentSearchServiceImpl(embeddingService, elasticsearchChunkRepository, reciprocalRankFusion);
    }

    @Test
    void shouldFindDocumentByID() throws IOException {
        when(embeddingService.embed(anyString())).thenReturn(new float[]{1f, 2f, 3f, 4f});
        when(elasticsearchChunkRepository.search(any(float[].class), anyInt())).thenReturn(CHUNK_LIST);

        String relevantContext = documentSearchService.findRelevantContext(CONTENT);

        assertNotNull(relevantContext);
        assertEquals(CONTENT, relevantContext);
    }

    @Test
    void shouldFindDocumentByQuestion() throws IOException {
        when(embeddingService.embed(anyString())).thenReturn(new float[]{1f, 2f, 3f, 4f});
        when(elasticsearchChunkRepository.searchByText(anyString(), anyInt())).thenReturn(SEARCH_RESULTS);
        when(elasticsearchChunkRepository.searchByVector(any(float[].class), anyInt())).thenReturn(SEARCH_RESULTS);
        when(reciprocalRankFusion.fuse(anyList(), anyList(), anyInt())).thenReturn(SEARCH_RESULTS);

        DocumentSearchResult relevantContext = documentSearchService.search(CONTENT);

        assertNotNull(relevantContext);
        assertEquals(CONTENT, relevantContext.context());
        assertEquals(SCORE, relevantContext.sources().getFirst().score());
        assertEquals(CHUNK_NUMBER, relevantContext.sources().getFirst().chunkNumber());
        assertEquals("S1", relevantContext.sources().getFirst().citation());
        assertEquals(DOCUMENT_ID, relevantContext.sources().getFirst().documentId());
        assertEquals(CONTENT, relevantContext.sources().getFirst().excerpt());
    }

    @Test
    public void shouldThrowExceptionWhenProcessorNotExist() throws IOException {
        when(embeddingService.embed(anyString())).thenReturn(new float[]{1f, 2f, 3f, 4f});
        when(elasticsearchChunkRepository.search(any(float[].class), anyInt())).thenThrow(new IOException());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> documentSearchService.findRelevantContext(CONTENT)
        );

        assertTrue(exception.getMessage().contains("Could not search document chunks"));
    }

    @Test
    public void shouldThrowExceptionFindDocumentByQuestion() throws IOException {
        when(embeddingService.embed(anyString())).thenReturn(new float[]{1f, 2f, 3f, 4f});
        when(elasticsearchChunkRepository.searchByText(anyString(), anyInt())).thenThrow(new IOException());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> documentSearchService.search(CONTENT)
        );

        assertTrue(exception.getMessage().contains("Could not perform hybrid search"));
    }
}
