package com.example.aichatbot.service;

import com.example.aichatbot.dto.FileType;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class FileTypeResolver {
    public FileType resolveFileType(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name is missing");
        }
        String lowerCaseName = fileName.toLowerCase(Locale.ROOT);
        if (lowerCaseName.endsWith(".pdf")) {
            return FileType.PDF;
        }
        if (lowerCaseName.endsWith(".txt")) {
            return FileType.TXT;
        }
        throw new IllegalArgumentException("Only TXT and PDF files are supported");
    }
}
