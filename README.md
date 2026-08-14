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
- PostgreSQL-backed session-based conversational memory
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
- Fully containerized runtime with Docker Compose
- PostgreSQL database support

## Technology Stack

* Java 21
* Spring Boot 3
* LangChain4j
* Ollama
* Elasticsearch
* PostgreSQL
* Docker / Docker Compose
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

Conversation history is persisted in PostgreSQL per session and survives application restarts.

The application stores the complete conversation history and loads the latest 10 messages for each session into the LLM context, enabling contextual conversations without allowing the prompt to grow indefinitely.

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

The complete application stack is containerized with Docker Compose.

### Docker Services

The Compose environment runs:

- `ai-chatbot` — Spring Boot application
- `ollama` — local LLM and embedding models
- `elasticsearch` — document chunks, BM25 search, and vector search
- `postgres` — relational database

Persistent Docker volumes are used for Ollama models, Elasticsearch data, and PostgreSQL data.

### Build and Start the Complete Stack

```bash
docker compose up --build -d
```

For a clean rebuild without Docker build cache:

```bash
docker compose down
docker compose build --no-cache
docker compose up -d
```

Check service status:

```bash
docker compose ps
```

### Service Endpoints

| Service | Host endpoint | Container endpoint |
| --- | --- | --- |
| AI Chatbot | `http://localhost:8080` | `http://ai-chatbot:8080` |
| Ollama | `http://localhost:11434` | `http://ollama:11434` |
| Elasticsearch | `http://localhost:9200` | `http://elasticsearch:9200` |
| PostgreSQL | `localhost:5432` | `postgres:5432` |

Docker service names are used for communication between containers. The Spring Boot container therefore connects to `ollama`, `elasticsearch`, and `postgres` instead of `localhost`.

### Ollama Models

The environment uses the following models:

```text
llama3.1
nomic-embed-text
```

If the models are not initialized automatically, pull them with:

```bash
docker exec -it ollama ollama pull llama3.1
docker exec -it ollama ollama pull nomic-embed-text
```

### PostgreSQL

PostgreSQL replaces the previous H2 runtime database. Spring Boot connects through the PostgreSQL JDBC driver and Spring Data JPA.

Docker configuration uses:

```text
Database: aichatbot
Host inside Docker: postgres
Port: 5432
```

Database data is stored in the `postgres-data` Docker volume and survives container recreation.

### Elasticsearch Health Check

From the host:

```bash
curl http://localhost:9200/_cluster/health
```

### Stop the Environment

```bash
docker compose down
```

To also remove persistent volumes and all stored data:

```bash
docker compose down -v
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

Conversation history is persisted in PostgreSQL through the `ChatMemoryService` abstraction. Each conversation is associated with a `sessionId`, while individual messages preserve their role (`USER` or `AI`), content, and timestamp.

The database keeps the complete conversation history. When processing a new request, the application loads the latest 10 messages and restores them in chronological order before sending them to the LLM. Clearing chat memory removes the corresponding persisted conversation.

```text
ChatService
      │
      ▼
ChatMemoryService
      │
      ▼
ChatMemoryServiceImpl
      │
      ├── ConversationRepository
      └── ChatMessageRepository
                 │
                 ▼
             PostgreSQL
```

### Conversation Persistence Tests

The persistence layer is covered by both unit and integration tests. Unit tests mock the repositories and verify the `ChatMemoryService` behavior in isolation. Integration tests use a real PostgreSQL instance through Testcontainers to verify JPA mappings, persistence, ordering, session isolation, the latest-10-message limit, and clearing conversation history.

Key integration scenarios:

- Persist and reload USER and AI messages
- Restore messages in chronological order
- Keep different `sessionId` conversations isolated
- Load only the latest 10 messages into the LLM context
- Clear one conversation without affecting other sessions

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
│   ├── ChunkRepository
│   ├── ConversationRepository
│   └── ChatMessageRepository
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