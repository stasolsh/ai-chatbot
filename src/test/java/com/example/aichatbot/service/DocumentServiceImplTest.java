package com.example.aichatbot.service;

import com.example.aichatbot.dto.FileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static com.example.aichatbot.dto.FileType.PDF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceImplTest {
    private static final String TEXT = "Hello World";
    private static final String ORIGINAL_FILE_NAME = "originalFileName";
    private static final MockMultipartFile MOCK_MULTIPART_FILE = new MockMultipartFile("fileName", ORIGINAL_FILE_NAME, MediaType.TEXT_PLAIN_VALUE, TEXT.getBytes());

    private DocumentService documentService;
    @Mock
    private DocumentProcessorRegistry documentProcessorRegistry;
    @Mock
    private FileTypeResolver fileTypeResolver;
    @Mock
    private DocumentProcessor documentProcessor;

    @BeforeEach
    void setUp() {
        documentService = new DocumentServiceImpl(documentProcessorRegistry, fileTypeResolver);
    }

    @Test
    void shouldFindDocumentByID() throws IOException, InterruptedException {
        when(fileTypeResolver.resolveFileType(anyString())).thenReturn(PDF);
        when(documentProcessorRegistry.getDocumentProcessor(any(FileType.class))).thenReturn(documentProcessor);
        when(documentProcessor.extractText(any(MockMultipartFile.class))).thenReturn(TEXT);

        String result = documentService.extractText(MOCK_MULTIPART_FILE);

        assertNotNull(result);
        assertEquals(TEXT, result);
    }
}
