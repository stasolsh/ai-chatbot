package com.example.aichatbot.service;

import com.example.aichatbot.dto.StoredChunk;
import com.example.aichatbot.repository.ChunkRepository;
import com.example.aichatbot.repository.ElasticsearchChunkRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public final class DocumentSearchServiceImpl implements DocumentSearchService {
    private static final int TOP_K = 5;
    private final EmbeddingServiceImpl embeddingService;
    private final ChunkRepository elasticsearchChunkRepository;

    public DocumentSearchServiceImpl(EmbeddingServiceImpl embeddingService, ChunkRepository elasticsearchChunkRepository) {
        this.embeddingService = embeddingService;
        this.elasticsearchChunkRepository = elasticsearchChunkRepository;
    }

    @Override
    public String findRelevantContext(String question) {
        try {
            float[] questionEmbedding = embeddingService.embed(question);

            List<StoredChunk> chunks = elasticsearchChunkRepository.search(questionEmbedding, TOP_K);
            return chunks.stream()
                    .map(StoredChunk::content)
                    .collect(Collectors.joining("\n\n---\n\n"));
        } catch (IOException e) {
            throw new RuntimeException("Could not search document chunks", e);
        }
    }
}
