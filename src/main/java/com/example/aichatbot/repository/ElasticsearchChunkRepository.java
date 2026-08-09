package com.example.aichatbot.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.aichatbot.dto.SearchResult;
import com.example.aichatbot.dto.StoredChunk;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Service
public class ElasticsearchChunkRepository implements ChunkRepository {
    private static final String DOCUMENTS = "documents";
    private final ElasticsearchClient client;
    public ElasticsearchChunkRepository(ElasticsearchClient client) {
        this.client = client;
    }

    @Override
    public void save(StoredChunk chunk) throws IOException {
        client.index(i -> i
                .index(DOCUMENTS)
                .id(chunk.id())
                .document(chunk));
    }

    @Override
    public List<StoredChunk> search(float[] embedding, int limit) throws IOException {
        return getStoredChunkSearchResponse(embedding, limit).hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<SearchResult> searchByText(String query, int limit) throws IOException {
        SearchResponse<StoredChunk> response = client.search(
                search -> search
                        .index(DOCUMENTS)
                        .size(limit)
                        .query(q -> q.match(m -> m
                                .field("content")
                                .query(query)
                        )),
                StoredChunk.class
        );

        return response.hits()
                .hits()
                .stream()
                .map(this::toSearchResult)
                .toList();
    }

    @Override
    public List<SearchResult> searchByVector(
            float[] embedding,
            int limit
    ) throws IOException {
        return getStoredChunkSearchResponse(embedding, limit).hits()
                .hits()
                .stream()
                .map(this::toSearchResult)
                .toList();
    }

    private SearchResponse<StoredChunk> getStoredChunkSearchResponse(float[] embedding, int limit) throws IOException {
        return client.search(
                search -> search
                        .index(DOCUMENTS)
                        .knn(knn -> knn
                                .field("embedding")
                                .queryVector(toFloatList(embedding))
                                .k(limit)
                                .numCandidates(50)
                        ),
                StoredChunk.class
        );
    }

    private SearchResult toSearchResult(Hit<StoredChunk> hit) {
        StoredChunk chunk = hit.source();

        if (chunk == null) {
            throw new IllegalStateException(
                    "Elasticsearch hit has no source: " + hit.id()
            );
        }

        return new SearchResult(
                hit.id(),
                chunk.documentId(),
                chunk.sourceName(),
                chunk.chunkNumber(),
                chunk.content(),
                hit.score() == null ? 0.0 : hit.score()
        );
    }

    private List<Float> toFloatList(float[] vector) {
        return IntStream.range(0, vector.length)
                .mapToObj(index -> vector[index])
                .toList();
    }
}
