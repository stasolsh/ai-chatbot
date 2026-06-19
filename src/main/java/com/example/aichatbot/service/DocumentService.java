package com.example.aichatbot.service;

import org.springframework.web.multipart.MultipartFile;

public sealed interface DocumentService permits DocumentServiceImpl {
    String extractText(MultipartFile file);
}
