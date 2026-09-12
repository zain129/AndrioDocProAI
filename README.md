# AndrioDocPro AI

**Enterprise Document Intelligence & Retrieval Platform**

AndrioDocPro AI is an enterprise-grade Retrieval-Augmented Generation (RAG) platform developed by **Andrisol Technologies**. It enables organizations to upload, version, categorize, and manage documents while providing AI-powered question answering using the document content.

The platform supports multiple document types, semantic search using vector embeddings, and contextual responses powered by Large Language Models (LLMs).

---

## Features

### Document Management

- Upload PDF and Microsoft Word (.docx) documents
- Version management
- Multiple categories
- Multiple countries per document
- Metadata management
- Document lifecycle management

### AI Capabilities

- Automatic document chunking
- Vector embedding generation
- Semantic similarity search
- Retrieval-Augmented Generation (RAG)
- Context-aware responses
- Source citations
- Conversation history

### Enterprise Features

- JWT Authentication
- Role-based access control
- REST APIs
- OpenAPI Documentation
- Audit Logging
- Docker Support
- PostgreSQL + pgvector

---

## Planned Features

- OCR support
- Image document processing
- Multi-language embeddings
- Hybrid Search (Keyword + Vector)
- Elasticsearch integration
- Streaming AI responses
- Multi-tenant support
- Local LLM support
- AI Agents
- Workflow automation

---

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| Framework | Micronaut |
| JDK | 17 |
| Build Tool | Gradle Kotlin DSL |
| Database | PostgreSQL |
| Vector Store | pgvector |
| AI Provider | Hugging Face |
| PDF Parser | Apache PDFBox |
| Document Parser | Apache POI |
| Security | JWT |
| Documentation | OpenAPI |
| Containerization | Docker |

---

## Project Structure

```
andriodocpro-ai
│
├── src
│   └── main
│       ├── kotlin
│       └── resources
│
├── docker
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

> **Note:** This public repository is the Community Edition — the internal test suite and API-testing collection are kept in the private development repo and are not published here.

---

## Document Metadata

Every uploaded document contains:

| Field | Required |
|---------|----------|
| Name | ✅ |
| Version | ✅ |
| Category | Optional |
| Countries | Optional |
| Description | Optional |
| File | ✅ |

---

## Supported Documents

- PDF
- DOCX

Future support:

- XLSX
- PPTX
- HTML
- Markdown
- TXT

---

## High-Level Architecture

```
        Upload Document                       User Question
              │                                      │
              ▼                                      ▼
      Document Processing                     Embed Question
              │
  ┌───────────┴───────────┐
  ▼                        ▼
Text Extraction      Metadata Storage
  │
  ▼
Chunk Generation
  │
  ▼
Embedding Generation
  │
  ▼
              PostgreSQL + pgvector  ◄─────────────────┘
                       │
                       ▼
               Semantic Search (top-K)
                       │
                       ▼
             Retrieved Chunks + Sources
                       │
                       ▼
                 Hugging Face LLM
                       │
                       ▼
             AI Answer + Citations
```

---

## Development Roadmap

### Phase 1

- Project setup
- PostgreSQL
- Docker
- Document upload
- PDF parsing

### Phase 2

- Chunk generation
- Embeddings
- Vector search
- AI chat

### Phase 3

- Conversation history
- Security
- User management
- API documentation

### Phase 4

- OCR
- Hybrid Search
- Elasticsearch
- Local LLMs

---

## License

Licensed under the [MIT License](LICENSE).

Copyright © 2026 Andrisol Technologies