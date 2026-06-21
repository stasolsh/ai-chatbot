package com.example.aichatbot.service;

import com.example.aichatbot.dto.DocumentChunk;
import com.example.aichatbot.dto.DocumentUploadResponse;
import com.example.aichatbot.dto.StoredChunk;
import com.example.aichatbot.repository.ChunkRepository;
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
    private final ChunkRepository chunkRepository;

    public DocumentIngestionServiceImpl(
            DocumentService documentService,
            ChunkingService chunkingService,
            EmbeddingService embeddingService,
            ChunkRepository chunkRepository) {

        this.documentService = documentService;
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
        this.chunkRepository = chunkRepository;
    }

    @Override
    public DocumentUploadResponse ingest(MultipartFile file) throws IOException {
        String text = documentService.extractText(file);

        for (DocumentChunk documentChunk : chunkingService.chunk(text)) {
            StoredChunk storedChunk = toStoredChunk(documentChunk);
            chunkRepository.save(storedChunk);
        }

        return new DocumentUploadResponse(
                file.getOriginalFilename(),
                file.getSize(),
                text.length(),
                preview(text)
        );
    }

    private StoredChunk toStoredChunk(DocumentChunk chunk) {
        return new StoredChunk(
                UUID.randomUUID().toString(),
                chunk.documentId(),
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
