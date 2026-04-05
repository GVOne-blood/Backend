# RAG Chatbot Architecture - SpringFood

## 🎯 Tổng quan

Triển khai RAG (Retrieval-Augmented Generation) Chatbot sử dụng:
- **Vector Database**: pgvector (PostgreSQL extension) - Tận dụng PostgreSQL hiện có
- **Embedding Model**: Gemini Embedding API (text-embedding-004) - FREE
- **LLM**: Gemini 1.5 Flash (đã có) - FREE
- **Framework**: Spring AI + Spring Boot
- **Storage**: PostgreSQL + Redis (cache)

---

## 🏗️ Kiến trúc Tổng thể

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         DATA INGESTION LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                  │
│  │ Admin API    │  │ Kafka Events │  │ Scheduled    │                  │
│  │ (Manual)     │  │ (Auto)       │  │ Jobs         │                  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘                  │
│         │                 │                 │                           │
│         └─────────────────┴─────────────────┘                           │
│                           │                                              │
│                           ▼                                              │
│              ┌─────────────────────────┐                                │
│              │  Document Processor     │                                │
│              │  - Text extraction      │                                │
│              │  - Chunking (512 tokens)│                                │
│              │  - Metadata extraction  │                                │
│              └────────────┬────────────┘                                │
│                           │                                              │
│                           ▼                                              │
│              ┌─────────────────────────┐                                │
│              │  Embedding Service      │                                │
│              │  (Gemini text-embedding)│                                │
│              │  - Batch processing     │                                │
│              │  - Rate limiting        │                                │
│              └────────────┬────────────┘                                │
│                           │                                              │
│                           ▼                                              │
│              ┌─────────────────────────┐                                │
│              │  Vector Store           │                                │
│              │  (pgvector)             │                                │
│              │  - HNSW index           │                                │
│              │  - Cosine similarity    │                                │
│              └─────────────────────────┘                                │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                         RETRIEVAL LAYER                                  │
│                                                                          │
│  User Query → Embedding → Vector Search → Top-K Results → Reranking    │
│                                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                 │
│  │ Query        │  │ Semantic     │  │ Hybrid       │                 │
│  │ Embedding    │→ │ Search       │→ │ Search       │                 │
│  │ (Gemini)     │  │ (pgvector)   │  │ (Optional)   │                 │
│  └──────────────┘  └──────────────┘  └──────────────┘                 │
│                                              │                           │
│                                              ▼                           │
│                                   ┌──────────────────┐                  │
│                                   │ Context Builder  │                  │
│                                   │ - Top 5 chunks   │                  │
│                                   │ - Metadata       │                  │
│                                   │ - Deduplication  │                  │
│                                   └────────┬─────────┘                  │
└──────────────────────────────────────────────┼──────────────────────────┘
                                               │
┌──────────────────────────────────────────────┼──────────────────────────┐
│                         GENERATION LAYER     ▼                           │
│                                                                          │
│              ┌─────────────────────────────────────┐                    │
│              │  RAG Prompt Builder                 │                    │
│              │  System: "You are SpringFood AI"   │                    │
│              │  Context: [Retrieved chunks]        │                    │
│              │  History: [Last 5 messages]         │                    │
│              │  Query: [User question]             │                    │
│              └────────────┬────────────────────────┘                    │
│                           │                                              │
│                           ▼                                              │
│              ┌─────────────────────────┐                                │
│              │  Gemini 1.5 Flash       │                                │
│              │  - Streaming response   │                                │
│              │  - Citation generation  │                                │
│              │  - Fallback handling    │                                │
│              └────────────┬────────────┘                                │
│                           │                                              │
│                           ▼                                              │
│              ┌─────────────────────────┐                                │
│              │  Response Post-process  │                                │
│              │  - Add citations        │                                │
│              │  - Format markdown      │                                │
│              │  - Track metrics        │                                │
│              └─────────────────────────┘                                │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Database Schema

### Vector Store Table (pgvector)

```sql
-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Knowledge base documents
CREATE TABLE knowledge_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    source_type VARCHAR(50) NOT NULL, -- 'PRODUCT', 'ORDER', 'POLICY', 'FAQ', 'MANUAL'
    source_id VARCHAR(255), -- Reference to original entity (product_id, order_id, etc.)
    metadata JSONB, -- Additional metadata (category, tags, author, etc.)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Vector embeddings for semantic search
CREATE TABLE knowledge_embeddings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL REFERENCES knowledge_documents(id) ON DELETE CASCADE,
    chunk_index INTEGER NOT NULL, -- Order of chunk in document
    chunk_text TEXT NOT NULL, -- Actual text chunk (512 tokens)
    embedding vector(768), -- Gemini text-embedding-004 produces 768-dim vectors
    token_count INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT unique_document_chunk UNIQUE (document_id, chunk_index)
);

-- HNSW index for fast similarity search
CREATE INDEX ON knowledge_embeddings 
USING hnsw (embedding vector_cosine_ops)
WITH (m = 16, ef_construction = 64);

-- Additional indexes
CREATE INDEX idx_embeddings_document_id ON knowledge_embeddings(document_id);
CREATE INDEX idx_documents_source ON knowledge_documents(source_type, source_id);
CREATE INDEX idx_documents_active ON knowledge_documents(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_documents_metadata ON knowledge_documents USING gin(metadata);

-- Search statistics
CREATE TABLE rag_search_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    query TEXT NOT NULL,
    query_embedding vector(768),
    retrieved_document_ids UUID[],
    response_generated BOOLEAN,
    response_time_ms INTEGER,
    feedback_score INTEGER, -- 1-5 rating from user
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_search_logs_user ON rag_search_logs(user_id);
CREATE INDEX idx_search_logs_created ON rag_search_logs(created_at);
```

