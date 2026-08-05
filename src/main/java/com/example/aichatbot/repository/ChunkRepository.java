package com.example.aichatbot.repository;

import com.example.aichatbot.dto.SearchResult;
import com.example.aichatbot.dto.StoredChunk;

import java.io.IOException;
import java.util.List;

public interface ChunkRepository {
    void save(StoredChunk chunk) throws IOException;
    List<StoredChunk> search(float[] embedding, int limit) throws IOException;
    List<SearchResult> searchByVector(
            float[] queryVector,
            int limit
    ) throws IOException;

    List<SearchResult> searchByText(
            String query,
            int limit
    ) throws IOException;
}
