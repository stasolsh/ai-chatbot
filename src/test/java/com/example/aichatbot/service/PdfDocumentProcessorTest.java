package com.example.aichatbot.service;

import com.example.aichatbot.dto.FileType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@ExtendWith(MockitoExtension.class)
public class PdfDocumentProcessorTest {
    private static final String FILE_NAME = "fileName";
    private static final String ORIGINAL_FILE_NAME = "originalFileName";
    private static final String TEXT = "Hello World";
    private static final MockMultipartFile MOCK_MULTIPART_FILE = new MockMultipartFile(FILE_NAME, ORIGINAL_FILE_NAME, APPLICATION_PDF_VALUE, TEXT.getBytes());

    @InjectMocks
    private PdfDocumentProcessor pdfDocumentProcessor;

    @Test
    public void shouldFindFileTypes() {
        FileType type = pdfDocumentProcessor.fileType();
        assertEquals(FileType.PDF, type);
    }

    @Test
    public void shouldThrowExceptionWhenExtractText() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> pdfDocumentProcessor.extractText(null)
        );

        assertTrue(exception.getMessage().contains("Could not read PDF file"));
    }

    @Test
    public void shouldExtractTextFromPdfFile() throws IOException {
        try (MockedStatic<Loader> mockedLoader = mockStatic(Loader.class)) {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream =
                         new PDPageContentStream(document, page)) {

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText(TEXT);
                contentStream.endText();
            }
            mockedLoader.when(() -> Loader.loadPDF(any(byte[].class)))
                    .thenReturn(document);
            String result = pdfDocumentProcessor.extractText(MOCK_MULTIPART_FILE);
            assertEquals(TEXT, result.trim());
        }
    }
}
