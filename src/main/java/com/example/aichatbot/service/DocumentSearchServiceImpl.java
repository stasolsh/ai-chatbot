package com.example.aichatbot.service;

import com.example.aichatbot.dto.DocumentSearchResult;
import com.example.aichatbot.dto.DocumentSource;
import com.example.aichatbot.dto.SearchResult;
import com.example.aichatbot.dto.StoredChunk;
import com.example.aichatbot.repository.ChunkRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public final class DocumentSearchServiceImpl implements DocumentSearchService {
    private static final int TOP_K = 5;
    private static final int RESULT_LIMIT = 5;
    private static final int CANDIDATE_LIMIT = 20;
    private final EmbeddingService embeddingService;
    private final ChunkRepository elasticsearchChunkRepository;
    private final ReciprocalRankFusion rankFusion;

    public DocumentSearchServiceImpl(EmbeddingService embeddingService, ChunkRepository elasticsearchChunkRepository, ReciprocalRankFusion rankFusion) {
        this.embeddingService = embeddingService;
        this.elasticsearchChunkRepository = elasticsearchChunkRepository;
        this.rankFusion = rankFusion;
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

    @Override
    public DocumentSearchResult search(String question) {
        try {
            float[] embedding = embeddingService.embed(question);

            List<SearchResult> textResults =
                    elasticsearchChunkRepository.searchByText(
                            question,
                            CANDIDATE_LIMIT
                    );

            List<SearchResult> vectorResults =
                    elasticsearchChunkRepository.searchByVector(
                            embedding,
                            CANDIDATE_LIMIT
                    );

            List<SearchResult> hybridResults =
                    rankFusion.fuse(
                            textResults,
                            vectorResults,
                            RESULT_LIMIT
                    );

            return buildSearchResult(hybridResults);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not perform hybrid search",
                    e
            );
        }
    }

    private DocumentSearchResult buildSearchResult(
            List<SearchResult> results
    ) {
        if (results.isEmpty()) {
            return DocumentSearchResult.empty();
        }

        String context = results.stream()
                .map(SearchResult::content)
                .collect(Collectors.joining("\n\n---\n\n"));

        List<DocumentSource> sources =
                IntStream.range(0, results.size())
                        .mapToObj(index -> {
                            SearchResult result = results.get(index);

                            return new DocumentSource(
                                    "S" + (index + 1),
                                    result.documentId(),
                                    result.sourceName(),
                                    result.chunkNumber(),
                                    result.content(),
                                    result.score()
                            );
                        })
                        .toList();

        return new DocumentSearchResult(context, sources);
    }
}
