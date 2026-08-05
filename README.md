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
- Hybrid Search (BM25 + Vector Search)
- Source citations
- Server-Sent Events (SSE) streaming
- Multiple embedding providers
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
      ┌────────────┴─────────────┐
      ▼                          ▼
Document Upload             Chat Request
      │                          │
      ▼                          ▼
Document Service            Chat Memory
      │                          │
      ▼                          ▼
Document Processor      Document Search Service
      │                          │
      ▼                          ▼
Chunking Service       ┌──────────┴──────────┐
      │                ▼                     ▼
      ▼            BM25 Search        Vector Search
Embedding Service          │                │
      │                    └──────┬─────────┘
      ▼                           ▼
Embedding Provider      Reciprocal Rank Fusion
      │                           │
      ▼                           ▼
Ollama Embeddings      Retrieved Sources
      │                           │
      ▼                           ▼
 Elasticsearch        StreamingChatModel
                                   │
                                   ▼
                             SSE Response
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
      │
      ▼
Generate Question Embedding
      │
      ▼
BM25 Search
      │
      ├────────────┐
      ▼            ▼
Vector Search      │
      │            │
      └──────┬─────┘
             ▼
 Reciprocal Rank Fusion
             ▼
Retrieve Relevant Chunks
             ▼
Generate Source Citations
             ▼
Combine with Chat Memory
             ▼
Ollama
             ▼
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

## Hybrid Search
The chatbot combines lexical and semantic retrieval to improve answer quality.

Hybrid retrieval consists of:

- BM25 keyword search
- Vector similarity search
- Reciprocal Rank Fusion (RRF)

Benefits:

- Better handling of exact class names
- Better semantic understanding
- More accurate retrieval for technical documentation

Pipeline:

```text
Question
     │
     ├──────────────┐
     ▼              ▼
 BM25 Search   Vector Search
     │              │
     └──────┬───────┘
            ▼
 Reciprocal Rank Fusion
            ▼
 Top document chunks
 ```
## Source Citations

Each generated answer is accompanied by citations pointing to the document chunks used during generation.

Example response:

```json
{
  "answer": "Spring Boot simplifies application configuration [S1].",
  "sources": [
    {
      "citation": "S1",
      "sourceName": "spring-guide.pdf",
      "chunkNumber": 4,
      "score": 0.032
    }
  ]
}
 ```

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
  "answer": "Spring Boot simplifies configuration [S1].",
  "sources": [
    {
      "citation": "S1",
      "sourceName": "spring-guide.pdf",
      "chunkNumber": 2,
      "score": 0.031
    }
  ]
}
```
### Stream Chat

#### Request

GET /api/chat/stream

Parameters

- sessionId
- message

Response

Server-Sent Events

Example:

event: token
data: {"text":"Spring"}

event: token
data: {"text":" Boot"}

event: sources
data: [...]

event: done
data: {}


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
│   ├── ChatResult
│   ├── SearchResult
│   ├── DocumentSearchResult
│   ├── DocumentSource
│   ├── StreamTokenEvent
│   └── StreamErrorEvent  
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
│   ├── DocumentProcessorRegistry
│   ├──ChatService
│   ├── ChatMemoryService
│   ├── ChunkingService
│   ├── DocumentIngestionService
│   ├── DocumentSearchService
│   ├── EmbeddingService
│   ├── ReciprocalRankFusion
│   ├── EmbeddingProvider
│   ├── EmbeddingProviderRegistry
│   └── OllamaEmbeddingProvider
└── processor
    ├── PdfDocumentProcessor
    └── TxtDocumentProcessor
```
## End-to-End Workflow

```text
Upload Document
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
────────────────────────────────────────────
User Question
      │
      ▼
Generate Query Embedding
      │
      ├─────────────┐
      ▼             ▼
BM25 Search   Vector Search
      │             │
      └──────┬──────┘
             ▼
Reciprocal Rank Fusion
             ▼
Top Chunks
             ▼
Generate Source Citations
             ▼
Combine with Chat Memory
             ▼
StreamingChatModel
             ▼
Stream Tokens (SSE)
             ▼
Return Sources
             ▼
Persist Conversation
```