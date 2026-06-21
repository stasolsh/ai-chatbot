package com.example.aichatbot.service;

import com.example.aichatbot.dto.FileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static com.example.aichatbot.dto.FileType.PDF;
import static com.example.aichatbot.dto.FileType.TXT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocumentProcessorRegistryTest {
    private DocumentProcessorRegistry registry;
    @Mock
    private PdfDocumentProcessor pdfDocumentProcessor;
    @Mock
    private TxtDocumentProcessor txtDocumentProcessor;

    @BeforeEach
    void setUp() {
        when(pdfDocumentProcessor.fileType()).thenReturn(PDF);
        when(txtDocumentProcessor.fileType()).thenReturn(TXT);
        registry = new DocumentProcessorRegistry(List.of(pdfDocumentProcessor, txtDocumentProcessor));
    }

    @ParameterizedTest
    @MethodSource("provideDocumentTypes")
    public void registerDocumentProcessor(FileType fileType) {
        DocumentProcessor processor = registry.getDocumentProcessor(fileType);
        assertNotNull(processor);
        assertEquals(fileType, processor.fileType());
    }

    @Test
    public void shouldThrowExceptionWhenProcessorNotExist() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> registry.getDocumentProcessor(null)
        );

        assertTrue(exception.getMessage().contains("Unsupported file type:"));
    }

    private static Stream<Arguments> provideDocumentTypes() {
        return Stream.of(
                Arguments.of(PDF),
                Arguments.of(TXT)
        );
    }
}