---

## 🔧 Tech Stack Details

### Dependencies (chat/pom.xml)

```xml
<!-- Already have Spring AI -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-vertex-ai-gemini-spring-boot-starter</artifactId>
</dependency>

<!-- Add pgvector support -->
<dependency>
    <groupId>com.pgvector</groupId>
    <artifactId>pgvector</artifactId>
    <version>0.1.4</version>
</dependency>

<!-- Spring AI Vector Store -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-pgvector-store-spring-boot-starter</artifactId>
</dependency>

<!-- Text processing -->
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-core</artifactId>
    <version>2.9.1</version>
</dependency>
```

---

## 🚀 Implementation Phases

### Phase 1: Vector Store Setup (Week 1)
1. ✅ Enable pgvector extension in PostgreSQL
2. ✅ Create database schema
3. ✅ Implement Document entity & repository
4. ✅ Implement Embedding entity & repository
5. ✅ Create VectorStoreService

### Phase 2: Data Ingestion API (Week 1-2)
1. ✅ REST API for manual document upload
2. ✅ Document processor (chunking, cleaning)
3. ✅ Embedding service (Gemini API integration)
4. ✅ Batch processing for large documents
5. ✅ Kafka consumer for auto-ingestion

### Phase 3: Retrieval Service (Week 2)
1. ✅ Semantic search implementation
2. ✅ Hybrid search (vector + keyword)
3. ✅ Context builder
4. ✅ Reranking logic
5. ✅ Caching with Redis

### Phase 4: RAG Integration (Week 2-3)
1. ✅ Enhance GeminiAIService with RAG
2. ✅ Prompt engineering for RAG
3. ✅ Citation generation
4. ✅ Streaming response with context
5. ✅ Fallback handling

### Phase 5: Admin & Monitoring (Week 3)
1. ✅ Admin API for knowledge management
2. ✅ Search analytics dashboard
3. ✅ Quality metrics (relevance, accuracy)
4. ✅ A/B testing framework
5. ✅ Performance monitoring

---

## 📝 API Endpoints Design

### Data Ingestion APIs

```
POST /api/v1/chat/rag/documents
- Upload document manually (PDF, TXT, JSON)
- Admin only

POST /api/v1/chat/rag/documents/batch
- Batch upload multiple documents
- Admin only

POST /api/v1/chat/rag/documents/from-product/{productId}
- Auto-generate knowledge from product data
- Admin only

POST /api/v1/chat/rag/documents/from-order/{orderId}
- Auto-generate knowledge from order data
- Admin only

PUT /api/v1/chat/rag/documents/{documentId}
- Update existing document
- Admin only

DELETE /api/v1/chat/rag/documents/{documentId}
- Soft delete document
- Admin only

GET /api/v1/chat/rag/documents
- List all documents with pagination
- Admin only

GET /api/v1/chat/rag/documents/{documentId}
- Get document details
- Admin only
```

### Search & Query APIs

```
POST /api/v1/chat/rag/search
- Semantic search in knowledge base
- Returns top-K relevant chunks
- Public (with auth)

POST /api/v1/chat/ai/ask
- Enhanced with RAG
- Existing endpoint, add RAG context
- Public (with auth)
```

### Analytics APIs

```
GET /api/v1/chat/rag/analytics/searches
- Search statistics
- Admin only

GET /api/v1/chat/rag/analytics/documents
- Document usage statistics
- Admin only

POST /api/v1/chat/rag/feedback
- Submit feedback on RAG response
- Public (with auth)
```

---

## 🔐 Security Considerations

1. **Authentication**: Reuse existing JWT authentication
2. **Authorization**: Admin-only for ingestion APIs
3. **Rate Limiting**: Prevent abuse of embedding API
4. **Data Privacy**: Mask PII in embeddings
5. **Audit Logging**: Track all document operations

---

## 💰 Cost Estimation (FREE Tier)

**Gemini API (100% FREE):**
- Embedding API: 1,500 requests/day FREE
- Gemini 1.5 Flash: 15 requests/minute FREE
- Total: $0/month

**Infrastructure:**
- PostgreSQL: Already have
- Redis: Already have
- Storage: ~1GB for 10K documents

**Total Monthly Cost: $0** (within free tier)

---

## 📈 Performance Targets

- **Embedding Generation**: < 500ms per document
- **Vector Search**: < 100ms for top-10 results
- **End-to-End RAG**: < 2s (including LLM generation)
- **Throughput**: 100 concurrent users
- **Accuracy**: > 80% relevance score

---

## 🎯 Next Steps

1. Review and approve architecture
2. Setup pgvector in PostgreSQL
3. Start Phase 1 implementation
4. Create detailed API specifications
5. Design admin UI mockups
