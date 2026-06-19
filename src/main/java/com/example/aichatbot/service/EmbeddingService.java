package com.example.aichatbot.service;

public sealed interface EmbeddingService permits EmbeddingServiceImpl {
    float[] embed(String text);
}
