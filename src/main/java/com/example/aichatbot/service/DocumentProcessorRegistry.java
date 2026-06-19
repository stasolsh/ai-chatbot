package com.example.aichatbot.service;

import com.example.aichatbot.dto.FileType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class DocumentProcessorRegistry {
    private final Map<FileType, DocumentProcessor> documentProcessors = new EnumMap<>(FileType.class);

    public DocumentProcessorRegistry(List<DocumentProcessor> processors) {
        processors.forEach(processor -> this.documentProcessors.put(processor.fileType(), processor));
    }

    public DocumentProcessor getDocumentProcessor(FileType fileType) {
        DocumentProcessor processor = documentProcessors.get(fileType);
        if (processor == null) {
            throw new IllegalStateException("Unsupported file type: " + fileType);
        }
        return processor;
    }
}
