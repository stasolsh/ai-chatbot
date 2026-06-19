package com.example.aichatbot.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorSimilarity;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class ElasticsearchIndexInitializer {
    private static final String INDEX_NAME = "document";
    private final ElasticsearchClient client;
    private final EmbeddingServiceImpl embeddingService;

    public ElasticsearchIndexInitializer(ElasticsearchClient client, EmbeddingServiceImpl embeddingService) {
        this.client = client;
        this.embeddingService = embeddingService;
    }

    @PostConstruct
    public void initialize() throws IOException {
        boolean exists = client.indices()
                .exists(e -> e.index(INDEX_NAME))
                .value();
        if (exists) {
            return;
        }
        int dimensions = embeddingService.embed("text").length;
        createDocumentsIndex(dimensions);
    }

    private void createDocumentsIndex(int dimensions) throws IOException {
        client.indices().create(c -> c
                .index(INDEX_NAME)
                .mappings(m -> m
                        .properties("documentId", p -> p.keyword(k -> k))
                        .properties("chunkNumber", p -> p.integer(i -> i))
                        .properties("content", p -> p.text(t -> t))
                        .properties("embedding", p -> p.denseVector(v -> v
                                .dims(dimensions)
                                .index(true)
                                .similarity(DenseVectorSimilarity.Cosine)
                        ))
                )
        );
    }
}
