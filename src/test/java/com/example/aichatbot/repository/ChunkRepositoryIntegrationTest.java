package com.example.aichatbot.repository;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorSimilarity;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.example.aichatbot.dto.StoredChunk;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Testcontainers
public class ChunkRepositoryIntegrationTest {

    private static final String INDEX_NAME = "documents";
    private static final int DIMS = 3;

    @Container
    private static ElasticsearchContainer elasticsearch =
            new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.17.0")
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("discovery.type", "single-node");

    private ChunkRepository repository;
    private ElasticsearchClient client;

    @BeforeEach
    public void setUp() throws IOException {
        RestClient restClient = RestClient.builder(
                HttpHost.create(elasticsearch.getHttpHostAddress())
        ).build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );

        client = new ElasticsearchClient(transport);
        repository = new ChunkRepository(client);

        createIndex();
    }

    @AfterEach
    public void tearDown() throws IOException {
        client.indices().delete(d -> d.index(INDEX_NAME));
    }

    @Test
    public void shouldSaveAndSearchChunkByVector() throws IOException {
        StoredChunk chunk = new StoredChunk(
                "chunk-1",
                "doc-1",
                0,
                "Java Spring Boot Elasticsearch AI chatbot",
                new float[]{1.0f, 0.0f, 0.0f}
        );

        repository.save(chunk);

        client.indices().refresh(r -> r.index(INDEX_NAME));

        List<StoredChunk> result =
                repository.search(new float[]{1.0f, 0.0f, 0.0f}, 1);

        assertEquals(1, result.size());
        assertEquals("chunk-1", result.getFirst().id());
        assertEquals("doc-1", result.getFirst().documentId());
        assertEquals("Java Spring Boot Elasticsearch AI chatbot", result.getFirst().content());
    }

    private void createIndex() throws IOException {
        boolean exists = client.indices()
                .exists(e -> e.index(INDEX_NAME))
                .value();

        if (exists) {
            client.indices().delete(d -> d.index(INDEX_NAME));
        }

        client.indices().create(c -> c
                .index(INDEX_NAME)
                .mappings(m -> m
                        .properties("documentId", p -> p.keyword(k -> k))
                        .properties("chunkNumber", p -> p.integer(i -> i))
                        .properties("content", p -> p.text(t -> t))
                        .properties("embedding", p -> p.denseVector(v -> v
                                .dims(DIMS)
                                .index(true)
                                .similarity(DenseVectorSimilarity.Cosine)
                        ))
                )
        );
    }
}

