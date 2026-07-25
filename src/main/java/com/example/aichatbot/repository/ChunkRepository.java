package com.example.aichatbot.repository;

import com.example.aichatbot.dto.ChunkSearchResult;
import com.example.aichatbot.dto.StoredChunk;

import java.io.IOException;
import java.util.List;

public interface ChunkRepository {

    void save(StoredChunk chunk) throws IOException;
    List<StoredChunk> search(float[] embedding, int limit) throws IOException;
    List<ChunkSearchResult> searchByVector(
            float[] queryVector,
            int limit
    );

    List<ChunkSearchResult> searchByText(
            String query,
            int limit
    );
}
