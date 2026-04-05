# MCP Integration Proposal - SpringFood Microservices

## 🎯 Tổng quan

Model Context Protocol (MCP) sẽ biến AI chatbot từ "chỉ trả lời câu hỏi" thành "AI assistant có thể thực hiện actions". Kết hợp với RAG hiện có, bạn sẽ có một hệ thống AI toàn diện.

## 🔄 So sánh RAG vs MCP

| Khả năng | RAG (Hiện tại) | MCP (Đề xuất) |
|----------|----------------|---------------|
| Trả lời từ documents | ✅ | ✅ |
| Tìm kiếm sản phẩm | ❌ | ✅ |
| Đặt hàng | ❌ | ✅ |
| Kiểm tra trạng thái đơn | ❌ | ✅ |
| Cập nhật giỏ hàng | ❌ | ✅ |
| Truy vấn database | ❌ | ✅ |
| Gọi API microservices | ❌ | ✅ |
| Real-time data | ❌ | ✅ |

## 🏗️ Kiến trúc MCP cho SpringFood

```
┌─────────────────────────────────────────────────────────────────┐
│                      USER INTERACTION                            │
│  Chat UI → WebSocket → Chat Service → AI Assistant              │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────┴────────────────────────────────────┐
│                    AI DECISION LAYER                             │
│                                                                  │
│  ┌──────────────┐         ┌──────────────┐                     │
│  │ RAG Service  │         │ MCP Client   │                     │
│  │ (Documents)  │         │ (Actions)    │                     │
│  └──────┬───────┘         └──────┬───────┘                     │
│         │                        │                              │
│         │  "Gợi ý món ăn"       │  "Đặt món này"              │
│         │  "Chính sách giao"    │  "Kiểm tra đơn"             │
│         │                        │                              │
└─────────┼────────────────────────┼──────────────────────────────┘
          │                        │
          ▼                        ▼
┌──────────────────┐    ┌──────────────────────────────────────┐
│  Vector Store    │    │       MCP TOOLS LAYER                │
│  (pgvector)      │    │                                       │
│  - Product docs  │    │  ┌─────────────────────────────────┐ │
│  - FAQ           │    │  │ Product Service Tools           │ │
│  - Policies      │    │  │ - search_products()             │ │
└──────────────────┘    │  │ - get_product_details()         │ │
                        │  │ - check_stock()                 │ │
                        │  └─────────────────────────────────┘ │
                        │                                       │
                        │  ┌─────────────────────────────────┐ │
                        │  │ Order Service Tools             │ │
                        │  │ - create_order()                │ │
                        │  │ - get_order_status()            │ │
                        │  │ - cancel_order()                │ │
                        │  └─────────────────────────────────┘ │
                        │                                       │
                        │  ┌─────────────────────────────────┐ │
                        │  │ Cart Service Tools              │ │
                        │  │ - add_to_cart()                 │ │
                        │  │ - view_cart()                   │ │
                        │  │ - update_quantity()             │ │
                        │  └─────────────────────────────────┘ │
                        │                                       │
                        │  ┌─────────────────────────────────┐ │
                        │  │ Shop Service Tools              │ │
                        │  │ - search_shops()                │ │
                        │  │ - get_shop_menu()               │ │
                        │  └─────────────────────────────────┘ │
                        │                                       │
                        │  ┌─────────────────────────────────┐ │
                        │  │ Database Query Tools            │ │
                        │  │ - query_orders()                │ │
                        │  │ - query_products()              │ │
                        │  └─────────────────────────────────┘ │
                        └───────────────────────────────────────┘
                                        │
                        ┌───────────────┼───────────────┐
                        ▼               ▼               ▼
                ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
                │ Product      │ │ Order        │ │ Cart         │
                │ Service      │ │ Service      │ │ Service      │
                └──────────────┘ └──────────────┘ └──────────────┘
```

## 📋 MCP Tools cần triển khai

### 1. Product Service MCP Tools

