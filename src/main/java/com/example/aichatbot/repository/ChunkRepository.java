package com.example.aichatbot.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.example.aichatbot.dto.StoredChunk;
import org.springframework.stereotype.Service;

import java.io.IOException;

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
}
