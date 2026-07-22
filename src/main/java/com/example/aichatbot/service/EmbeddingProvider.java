package com.example.aichatbot.service;

public interface EmbeddingProvider {
    String name();

    float[] embed(String text);
}
