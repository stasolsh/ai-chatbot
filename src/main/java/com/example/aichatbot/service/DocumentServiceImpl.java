package com.example.aichatbot.service;

import com.example.aichatbot.dto.FileType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public final class DocumentServiceImpl implements DocumentService {
    private final DocumentProcessorRegistry documentProcessorRegistry;
    private final FileTypeResolver fileTypeResolver;

    public DocumentServiceImpl(DocumentProcessorRegistry documentProcessorRegistry, FileTypeResolver fileTypeResolver) {
        this.documentProcessorRegistry = documentProcessorRegistry;
        this.fileTypeResolver = fileTypeResolver;
    }

    @Override
    public String extractText(MultipartFile file) {
        FileType fileType = fileTypeResolver.resolveFileType(file.getOriginalFilename());
        DocumentProcessor documentProcessor = documentProcessorRegistry.getDocumentProcessor(fileType);
        return documentProcessor.extractText(file);
    }
}
