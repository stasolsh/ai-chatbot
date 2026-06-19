package com.example.aichatbot.service;

import com.example.aichatbot.dto.FileType;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentProcessor {
    String extractText(MultipartFile text);
    FileType fileType();
}
