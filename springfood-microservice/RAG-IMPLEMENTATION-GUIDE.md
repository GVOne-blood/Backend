# RAG Implementation Guide - Chat Service

## Tổng quan

Hệ thống RAG (Retrieval-Augmented Generation) đã được triển khai trong Chat service sử dụng:

- **Spring AI 1.1.1** - Framework AI cho Spring Boot
- **pgvector** - PostgreSQL extension cho vector similarity search
- **Gemini Embeddings** - Google's embedding model (768 dimensions)

## Kiến trúc

### Components

1. **VectorStoreConfig** - Cấu hình PgVectorStore
2. **DocumentIngestionService** - Upload và xử lý documents
3. **RAGService** - Semantic search và context building
4. **KnowledgeBaseController** - REST API endpoints
5. **DocumentTextExtractor** - Extract text từ Word documents

### Database Schema

```sql
-- Vector store table (tự động tạo bởi Spring AI)
CREATE TABLE chat.vector_store (
    id UUID PRIMARY KEY,
    content TEXT,
    metadata JSONB,
    embedding vector(768)
);

-- Tracking table (quản lý documents)
CREATE TABLE chat.knowledge_document (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500),
    content TEXT,
    source_type VARCHAR(50),
    source_id VARCHAR(255),
    metadata JSONB,
    vector_store_ids TEXT[],
    chunk_count INTEGER,
    is_active BOOLEAN,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP
);

-- Analytics table
CREATE TABLE chat.rag_search_log (
    id BIGSERIAL PRIMARY KEY,
    query TEXT,
    results_count INTEGER,
    top_k INTEGER,
    created_date TIMESTAMP
);
```

## API Endpoints

### 1. Upload Text Document

```bash
POST /api/knowledge-base/documents/text
Content-Type: application/json

{
  "title": "iPhone 15 Product Guide",
  "content": "iPhone 15 features A16 Bionic chip, 48MP camera...",
  "sourceType": "PRODUCT",
  "sourceId": "product_12345",
  "additionalMetadata": {
    "category": "electronics",
    "language": "vi",
    "tags": ["featured", "new"]
  }
}
```

### 2. Upload PDF Document

```bash
POST /api/knowledge-base/documents/pdf
Content-Type: multipart/form-data

curl -X POST http://localhost:8080/api/knowledge-base/documents/pdf \
  -F "file=@product_manual.pdf" \
  -F "title=Product Manual" \
  -F "sourceType=PRODUCT" \
  -F "sourceId=product_123" \
  -F "category=electronics" \
  -F "language=vi"
```

### 3. Upload Word Document

```bash
POST /api/knowledge-base/documents/word
Content-Type: multipart/form-data

curl -X POST http://localhost:8080/api/knowledge-base/documents/word \
  -F "file=@policy.docx" \
  -F "title=Return Policy" \
  -F "sourceType=POLICY" \
  -F "sourceId=policy_001"
```

### 4. Semantic Search

```bash
POST /api/knowledge-base/search
Content-Type: application/json

{
  "query": "How to use iPhone camera?",
  "topK": 5,
  "similarityThreshold": 0.7,
  "sourceType": "PRODUCT"
}
```

Response:

```json
{
  "query": "How to use iPhone camera?",
  "totalResults": 3,
  "results": [
    {
      "content": "iPhone camera features...",
      "similarity": 0.89,
      "metadata": {
        "title": "iPhone Guide",
        "source_type": "PRODUCT",
        "source_id": "product_123"
      },
      "sourceType": "PRODUCT",
      "sourceId": "product_123",
      "title": "iPhone Guide"
    }
  ],
  "context": "Relevant information:\n\n[Document 1]\nTitle: iPhone Guide\nContent: ..."
}
```

### 5. Delete Document

```bash
# Delete by source
DELETE /api/knowledge-base/documents/{sourceType}/{sourceId}

# Delete all documents of a type
DELETE /api/knowledge-base/documents/type/{sourceType}
```

## Metadata Structure

Metadata được sử dụng để:

- **Tracking**: Biết document từ đâu
- **Filtering**: Lọc kết quả search
- **Deletion**: Xóa document khi cập nhật
- **Context**: Cung cấp thông tin cho AI

### Required Metadata

```java
{
  "title": "Document title",           // Tiêu đề hiển thị
  "source_type": "PRODUCT",            // Loại: PRODUCT, ORDER, FAQ, POLICY, GENERAL
  "source_id": "product_123"           // ID để tracking và deletion
}
```

### Optional Metadata

