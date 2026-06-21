package com.example.aichatbot.service;

import com.example.aichatbot.dto.DocumentChunk;
import com.example.aichatbot.dto.DocumentUploadResponse;
import com.example.aichatbot.repository.ChunkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocumentIngestionServiceImplTest {
    private static final String TEXT = "Hello World";
    private static final String BIG_TEXT = "Oracle SQL 1Z0-071 Exam Cheat Sheet\n" +
            "1. SELECT & FROM Basics\n" +
            "• SELECT * returns rows in no guaranteed order unless ORDER BY is used.\n" +
            "• Aliases: SELECT last_name AS surname FROM employees; (AS optional)\n" +
            "• Double quotes preserve case & spaces in aliases.\n" +
            "• DISTINCT applies to all selected columns.\n" +
            "• ORDER BY can use column name, alias, or position number.\n" +
            "2. WHERE Clause & NULL\n" +
            "• NULL comparisons: col = NULL is always false; use IS NULL / IS NOT NULL.\n" +
            "• Any operation with NULL returns NULL (except NVL/COALESCE).\n" +
            "• NOT IN with NULL in list returns no rows.\n" +
            "• Date literal: DATE 'YYYY-MM-DD' safe for Oracle.\n" +
            "• BETWEEN is inclusive.\n" +
            "3. Joins\n" +
            "• INNER JOIN: match in both tables.\n" +
            "• LEFT OUTER JOIN: all left + matches.\n" +
            "• RIGHT OUTER JOIN: all right + matches.\n" +
            "• FULL OUTER JOIN: all from both.\n" +
            "• NATURAL JOIN: matches by same column name in both.\n" +
            "• JOIN ... USING(col): removes duplicate column in output.\n" +
            "4. Set Operators\n" +
            "• UNION removes duplicates; UNION ALL keeps them.\n" +
            "• INTERSECT returns common rows.\n" +
            "• MINUS returns rows from first query not in second.\n" +
            "• All SELECTs must have same number & type of columns.\n" +
            "• ORDER BY comes after the final SELECT.";
    private static final int FILE_SIZE = 11;
    private static final String FILE_NAME = "fileName";
    private static final String ORIGINAL_FILE_NAME = "originalFileName";
    private static final List<DocumentChunk> DOCUMENT_CHUNKS = List.of(new DocumentChunk("1", 1, TEXT));
    private static final List<DocumentChunk> DOCUMENT_CHUNKS_LARGE = List.of(new DocumentChunk("1", 1, BIG_TEXT));
    private static final int PREVIEW_LENGTH = 500;

    private static final MockMultipartFile MOCK_MULTIPART_FILE = new MockMultipartFile(FILE_NAME, ORIGINAL_FILE_NAME, MediaType.TEXT_PLAIN_VALUE, TEXT.getBytes());

    private DocumentIngestionService documentIngestionService;
    @Mock
    private DocumentServiceImpl documentService;
    @Mock
    private ChunkingServiceImpl chunkingService;
    @Mock
    private EmbeddingServiceImpl embeddingService;
    @Mock
    private ChunkRepository chunkRepository;

    @BeforeEach
    void setUp() {
        documentIngestionService = new DocumentIngestionServiceImpl(documentService, chunkingService, embeddingService, chunkRepository);
    }

    @Test
    void verifyIngestFunctionalityWithSmallFileSize() throws IOException {
        when(documentService.extractText(any(MultipartFile.class))).thenReturn(TEXT);
        when(chunkingService.chunk(anyString())).thenReturn(DOCUMENT_CHUNKS);
        when(embeddingService.embed(anyString())).thenReturn(new float[]{1.0f, 2.0f, 3.0f});

        DocumentUploadResponse service = documentIngestionService.ingest(MOCK_MULTIPART_FILE);

        assertEquals(ORIGINAL_FILE_NAME, service.fileName());
        assertEquals(FILE_SIZE, service.characters());
        assertEquals(FILE_SIZE, service.size());
        assertEquals(TEXT, service.preview());
    }


    @Test
    void verifyIngestFunctionalityWithBigFileSize() throws IOException {
        when(documentService.extractText(any(MultipartFile.class))).thenReturn(BIG_TEXT);
        when(chunkingService.chunk(anyString())).thenReturn(DOCUMENT_CHUNKS_LARGE);
        when(embeddingService.embed(anyString())).thenReturn(new float[]{1.0f, 2.0f, 3.0f});

        DocumentUploadResponse service = documentIngestionService.ingest(MOCK_MULTIPART_FILE);

        assertEquals(ORIGINAL_FILE_NAME, service.fileName());
        assertEquals(FILE_SIZE, service.size());
        assertEquals(PREVIEW_LENGTH, service.preview().length());
    }

}
