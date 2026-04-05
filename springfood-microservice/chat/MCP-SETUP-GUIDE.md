# MCP (Function Calling) Setup Guide

## Tổng quan

Chat service đã được tích hợp Spring AI 1.0.0 GA với Google Gemini để hỗ trợ MCP (Model Context Protocol) - hay còn gọi là Function Calling. Điều này cho phép AI có thể gọi các API của microservices để thực hiện các tác vụ thực tế.

## Kiến trúc

```
User → Chat Service → Gemini AI → Function Calls → Microservices APIs
                          ↓
                    Chat Memory (JDBC)
                          ↓
                    Vector Store (PgVector)
```

## Cấu hình

### 1. Environment Variables

Thêm vào file `.env` (đã có sẵn):

```env
# Google Gemini AI
GEMINI_API_KEY=AIzaSyCJ1ZuBXIn_IrG6rzij4G_jilhUqXtc-eQ
```

### 2. Application Configuration

File `chat/src/main/resources/config/application.yml` đã được cấu hình:

```yaml
spring:
  ai:
    google:
      ai:
        gemini:
          api-key: ${GEMINI_API_KEY}
          model: ${GEMINI_MODEL:gemini-1.5-flash}
    
    vectorstore:
      pgvector:
        schema-name: chat
        index-type: HNSW
        distance-type: COSINE_DISTANCE
        dimensions: 768
        initialize-schema: true
```

### 3. Dependencies

File `chat/pom.xml` đã có các dependencies cần thiết:

- `spring-ai-starter-model-google-gemini` - Google Gemini API
- `spring-ai-starter-model-chat-memory-repository-jdbc` - Chat memory
- `spring-ai-starter-vector-store-pgvector` - Vector store cho RAG
- `spring-ai-advisors-vector-store` - RAG advisors
- `spring-ai-starter-mcp-client` - MCP client (optional)

## MCP Tools đã implement

### 1. Search Products
```java
@Tool(description = "Search for products by name, category, or keywords")
public ProductSearchResponse searchProducts(
    @ToolParam(description = "Search query") String query,
    @ToolParam(description = "Page number", required = false) Integer page,
    @ToolParam(description = "Page size", required = false) Integer size
)
```

### 2. Get Product Detail
```java
@Tool(description = "Get detailed information about a specific product")
public ProductDetailResponse getProductDetail(
    @ToolParam(description = "Product ID") Long productId
)
```

### 3. Add to Cart
```java
@Tool(description = "Add a product to user's shopping cart")
public CartResponse addToCart(
    @ToolParam(description = "Product ID") Long productId,
    @ToolParam(description = "Quantity") Integer quantity,
    @ToolParam(description = "Variant ID", required = false) Long variantId
)
```

### 4. Get Cart
```java
@Tool(description = "Get user's current shopping cart")
public CartResponse getCart()
```

### 5. Create Order
```java
@Tool(description = "Create an order from user's cart")
public OrderResponse createOrder(
    @ToolParam(description = "Shipping address") String shippingAddress,
    @ToolParam(description = "Payment method") String paymentMethod
)
```

## Cách hoạt động

### Spring AI 1.0.0 GA Auto-Discovery

Spring AI 1.0.0 GA tự động discover các Function beans:

1. **Không cần ToolCallbackProvider bean** - Spring Boot auto-configuration tự động làm
2. **Function beans được auto-discover** - Các `@Bean` với type `Function<Request, Response>` tự động trở thành tools
3. **@Tool annotation** - Methods annotated với `@Tool` tự động trở thành tools

### Tool Execution Flow

1. User gửi message: "Tìm sản phẩm iPhone"
2. ChatClient gửi request đến Gemini với tool definitions
3. Gemini quyết định gọi tool `searchProducts` với params `{query: "iPhone"}`
4. Spring AI tự động execute tool và trả kết quả cho Gemini
5. Gemini sử dụng kết quả để generate response cuối cùng
6. User nhận được response: "Tôi tìm thấy 5 sản phẩm iPhone..."

