package com.example.aichatbot.service;

import com.example.aichatbot.dto.DocumentUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public sealed interface DocumentIngestionService permits DocumentIngestionServiceImpl {
    DocumentUploadResponse ingest(MultipartFile file) throws IOException;
}
