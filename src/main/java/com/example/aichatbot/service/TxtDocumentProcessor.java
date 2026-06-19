package com.example.aichatbot.service;

import com.example.aichatbot.dto.FileType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@Component
public class TxtDocumentProcessor implements DocumentProcessor {
    @Override
    public String extractText(MultipartFile text) {
        try {
            return new String(text.getBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Could not read TXT file" + e);
        }
    }

    @Override
    public FileType fileType() {
        return FileType.TXT;
    }
}
