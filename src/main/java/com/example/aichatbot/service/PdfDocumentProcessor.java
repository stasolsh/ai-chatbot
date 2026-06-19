package com.example.aichatbot.service;

import com.example.aichatbot.dto.FileType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class PdfDocumentProcessor implements DocumentProcessor {

    @Override
    public String extractText(MultipartFile text) {
        try {
            PDDocument document = Loader.loadPDF(text.getBytes());
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            throw new RuntimeException("Could not read PDF file", e);
        }
    }

    @Override
    public FileType fileType() {
        return FileType.PDF;
    }
}