```typescript
// Tool: search_products
{
  name: "search_products",
  description: "Tìm kiếm sản phẩm theo từ khóa, category, giá",
  parameters: {
    query: "string",
    category: "string?",
    minPrice: "number?",
    maxPrice: "number?",
    limit: "number?"
  }
}

// Tool: get_product_details
{
  name: "get_product_details",
  description: "Lấy chi tiết sản phẩm theo ID",
  parameters: {
    productId: "string"
  }
}

// Tool: check_product_availability
{
  name: "check_product_availability",
  description: "Kiểm tra sản phẩm còn hàng không",
  parameters: {
    productId: "string",
    quantity: "number"
  }
}
```

### 2. Order Service MCP Tools

```typescript
// Tool: create_order
{
  name: "create_order",
  description: "Tạo đơn hàng mới",
  parameters: {
    userId: "string",
    items: "array",
    deliveryAddress: "string",
    paymentMethod: "string"
  }
}

// Tool: get_order_status
{
  name: "get_order_status",
  description: "Kiểm tra trạng thái đơn hàng",
  parameters: {
    orderId: "string"
  }
}

// Tool: get_user_orders
{
  name: "get_user_orders",
  description: "Lấy danh sách đơn hàng của user",
  parameters: {
    userId: "string",
    status: "string?",
    limit: "number?"
  }
}

// Tool: cancel_order
{
  name: "cancel_order",
  description: "Hủy đơn hàng",
  parameters: {
    orderId: "string",
    reason: "string"
  }
}
```

### 3. Cart Service MCP Tools

```typescript
// Tool: add_to_cart
{
  name: "add_to_cart",
  description: "Thêm sản phẩm vào giỏ hàng",
  parameters: {
    userId: "string",
    productId: "string",
    quantity: "number",
    variantId: "string?"
  }
}

// Tool: view_cart
{
  name: "view_cart",
  description: "Xem giỏ hàng hiện tại",
  parameters: {
    userId: "string"
  }
}

// Tool: update_cart_item
{
  name: "update_cart_item",
  description: "Cập nhật số lượng trong giỏ",
  parameters: {
    userId: "string",
    itemId: "string",
    quantity: "number"
  }
}

// Tool: clear_cart
{
  name: "clear_cart",
  description: "Xóa toàn bộ giỏ hàng",
  parameters: {
    userId: "string"
  }
}
```

### 4. Shop Service MCP Tools

```typescript
// Tool: search_shops
{
  name: "search_shops",
  description: "Tìm kiếm cửa hàng/nhà hàng",
  parameters: {
    query: "string",
    location: "string?",
    cuisine: "string?",
    rating: "number?"
  }
}

// Tool: get_shop_menu
{
  name: "get_shop_menu",
  description: "Lấy menu của shop",
  parameters: {
    shopId: "string",
    category: "string?"
  }
}

// Tool: get_shop_info
{
  name: "get_shop_info",
  description: "Lấy thông tin chi tiết shop",
  parameters: {
    shopId: "string"
  }
}
```

### 5. Database Query Tools

```typescript
// Tool: query_database
{
  name: "query_database",
  description: "Truy vấn database với SQL an toàn (read-only)",
  parameters: {
    service: "string", // product, order, cart, shop
    query: "string",   // Parameterized query
    params: "object"
  }
}
```

## 🔧 Implementation Plan

### Phase 1: MCP Server Setup (Week 1)

#### 1.1. Tạo MCP Server Module

```
springfood-microservice/
├── mcp-server/
│   ├── src/main/java/com/theblood/springfood/mcp/
│   │   ├── MCPServerApp.java
│   │   ├── config/
│   │   │   ├── MCPConfiguration.java
│   │   │   └── SecurityConfiguration.java
│   │   ├── tools/
│   │   │   ├── ProductTools.java
│   │   │   ├── OrderTools.java
│   │   │   ├── CartTools.java
│   │   │   ├── ShopTools.java
│   │   │   └── DatabaseTools.java
│   │   ├── client/
│   │   │   ├── ProductServiceClient.java
│   │   │   ├── OrderServiceClient.java
│   │   │   ├── CartServiceClient.java
│   │   │   └── ShopServiceClient.java
│   │   └── dto/
│   │       ├── MCPRequest.java
│   │       ├── MCPResponse.java
│   │       └── ToolDefinition.java
│   ├── pom.xml
│   └── src/main/resources/
│       └── application.yml
```

