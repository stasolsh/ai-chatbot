package com.example.aichatbot.controller;

import com.example.aichatbot.dto.DocumentUploadResponse;
import com.example.aichatbot.service.DocumentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    public static final int TEST_LENGTH = 500;
    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping("/upload")
    public DocumentUploadResponse upload(@RequestParam("file") MultipartFile file) {
        String text = service.extractText(file);

        String preview = text.length() > TEST_LENGTH
                ? text.substring(0, TEST_LENGTH)
                : text;

        return new DocumentUploadResponse(
                file.getOriginalFilename(),
                file.getSize(),
                text.length(),
                preview
        );
    }
}
