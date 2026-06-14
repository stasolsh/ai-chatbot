package com.example.aichatbot.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@Service
public class DocumentService {

    public String extractText(MultipartFile file) {
        String filename = file.getOriginalFilename();

        if (filename == null) {
            throw new IllegalArgumentException("File name is missing");
        }

        try {
            if (filename.toLowerCase().endsWith(".txt")) {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            }

            if (filename.toLowerCase().endsWith(".pdf")) {
                try (PDDocument document = Loader.loadPDF(file.getBytes())) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    return stripper.getText(document);
                }
            }
            throw new IllegalArgumentException("Only TXT and PDF files are supported");

        } catch (Exception e) {
            throw new RuntimeException("Could not extract text from file: " + filename, e);
        }
    }
}
