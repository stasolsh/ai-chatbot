package com.example.aichatbot.service;

import com.example.aichatbot.dto.FileType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static com.example.aichatbot.dto.FileType.PDF;
import static com.example.aichatbot.dto.FileType.TXT;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FileTypeResolverTest {
    @InjectMocks
    private FileTypeResolver fileTypeResolver;

    @ParameterizedTest
    @MethodSource("provideDocuments")
    public void shouldFindFileTypes(String fileName, FileType fileType) throws Exception {
        FileType resalt = fileTypeResolver.resolveFileType(fileName);
        assertEquals(fileType, resalt);
    }

    @Test
    public void shouldThrowExceptionWhenFileNameNotExist() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fileTypeResolver.resolveFileType(null)
        );

        assertTrue(exception.getMessage().contains("File name is missing"));
    }

    @Test
    public void shouldThrowExceptionWhenFileNameNotSupported() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fileTypeResolver.resolveFileType("file.doc")
        );

        assertTrue(exception.getMessage().contains("Only TXT and PDF files are supported"));
    }

    private static Stream<Arguments> provideDocuments() {
        return Stream.of(
                Arguments.of("file.pdf", PDF),
                Arguments.of("file.txt", TXT)
        );
    }

}