## Database Schema

### Chat Memory Table

Spring AI tự động tạo bảng `ai_chat_memory` để lưu conversation history:

```sql
CREATE TABLE ai_chat_memory (
    id VARCHAR(255) PRIMARY KEY,
    conversation_id VARCHAR(255) NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    content TEXT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chat_memory_conversation ON ai_chat_memory(conversation_id);
```

### Vector Store Tables

PgVector tự động tạo các bảng trong schema `chat`:

```sql
CREATE SCHEMA IF NOT EXISTS chat;

CREATE TABLE chat.vector_store (
    id UUID PRIMARY KEY,
    content TEXT,
    metadata JSONB,
    embedding vector(768)
);

CREATE INDEX ON chat.vector_store USING hnsw (embedding vector_cosine_ops);
```

## Testing

### 1. Test với ChatClient

```java
@Autowired
private ChatClient.Builder chatClientBuilder;

@Test
void testFunctionCalling() {
    ChatClient chatClient = chatClientBuilder.build();
    
    String response = chatClient.prompt()
        .user("Tìm sản phẩm iPhone cho tôi")
        .call()
        .content();
    
    assertThat(response).contains("iPhone");
}
```

### 2. Test với REST API

```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "conversationId": "test-123",
    "message": "Tìm sản phẩm iPhone cho tôi"
  }'
```

### 3. Test với HTML Client

Mở file `chat/test-resources/ai-chat-test.html` trong browser để test UI.

## Troubleshooting

### Lỗi: "API key not found"

**Nguyên nhân**: GEMINI_API_KEY chưa được set

**Giải pháp**: 
1. Check file `.env` có `GEMINI_API_KEY`
2. Restart application để load lại env vars

### Lỗi: "Tool not found"

**Nguyên nhân**: Function beans chưa được register

**Giải pháp**:
1. Check `MCPToolsConfiguration` có `@Configuration`
2. Check methods có `@Bean` annotation
3. Check Spring Boot đã scan package `com.theblood.springfood.chat.mcp`

### Lỗi: "Chat memory table not found"

**Nguyên nhân**: Database chưa có bảng `ai_chat_memory`

**Giải pháp**:
1. Spring AI sẽ tự động tạo bảng khi khởi động
2. Nếu không tự động, chạy SQL script thủ công (xem phần Database Schema)

## Next Steps

### 1. Implement Real API Calls

Hiện tại các tools đang return mock data. Cần implement real API calls:

```java
@Service
@RequiredArgsConstructor
public class MCPToolService {
    private final DefaultClientFactory clientFactory;
    
    public ProductSearchResponse searchProducts(String query, Integer page, Integer size) {
        // TODO: Call product-service API via client module
        ProductClient productClient = clientFactory.createClient(
            ProductClient.class,
            "product-service"
        );
        return productClient.searchProducts(query, page, size);
    }
}
```

### 2. Add More Tools

Thêm các tools khác:
- Update cart item quantity
- Remove item from cart
- Get order status
- Cancel order
- Get user profile
- Update user profile

### 3. Add RAG Support

Tích hợp RAG để AI có thể trả lời câu hỏi dựa trên knowledge base:

```java
@Bean
public QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
    return QuestionAnswerAdvisor.builder()
        .vectorStore(vectorStore)
        .build();
}
```

### 4. Add Observability

Enable observability cho tool calling:

```yaml
management:
  observations:
    key-values:
      application: ${spring.application.name}
  metrics:
    enable:
      spring.ai.tool: true
```

## References

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Spring AI Tool Calling](https://docs.spring.io/spring-ai/reference/api/tools.html)
- [Google Gemini API](https://ai.google.dev/docs)
- [MCP Integration Proposal](../MCP-INTEGRATION-PROPOSAL.md)
