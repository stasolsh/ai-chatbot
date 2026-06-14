# AI Chatbot

A simple AI chatbot built with Java 21, Spring Boot, LangChain4j, Ollama, and Elasticsearch.

The project demonstrates:

* Local LLM integration with Ollama
* Conversational memory
* PDF and TXT document upload
* REST API
* Foundation for Retrieval-Augmented Generation (RAG)

## Technology Stack

* Java 21
* Spring Boot 3
* LangChain4j
* Ollama
* Elasticsearch
* Docker Compose
* Apache PDFBox

## Architecture

```text
User
  │
  ▼
Spring Boot REST API
  │
  ▼
LangChain4j
  │
  ▼
Ollama (Llama 3.1)
  │
  ▼
Elasticsearch
```

## Features

### Chat

The chatbot communicates with a local Ollama model through LangChain4j.

Current model:

```text
llama3.1
```

### Chat Memory

Conversation history is stored per session.

The application keeps the latest messages for each user session and sends them together with new prompts, enabling contextual conversations.

Example:

```text
User: My name is Stas.
AI: Nice to meet you, Stas.

User: What is my name?
AI: Your name is Stas.
```

### Document Upload

Supported formats:

* TXT
* PDF

Uploaded documents are parsed and converted to plain text.

PDF extraction is implemented using Apache PDFBox.

Current functionality:

* Upload document
* Extract text
* Return document statistics
* Return text preview

Future versions will store document chunks in Elasticsearch and use them for RAG.

## Running the Application

### Start Infrastructure

```bash
docker compose up -d
```

### Pull Models

```bash
docker exec -it ollama ollama pull llama3.1
docker exec -it ollama ollama pull nomic-embed-text
```

### Run Application

```bash
mvn clean spring-boot:run
```

## Docker Services

### Ollama

```text
http://localhost:11434
```

### Elasticsearch

```text
http://localhost:9200
```

Health check:

```bash
curl http://localhost:9200
```

## API

### Chat

#### Request

```http
POST /api/chat
```

```json
{
  "sessionId": "user1",
  "message": "Explain Kafka in one sentence"
}
```

#### Response

```json
{
  "answer": "Apache Kafka is a distributed event streaming platform."
}
```

### Clear Chat Memory

#### Request

```http
DELETE /api/chat/{sessionId}
```

Example:

```http
DELETE /api/chat/user1
```

### Upload Document

#### Request

```http
POST /api/documents/upload
```

Multipart form:

```text
file=<document.pdf>
```

Example:

```bash
curl -X POST http://localhost:8080/api/documents/upload \
-F "file=@sample.pdf"
```

#### Response

```json
{
  "fileName": "sample.pdf",
  "size": 12045,
  "characters": 6540,
  "preview": "Document preview..."
}
```

## Project Structure

```text
src/main/java
├── config
│   └── AiConfig
├── controller
│   ├── ChatController
│   └── DocumentController
├── dto
│   ├── ChatRequest
│   ├── ChatResponse
│   └── DocumentUploadResponse
├── service
│   ├── ChatService
│   ├── ChatMemoryService
│   └── DocumentService
└── AiChatbotApplication
```
