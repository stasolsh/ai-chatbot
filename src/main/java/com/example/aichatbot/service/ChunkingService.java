package com.example.aichatbot.service;

import com.example.aichatbot.dto.DocumentChunk;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class ChunkingService {

    private static final int CHUNK_SIZE = 1000;
    private static final int OVERLAP = 200;

    public List<DocumentChunk> chunk(String text) {

        String documentId = UUID.randomUUID().toString();
        int step = CHUNK_SIZE - OVERLAP;

        return IntStream.iterate(0, start -> start < text.length(), start -> start + step)
                .mapToObj(start -> new DocumentChunk(
                        documentId,
                        start / step,
                        text.substring(
                                start,
                                Math.min(start + CHUNK_SIZE, text.length())
                        )
                ))
                .toList();
    }
}
