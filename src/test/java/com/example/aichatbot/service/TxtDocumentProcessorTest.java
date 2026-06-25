package com.example.aichatbot.service;

import com.example.aichatbot.dto.FileType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@ExtendWith(MockitoExtension.class)
public class TxtDocumentProcessorTest {
    private static final String FILE_NAME = "fileName";
    private static final String ORIGINAL_FILE_NAME = "originalFileName";
    private static final String TEXT = "Hello World";
    private static final MockMultipartFile MOCK_MULTIPART_FILE = new MockMultipartFile(FILE_NAME, ORIGINAL_FILE_NAME, APPLICATION_PDF_VALUE, TEXT.getBytes());
    @InjectMocks
    private TxtDocumentProcessor txtDocumentProcessor;

    @Test
    public void shouldFindFileTypes() {
        FileType type = txtDocumentProcessor.fileType();
        assertEquals(FileType.TXT, type);
    }

    @Test
    public void shouldThrowExceptionWhenExtractText() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> txtDocumentProcessor.extractText(null)
        );

        assertTrue(exception.getMessage().contains("Could not read TXT file"));
    }

    @Test
    public void shouldExtractTextFromTxtFile() {
        String result = txtDocumentProcessor.extractText(MOCK_MULTIPART_FILE);
        assertEquals(TEXT, result);
    }
}
