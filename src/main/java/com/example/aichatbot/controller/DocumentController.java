package com.example.aichatbot.controller;

import com.example.aichatbot.dto.DocumentUploadResponse;
import com.example.aichatbot.service.DocumentIngestionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentIngestionService documentIngestionService;

    public DocumentController(DocumentIngestionService documentIngestionService) {
        this.documentIngestionService = documentIngestionService;
    }

    @PostMapping("/upload")
    public DocumentUploadResponse upload(@RequestParam("file") MultipartFile file) throws IOException {
        return documentIngestionService.ingest(file);
    }
}
