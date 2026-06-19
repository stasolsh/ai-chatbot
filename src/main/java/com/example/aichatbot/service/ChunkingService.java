package com.example.aichatbot.service;

import com.example.aichatbot.dto.DocumentChunk;

import java.util.List;

public sealed interface ChunkingService permits ChunkingServiceImpl {
    List<DocumentChunk> chunk(String text);
}
