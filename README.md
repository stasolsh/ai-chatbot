# AI Chatbot
![Build](https://github.com/stasolsh/ai-chatbot/actions/workflows/custom-action.yml/badge.svg)
![Coverage](https://codecov.io/gh/stasolsh/ai-chatbot/branch/master/graph/badge.svg)
![Java](https://img.shields.io/badge/Java-21-red)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=springboot)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue)
![JUnit](https://img.shields.io/badge/JUnit-5-red?logo=junit5)
![License](https://img.shields.io/badge/license-MIT-green)

A simple AI chatbot built with Java 21, Spring Boot, LangChain4j, Ollama, and Elasticsearch.

The project demonstrates:

- Local LLM integration with Ollama
- Session-based conversational memory
- Retrieval-Augmented Generation (RAG)
- PDF and TXT document processing
- Automatic document chunking
- Vector embedding generation
- Elasticsearch vector storage
- Semantic document search
- REST API

## Technology Stack

* Java 21
* Spring Boot 3
* LangChain4j
* Ollama
* Elasticsearch
* Docker Compose
* Apache PDFBox
* Spring MVC Server-Sent Events (SSE)
* Spring Data JPA (Conversation Persistence)

## Architecture

```text
                REST API
                   │
       ┌───────────┴────────────┐
       ▼                        ▼
Document Upload            Chat Request
       │                        │
       ▼                        ▼
Document Service          Chat Memory
       │                        │
       ▼                        ▼
Document Processor      Document Search
       │                        │
       ▼                        ▼
Chunking Service     StreamingChatModel
       │                        │
       ▼                        ▼
Embedding Service      SSE Response
       │
       ▼
Embedding Provider
       │
       ▼
Ollama Embeddings
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

### Document Processing

Supported formats:

- TXT
- PDF

The upload pipeline performs the following steps:

```text
Upload
   ↓
Extract text
   ↓
Split into overlapping chunks
   ↓
Generate vector embeddings
   ↓
Store chunks in Elasticsearch
```

The project uses the Strategy pattern to support multiple document processors, making it easy to add new document formats such as DOCX or HTML.

Current supported processors:

- TXT
- PDF

### Retrieval-Augmented Generation (RAG)

During a chat request:

```text
User Question
      ↓
Generate Question Embedding
      ↓
Semantic Search in Elasticsearch
      ↓
Retrieve Relevant Chunks
      ↓
Combine with Chat Memory
      ↓
Ollama
      ↓
AI Response
```

## Streaming Responses

The chatbot supports real-time AI response streaming using Server-Sent Events (SSE).

Streaming pipeline:

```text
Client
   │
   ▼
ChatController
   │
   ▼
ChatService
   │
   ▼
StreamingChatModel
   │
   ▼
Token 1 → Token 2 → Token 3 → ...
```

Conversation memory and retrieved document context are combined to produce context-aware responses.

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

## Multiple Embedding Providers

The embedding layer is built around a provider abstraction.

```text
EmbeddingService
        │
        ▼
EmbeddingProvider
        │
        ▼
OllamaEmbeddingProvider
```
## Conversation Persistence

Conversation history is managed through a dedicated ChatMemoryService.

```text
ChatService
      │
      ▼
ChatMemoryService
      │
      ▼
Persistence Layer
```

## Project Structure

```text
src/main/java
├── config
├── controller
│   ├── ChatController
│   └── DocumentController
├── dto
├── repository
│   └── ChunkRepository
├── service
│   ├── ChatService
│   ├── ChatMemoryService
│   ├── DocumentIngestionService
│   ├── DocumentSearchService
│   ├── DocumentService
│   ├── ChunkingService
│   ├── EmbeddingService
│   ├── ElasticsearchIndexInitializer
│   ├── DocumentProcessor
│   └── DocumentProcessorRegistry
└── processor
    ├── PdfDocumentProcessor
    └── TxtDocumentProcessor
```
## End-to-End Workflow

```text
Upload PDF
      │
      ▼
Extract Text
      │
      ▼
Chunk Document
      │
      ▼
Generate Embeddings
      │
      ▼
Store in Elasticsearch
      │
      ▼
────────────────────────────────────
User asks a question
      │
      ▼
Generate Question Embedding
        │
        ▼
Semantic Search
        │
        ▼
Retrieve Relevant Chunks
        │
        ▼
Combine with Chat Memory
        │
        ▼
StreamingChatModel
        │
        ▼
Stream AI Tokens (SSE)
        │
        ▼
Persist Conversation
```