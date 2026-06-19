package com.example.aichatbot.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.aichatbot.dto.StoredChunk;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Service
public class ChunkRepository {

    private final ElasticsearchClient client;

    public ChunkRepository(ElasticsearchClient client) {
        this.client = client;
    }

    public void save(StoredChunk chunk) throws IOException {
        client.index(i -> i
                .index("documents")
                .id(chunk.id())
                .document(chunk));
    }

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

    private List<Float> toFloatList(float[] vector) {
        return IntStream.range(0, vector.length)
                .mapToObj(i -> vector[i])
                .toList();
    }
}