#### 1.2. Dependencies (mcp-server/pom.xml)

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Cloud OpenFeign (gọi microservices) -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
    
    <!-- gRPC Client (nếu dùng gRPC) -->
    <dependency>
        <groupId>net.devh</groupId>
        <artifactId>grpc-client-spring-boot-starter</artifactId>
    </dependency>
    
    <!-- MCP Protocol -->
    <dependency>
        <groupId>io.modelcontextprotocol</groupId>
        <artifactId>mcp-java-sdk</artifactId>
        <version>1.0.0</version>
    </dependency>
    
    <!-- Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
</dependencies>
```

### Phase 2: Implement MCP Tools (Week 1-2)

#### 2.1. Product Tools Implementation

```java
@Component
@RequiredArgsConstructor
public class ProductTools implements MCPToolProvider {
    
    private final ProductServiceClient productClient;
    
    @MCPTool(
        name = "search_products",
        description = "Tìm kiếm sản phẩm theo từ khóa, category, giá"
    )
    public MCPResponse searchProducts(
        @MCPParam("query") String query,
        @MCPParam("category") Optional<String> category,
        @MCPParam("minPrice") Optional<Double> minPrice,
        @MCPParam("maxPrice") Optional<Double> maxPrice,
        @MCPParam("limit") Optional<Integer> limit
    ) {
        try {
            var products = productClient.searchProducts(
                query, 
                category.orElse(null),
                minPrice.orElse(null),
                maxPrice.orElse(null),
                limit.orElse(10)
            );
            
            return MCPResponse.success(products);
        } catch (Exception e) {
            return MCPResponse.error("Failed to search products: " + e.getMessage());
        }
    }
    
    @MCPTool(
        name = "get_product_details",
        description = "Lấy chi tiết sản phẩm theo ID"
    )
    public MCPResponse getProductDetails(@MCPParam("productId") String productId) {
        try {
            var product = productClient.getProductById(productId);
            return MCPResponse.success(product);
        } catch (Exception e) {
            return MCPResponse.error("Product not found: " + productId);
        }
    }
    
    @MCPTool(
        name = "check_product_availability",
        description = "Kiểm tra sản phẩm còn hàng không"
    )
    public MCPResponse checkAvailability(
        @MCPParam("productId") String productId,
        @MCPParam("quantity") int quantity
    ) {
        try {
            var available = productClient.checkStock(productId, quantity);
            return MCPResponse.success(Map.of(
                "productId", productId,
                "requestedQuantity", quantity,
                "available", available
            ));
        } catch (Exception e) {
            return MCPResponse.error("Failed to check availability");
        }
    }
}
```

#### 2.2. Order Tools Implementation

```java
@Component
@RequiredArgsConstructor
public class OrderTools implements MCPToolProvider {
    
    private final OrderServiceClient orderClient;
    
    @MCPTool(
        name = "create_order",
        description = "Tạo đơn hàng mới"
    )
    public MCPResponse createOrder(
        @MCPParam("userId") String userId,
        @MCPParam("items") List<OrderItem> items,
        @MCPParam("deliveryAddress") String deliveryAddress,
        @MCPParam("paymentMethod") String paymentMethod
    ) {
        try {
            var order = orderClient.createOrder(
                userId, items, deliveryAddress, paymentMethod
            );
            return MCPResponse.success(order);
        } catch (Exception e) {
            return MCPResponse.error("Failed to create order: " + e.getMessage());
        }
    }
    
    @MCPTool(
        name = "get_order_status",
        description = "Kiểm tra trạng thái đơn hàng"
    )
    public MCPResponse getOrderStatus(@MCPParam("orderId") String orderId) {
        try {
            var order = orderClient.getOrderById(orderId);
            return MCPResponse.success(Map.of(
                "orderId", orderId,
                "status", order.getStatus(),
                "estimatedDelivery", order.getEstimatedDelivery(),
                "trackingInfo", order.getTrackingInfo()
            ));
        } catch (Exception e) {
            return MCPResponse.error("Order not found: " + orderId);
        }
    }
    
