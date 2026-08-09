package com.example.aichatbot.exception;

import com.example.aichatbot.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class GlobalExceptionHandlerTest {
    private static final String ERROR_MESSAGE =
            "Only TXT and PDF files are supported";

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    public void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    public void shouldHandleUnsupportedFileTypeException() {
        UnsupportedFileTypeException exception =
                new UnsupportedFileTypeException(ERROR_MESSAGE);

        ErrorResponse response =
                exceptionHandler.handleUnsupportedFile(exception);

        assertEquals(ERROR_MESSAGE, response.error());
    }
}
