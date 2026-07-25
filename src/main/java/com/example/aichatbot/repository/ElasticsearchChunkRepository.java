package com.example.aichatbot.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.aichatbot.dto.ChunkDocument;
import com.example.aichatbot.dto.ChunkSearchResult;
import com.example.aichatbot.dto.StoredChunk;
import com.example.aichatbot.exception.DocumentSearchException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Service
public class ElasticsearchChunkRepository implements ChunkRepository {
    private static final String INDEX_NAME = "document-chunks";

    private final ElasticsearchClient client;

    public ElasticsearchChunkRepository(ElasticsearchClient client) {
        this.client = client;
    }
    @Override
    public void save(StoredChunk chunk) throws IOException {
        client.index(i -> i
                .index("documents")
                .id(chunk.id())
                .document(chunk));
    }
    @Override
    public List<StoredChunk> search(float[] embedding, int limit) throws IOException {
        SearchResponse<StoredChunk> response = client.search(s -> s
                        .index("documents")
                        .knn(knn -> knn
                                .field("embedding")
                                .queryVector(toFloatList(embedding))
                                .k(limit)
                                .numCandidates(50)
                        ),
                StoredChunk.class
        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<ChunkSearchResult> searchByText(
            String query,
            int limit
    ) {
        try {
            SearchResponse<ChunkDocument> response = client.search(
                    request -> request
                            .index(INDEX_NAME)
                            .size(limit)
                            .query(q -> q
                                    .match(m -> m
                                            .field("content")
                                            .query(query)
                                    )
                            ),
                    ChunkDocument.class
            );

            return response.hits()
                    .hits()
                    .stream()
                    .filter(hit -> hit.source() != null)
                    .map(hit -> new ChunkSearchResult(
                            hit.id(),
                            hit.source().content(),
                            hit.score() == null
                                    ? 0.0
                                    : hit.score()
                    ))
                    .toList();

        } catch (IOException e) {
            throw new DocumentSearchException(
                    "Failed to execute BM25 search",
                    e
            );
        }
    }

    @Override
    public List<ChunkSearchResult> searchByVector(
            float[] queryVector,
            int limit
    ) {
        try {
            SearchResponse<ChunkDocument> response = client.search(
                    request -> request
                            .index(INDEX_NAME)
                            .size(limit)
                            .knn(knn -> knn
                                    .field("embedding")
                                    .queryVector(toFloatList(queryVector))
                                    .k(limit)
                                    .numCandidates(Math.max(limit * 10, 50))
                            ),
                    ChunkDocument.class
            );

            return response.hits()
                    .hits()
                    .stream()
                    .filter(hit -> hit.source() != null)
                    .map(hit -> new ChunkSearchResult(
                            hit.id(),
                            hit.source().content(),
                            hit.score() == null
                                    ? 0.0
                                    : hit.score()
                    ))
                    .toList();

        } catch (IOException e) {
            throw new DocumentSearchException(
                    "Failed to execute vector search",
                    e
            );
        }
    }

    private List<Float> toFloatList(float[] vector) {
        return IntStream.range(0, vector.length)
                .mapToObj(i -> vector[i])
                .toList();
    }
}