    @MCPTool(
        name = "get_user_orders",
        description = "Lấy danh sách đơn hàng của user"
    )
    public MCPResponse getUserOrders(
        @MCPParam("userId") String userId,
        @MCPParam("status") Optional<String> status,
        @MCPParam("limit") Optional<Integer> limit
    ) {
        try {
            var orders = orderClient.getUserOrders(
                userId, 
                status.orElse(null), 
                limit.orElse(10)
            );
            return MCPResponse.success(orders);
        } catch (Exception e) {
            return MCPResponse.error("Failed to get orders");
        }
    }
}
```

### Phase 3: Integrate MCP với Chat Service (Week 2)

#### 3.1. Update GeminiAIService với MCP

```java
@Service
@RequiredArgsConstructor
public class GeminiAIService implements AIAssistantService {
    
    private final ChatClient chatClient;
    private final RAGService ragService;
    private final MCPClient mcpClient; // NEW
    
    @Override
    public String chat(String conversationId, String userId, String message) {
        // 1. Determine if need RAG or MCP or both
        var intent = analyzeIntent(message);
        
        // 2. Get RAG context if needed
        String ragContext = "";
        if (intent.needsRAG()) {
            var ragResults = ragService.search(
                RAGSearchRequest.builder()
                    .query(message)
                    .topK(3)
                    .build()
            );
            ragContext = ragResults.getContext();
        }
        
        // 3. Get available MCP tools
        var availableTools = mcpClient.listTools();
        
        // 4. Build prompt with RAG context and MCP tools
        String systemPrompt = buildSystemPrompt(ragContext, availableTools);
        
        // 5. Call Gemini with function calling
        var response = chatClient.prompt()
            .system(systemPrompt)
            .user(message)
            .functions(availableTools) // Enable function calling
            .call()
            .content();
        
        // 6. If Gemini wants to call a tool
        if (response.hasFunctionCall()) {
            var toolCall = response.getFunctionCall();
            var toolResult = mcpClient.executeTool(
                toolCall.getName(),
                toolCall.getArguments()
            );
            
            // 7. Send tool result back to Gemini
            response = chatClient.prompt()
                .system(systemPrompt)
                .user(message)
                .function(toolCall.getName(), toolResult)
                .call()
                .content();
        }
        
        return response.getText();
    }
    
    private String buildSystemPrompt(String ragContext, List<MCPTool> tools) {
        return String.format("""
            Bạn là SpringFood AI Assistant.
            
            RAG Context:
            %s
            
            Available Tools:
            %s
            
            Hướng dẫn:
            - Sử dụng RAG context để trả lời câu hỏi chung
            - Sử dụng tools để thực hiện actions (tìm sản phẩm, đặt hàng, etc.)
            - Luôn xác nhận với user trước khi thực hiện action quan trọng
            """, 
            ragContext,
            formatTools(tools)
        );
    }
}
```

#### 3.2. MCP Client Implementation

```java
@Service
@RequiredArgsConstructor
public class MCPClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${mcp.server.url}")
    private String mcpServerUrl;
    
    public List<MCPTool> listTools() {
        return restTemplate.getForObject(
            mcpServerUrl + "/tools",
            new ParameterizedTypeReference<List<MCPTool>>() {}
        );
    }
    
    public MCPResponse executeTool(String toolName, Map<String, Object> arguments) {
        var request = MCPRequest.builder()
            .tool(toolName)
            .arguments(arguments)
            .build();
            
        return restTemplate.postForObject(
            mcpServerUrl + "/execute",
            request,
            MCPResponse.class
        );
    }
}
```

### Phase 4: Testing & Examples (Week 2-3)

#### 4.1. Example Conversations

**Example 1: Tìm và đặt món**

```
User: "Tìm món pizza giá dưới 200k"

AI: [Calls search_products tool]
    → Tool returns: [Pizza Margherita 150k, Pizza Hawaii 180k]

AI: "Tôi tìm thấy 2 món pizza phù hợp:
     1. Pizza Margherita - 150,000đ
     2. Pizza Hawaii - 180,000đ
     Bạn muốn đặt món nào?"

User: "Đặt món 1"

AI: [Calls add_to_cart tool]
    → Tool returns: {success: true, cartTotal: 150000}

AI: "Đã thêm Pizza Margherita vào giỏ hàng. 
     Tổng: 150,000đ. Bạn muốn đặt hàng luôn không?"
```

**Example 2: Kiểm tra đơn hàng**

```
User: "Đơn hàng của tôi đến đâu rồi?"