```java
{
  "category": "electronics",           // Phân loại
  "language": "vi",                    // Ngôn ngữ
  "tags": ["featured", "new"],         // Tags
  "author": "admin",                   // Tác giả
  "filename": "document.pdf",          // Tên file gốc
  "custom_field": "any value"          // Bất kỳ field nào
}
```

## Use Cases

### 1. Product Information RAG

```java
// Upload product info
DocumentUploadRequest request = DocumentUploadRequest.builder()
    .title("iPhone 15 Pro")
    .content("Detailed product specifications...")
    .sourceType("PRODUCT")
    .sourceId("product_iphone15")
    .additionalMetadata(Map.of(
        "category", "smartphones",
        "brand", "Apple",
        "price_range", "high-end"
    ))
    .build();

// Search when customer asks
RAGSearchRequest searchRequest = RAGSearchRequest.builder()
    .query("What are the camera features?")
    .topK(3)
    .sourceType("PRODUCT")
    .build();
```

### 2. FAQ System

```java
// Upload FAQ
request.setSourceType("FAQ");
request.setSourceId("faq_shipping");
request.setContent("Q: How long does shipping take? A: 3-5 business days...");

// Search FAQ
searchRequest.setSourceType("FAQ");
searchRequest.setQuery("shipping time");
```

### 3. Policy Documents

```java
// Upload policy
request.setSourceType("POLICY");
request.setSourceId("policy_return");
request.setContent("Return policy: Items can be returned within 30 days...");
```

### 4. Update Outdated Documents

```java
// Delete old version
DELETE /api/knowledge-base/documents/PRODUCT/product_123

// Upload new version
POST /api/knowledge-base/documents/text
{
  "sourceType": "PRODUCT",
  "sourceId": "product_123",  // Same ID
  "content": "Updated information..."
}
```

## Configuration

### application.yml

```yaml
spring:
  ai:
    vectorstore:
      pgvector:
        initialize-schema: true
        index-type: HNSW
        distance-type: COSINE_DISTANCE
        dimensions: 768
        schema-name: chat
        table-name: vector_store
```

### Dependencies

```xml
<!-- Spring AI -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-google-genai</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-pgvector</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-pdf-document-reader</artifactId>
</dependency>

<!-- pgvector -->
<dependency>
    <groupId>com.pgvector</groupId>
    <artifactId>pgvector</artifactId>
    <version>0.1.6</version>
</dependency>

<!-- Apache POI for Word -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

## Supported File Formats

- ✅ **Plain Text** (.txt)
- ✅ **PDF** (.pdf) - via Spring AI PDF Reader
- ✅ **Word** (.docx) - via Apache POI
- ❌ **Excel** (.xlsx) - Chưa hỗ trợ (có thể thêm sau)
- ❌ **Old Word** (.doc) - Chỉ hỗ trợ .docx

## Performance Tuning

### Chunking Strategy

```java
// Current settings
TokenTextSplitter(
    chunkSize: 512,      // Tokens per chunk
    overlap: 50,         // Overlap between chunks
    minChunkSize: 5,     // Minimum chunk size
    maxChunkSize: 10000, // Maximum chunk size
    keepSeparator: true  // Keep separators
)
```

### Search Parameters

- **topK**: 3-10 (số lượng kết quả)
- **similarityThreshold**: 0.6-0.8 (ngưỡng similarity)
- **dimensions**: 768 (Gemini embedding size)

## Integration với AI Assistant

```java
// 1. Search relevant context
RAGSearchResponse ragResults = ragService.search(searchRequest);

// 2. Build prompt with context
String prompt = String.format("""
    Context: %s
    
    User question: %s
    
    Please answer based on the context above.
    """, ragResults.getContext(), userQuestion);

// 3. Send to AI
AIMessageResponse response = geminiService.chat(prompt);
```

## Troubleshooting

### 1. No results found

- Check similarity threshold (lower it)
- Verify documents were uploaded successfully
- Check metadata filters

### 2. Poor search quality

- Increase chunk overlap
- Adjust chunk size
- Add more relevant documents
- Use better metadata

### 3. Slow performance

- Add indexes on metadata fields
- Reduce topK value
- Use metadata filters to narrow search

## Next Steps

1. ✅ Basic RAG implementation
2. ✅ Document upload (text, PDF, Word)
3. ✅ Semantic search
4. ✅ Document deletion/update
5. ⏳ Excel support
6. ⏳ Image extraction from PDFs
7. ⏳ Advanced filtering
8. ⏳ Hybrid search (keyword + semantic)
