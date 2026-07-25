package com.example.aichatbot.exception;

import java.io.IOException;

public class DocumentSearchException extends RuntimeException {
    public DocumentSearchException(String failedToExecuteBm25Search, IOException e) {
    }
}