AI: [Calls get_user_orders tool]
    → Tool returns: [{orderId: "ORD123", status: "DELIVERING"}]

AI: [Calls get_order_status tool with orderId="ORD123"]
    → Tool returns: {
        status: "DELIVERING",
        estimatedDelivery: "15:30",
        trackingInfo: "Shipper đang trên đường"
      }

AI: "Đơn hàng #ORD123 của bạn đang được giao.
     Dự kiến đến lúc 15:30.
     Shipper đang trên đường đến địa chỉ của bạn."
```

**Example 3: Kết hợp RAG + MCP**

```
User: "Chính sách đổi trả như thế nào? Và tôi muốn đổi đơn ORD456"

AI: [Searches RAG for return policy]
    → RAG returns: "Đổi trả trong 7 ngày..."

AI: [Calls get_order_status tool]
    → Tool returns: {orderId: "ORD456", status: "DELIVERED", deliveredAt: "2 days ago"}

AI: "Chính sách đổi trả của SpringFood:
     - Đổi trả trong 7 ngày kể từ khi nhận hàng
     - Sản phẩm còn nguyên vẹn, chưa sử dụng
     
     Đơn hàng #ORD456 của bạn được giao 2 ngày trước,
     vẫn trong thời gian đổi trả. Bạn muốn tôi tạo yêu cầu
     đổi trả không?"
```

## 🔐 Security Considerations

### 1. Authentication & Authorization

```java
@Configuration
public class MCPSecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/mcp/tools").permitAll()
                .requestMatchers("/mcp/execute").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt())
            .build();
    }
}
```

### 2. Tool Permissions

```java
@MCPTool(
    name = "cancel_order",
    description = "Hủy đơn hàng",
    requiredPermissions = {"order:cancel"}
)
public MCPResponse cancelOrder(
    @MCPParam("orderId") String orderId,
    @MCPParam("userId") String userId
) {
    // Verify user owns the order
    if (!orderService.isOrderOwner(orderId, userId)) {
        return MCPResponse.error("Unauthorized");
    }
    // ...
}
```

### 3. Rate Limiting

```java
@RateLimiter(name = "mcp-tools", fallbackMethod = "rateLimitFallback")
public MCPResponse executeTool(String toolName, Map<String, Object> args) {
    // ...
}
```

## 📊 Monitoring & Analytics

### 1. Tool Usage Metrics

```java
@Timed(value = "mcp.tool.execution", description = "MCP tool execution time")
@Counted(value = "mcp.tool.calls", description = "MCP tool call count")
public MCPResponse executeTool(String toolName, Map<String, Object> args) {
    // ...
}
```

### 2. Success/Failure Tracking

```sql
CREATE TABLE mcp_tool_logs (
    id BIGSERIAL PRIMARY KEY,
    tool_name VARCHAR(100),
    user_id VARCHAR(100),
    arguments JSONB,
    result JSONB,
    success BOOLEAN,
    execution_time_ms INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 💰 Cost Estimation

**Infrastructure:**
- MCP Server: 1 instance (512MB RAM) - $5/month
- Additional API calls: Minimal (internal network)

**Development Time:**
- Phase 1: 1 week
- Phase 2: 1-2 weeks
- Phase 3: 1 week
- Phase 4: 1 week

**Total: 4-5 weeks**

## 🎯 Benefits

1. **AI có thể thực hiện actions** - Không chỉ trả lời mà còn làm việc
2. **Real-time data** - Truy cập database và microservices trực tiếp
3. **Tự động hóa** - User chỉ cần nói, AI làm hết
4. **Trải nghiệm tốt hơn** - Giảm số bước user phải thực hiện
5. **Scalable** - Dễ thêm tools mới cho các service khác

## 📝 Next Steps

1. ✅ Review proposal này
2. ⏳ Setup MCP Server module
3. ⏳ Implement Product Tools (pilot)
4. ⏳ Test với Gemini function calling
5. ⏳ Expand to other services
6. ⏳ Production deployment

---

**Câu hỏi cần trả lời:**
1. Bạn muốn bắt đầu với service nào trước? (Đề xuất: Product Service)
2. Có muốn dùng gRPC hay REST cho internal communication?
3. Có cần implement permission system phức tạp không?
4. Timeline mong muốn?
