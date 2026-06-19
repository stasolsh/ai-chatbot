package com.example.aichatbot.controller;

import com.example.aichatbot.dto.DocumentUploadResponse;
import com.example.aichatbot.service.DocumentIngestionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(DocumentController.class)
@AutoConfigureJsonTesters
public class DocumentControllerTest {
    private static final DocumentUploadResponse DOCUMENT_UPLOAD_RESPONSE = new DocumentUploadResponse(
            "oracle_sql_1z0_071_cheatsheet.pdf",
            4961,
            3105,
            "Oracle SQL 1Z0-071 Exam Cheat Sheet"
    );
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private DocumentIngestionServiceImpl documentIngestionService;

    @Test
    public void shouldVerifyDocumentUploadEndpoint() throws Exception {
        when(documentIngestionService.ingest(any(MultipartFile.class))).thenReturn(DOCUMENT_UPLOAD_RESPONSE);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello from test file".getBytes()
        );
        mockMvc.perform(multipart("/api/documents/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value(DOCUMENT_UPLOAD_RESPONSE.fileName()))
                .andExpect(jsonPath("$.size").value(DOCUMENT_UPLOAD_RESPONSE.size()))
                .andExpect(jsonPath("$.characters").value(DOCUMENT_UPLOAD_RESPONSE.characters()))
                .andExpect(jsonPath("$.preview").value(DOCUMENT_UPLOAD_RESPONSE.preview()));
    }
}
