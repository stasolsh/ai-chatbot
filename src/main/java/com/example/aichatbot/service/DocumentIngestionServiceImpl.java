package com.example.aichatbot.service;

import com.example.aichatbot.dto.DocumentChunk;
import com.example.aichatbot.dto.DocumentUploadResponse;
import com.example.aichatbot.dto.StoredChunk;
import com.example.aichatbot.repository.ChunkRepository;
import com.example.aichatbot.repository.ElasticsearchChunkRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public final class DocumentIngestionServiceImpl implements DocumentIngestionService {

    private static final int PREVIEW_LENGTH = 500;

    private final DocumentService documentService;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final ChunkRepository elasticsearchChunkRepository;

    public DocumentIngestionServiceImpl(
            DocumentService documentService,
            ChunkingService chunkingService,
            EmbeddingService embeddingService,
            ChunkRepository elasticsearchChunkRepository) {

        this.documentService = documentService;
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
        this.elasticsearchChunkRepository = elasticsearchChunkRepository;
    }

    @Override
    public DocumentUploadResponse ingest(MultipartFile file) throws IOException {
        String text = documentService.extractText(file);
        String sourceName = file.getOriginalFilename();

        for (DocumentChunk documentChunk : chunkingService.chunk(text)) {
            elasticsearchChunkRepository.save(toStoredChunk(documentChunk, sourceName));
        }

        return new DocumentUploadResponse(
                sourceName,
                file.getSize(),
                text.length(),
                preview(text)
        );
    }

    private StoredChunk toStoredChunk(DocumentChunk chunk, String sourceName) {
        return new StoredChunk(
                UUID.randomUUID().toString(),
                chunk.documentId(),
                sourceName,
                chunk.chunkNumber(),
                chunk.content(),
                embeddingService.embed(chunk.content())
        );
    }

    private String preview(String text) {
        return text.length() > PREVIEW_LENGTH
                ? text.substring(0, PREVIEW_LENGTH)
                : text;
    }
}
