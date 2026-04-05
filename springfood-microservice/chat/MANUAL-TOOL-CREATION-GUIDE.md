# Hướng dẫn tạo Tools thủ công cho Spring AI

## Cách 1: Sử dụng @Tool annotation (Đơn giản nhất)

### Bước 1: Tạo class chứa tools

```java
package com.theblood.springfood.chat.mcp.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component  // QUAN TRỌNG: Phải có @Component để Spring scan
public class ProductTools {

    @Tool(description = "Search for products by name or keywords. Returns a list of matching products.")
    public String searchProducts(
        @ToolParam(description = "Search query (product name or keywords)") String query,
        @ToolParam(description = "Page number (default: 0)", required = false) Integer page,
        @ToolParam(description = "Page size (default: 10)", required = false) Integer size
    ) {
        // Mock implementation - bạn sẽ thay bằng real API call
        return String.format("""
            {
                "products": [
                    {"id": 1, "name": "iPhone 15 Pro", "price": 999.99},
                    {"id": 2, "name": "iPhone 15", "price": 799.99}
                ],
                "total": 2,
                "query": "%s"
            }
            """, query);
    }

    @Tool(description = "Get detailed information about a specific product by ID")
    public String getProductDetail(
        @ToolParam(description = "Product ID") Long productId
    ) {
        return String.format("""
            {
                "id": %d,
                "name": "iPhone 15 Pro",
                "price": 999.99,
                "description": "Latest iPhone with A17 Pro chip",
                "stock": 50
            }
            """, productId);
    }
}
```

### Bước 2: Sử dụng tools trong ChatClient

```java
package com.theblood.springfood.chat.web.rest;

import com.theblood.springfood.chat.mcp.tools.ProductTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final ChatClient chatClient;
    private final ProductTools productTools;

    public AIController(ChatClient.Builder chatClientBuilder, ProductTools productTools) {
        this.chatClient = chatClientBuilder.build();
        this.productTools = productTools;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody String message) {
        return chatClient.prompt()
            .user(message)
            .tools(productTools)  // Pass tool object
            .call()
            .content();
    }
}
```

### Lưu ý quan trọng:

1. **@Component**: Class phải có annotation này để Spring scan
2. **@Tool**: Đánh dấu method là tool, description rất quan trọng để AI hiểu khi nào gọi
3. **@ToolParam**: Mô tả parameter, giúp AI biết cần truyền gì
4. **Return type**: Nên return String (JSON format) để AI dễ parse

---

## Cách 2: Sử dụng Function Bean (Linh hoạt hơn)

### Bước 1: Tạo Request/Response DTOs

```java
package com.theblood.springfood.chat.mcp.dto;

public record ProductSearchRequest(
    String query,
    Integer page,
    Integer size
) {}

public record ProductSearchResponse(
    List<Product> products,
    int total,
    String query
) {
    public record Product(Long id, String name, Double price) {}
}
```

### Bước 2: Tạo Function Bean

```java
package com.theblood.springfood.chat.mcp.config;

import com.theblood.springfood.chat.mcp.dto.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.function.Function;

@Configuration
public class ProductToolsConfig {

    @Bean
    @Description("Search for products by name or keywords. Returns a list of matching products.")
    public Function<ProductSearchRequest, ProductSearchResponse> searchProducts() {
        return request -> {
            // Mock implementation
            var products = List.of(
                new ProductSearchResponse.Product(1L, "iPhone 15 Pro", 999.99),
                new ProductSearchResponse.Product(2L, "iPhone 15", 799.99)
            );
            return new ProductSearchResponse(products, products.size(), request.query());
        };
    }

    @Bean
    @Description("Get detailed information about a specific product by ID")
    public Function<Long, String> getProductDetail() {
        return productId -> String.format("""
            {
                "id": %d,
                "name": "iPhone 15 Pro",
                "price": 999.99,
                "description": "Latest iPhone with A17 Pro chip"
            }
            """, productId);
    }
}
```

### Bước 3: Sử dụng với ChatClient

```java
@PostMapping("/chat")
public String chat(@RequestBody String message) {
    return chatClient.prompt()
        .user(message)
        // Spring AI tự động discover Function beans, không cần pass
        .call()
        .content();
}
```

### Lưu ý:

1. **@Description**: Bắt buộc để AI hiểu tool làm gì
2. **Bean name**: Tên bean sẽ là tên tool (searchProducts, getProductDetail)
3. **Auto-discovery**: Spring AI tự động tìm tất cả Function beans
4. **Type safety**: Request/Response được type-safe

---

## Cách 3: Programmatic (Low-level, kiểm soát tối đa)

### Bước 1: Tạo ToolCallback thủ công

```java
package com.theblood.springfood.chat.mcp.tools;

import org.springframework.ai.tool.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class ManualProductTools {

    public String searchProducts(String query, Integer page, Integer size) {
        return String.format("""
            {
                "products": [
                    {"id": 1, "name": "iPhone 15 Pro", "price": 999.99}
                ],
                "query": "%s"
            }
            """, query);
    }

    public ToolCallback createSearchProductsTool() throws NoSuchMethodException {
        Method method = ManualProductTools.class.getMethod(
            "searchProducts", 
            String.class, 
            Integer.class, 
            Integer.class
        );

        ToolDefinition toolDefinition = ToolDefinition.builder()
            .name("searchProducts")
            .description("Search for products by name or keywords")
            .inputSchema("""
                {
                    "type": "object",
                    "properties": {
                        "query": {
                            "type": "string",
                            "description": "Search query"
                        },
                        "page": {
                            "type": "integer",
                            "description": "Page number"
                        },
                        "size": {
                            "type": "integer",
                            "description": "Page size"
                        }
                    },
                    "required": ["query"]
                }
                """)
            .build();

        return MethodToolCallback.builder()
            .toolDefinition(toolDefinition)
            .toolMethod(method)
            .toolObject(this)
            .build();
    }
}
```

### Bước 2: Sử dụng

```java
@PostMapping("/chat")
public String chat(@RequestBody String message) throws Exception {
    ManualProductTools tools = new ManualProductTools();
    ToolCallback searchTool = tools.createSearchProductsTool();

    return chatClient.prompt()
        .user(message)
        .toolCallbacks(searchTool)
        .call()
        .content();
}
```

---

## So sánh 3 cách

| Tiêu chí | @Tool | Function Bean | Programmatic |
|----------|-------|---------------|--------------|
| Độ đơn giản | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ |
| Type safety | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| Linh hoạt | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Auto-discovery | ✅ | ✅ | ❌ |
| Boilerplate code | Ít | Trung bình | Nhiều |

**Khuyến nghị**: Dùng **@Tool** cho đơn giản, **Function Bean** cho type-safe

---

## Ví dụ thực tế: Tích hợp với Client Module

### Bước 1: Tạo Feign Client interface

```java
package com.theblood.springfood.chat.mcp.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service")
public interface ProductClient {
    
    @GetMapping("/api/products/search")
    ProductSearchResponse searchProducts(
        @RequestParam String query,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    );

    @GetMapping("/api/products/{id}")
    ProductDetailResponse getProduct(@PathVariable Long id);
}
```

### Bước 2: Tạo Tool với real API call

```java
package com.theblood.springfood.chat.mcp.tools;

import com.theblood.springfood.chat.mcp.client.ProductClient;
import com.theblood.springfood.client.autoconf.DefaultClientFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RealProductTools {

    private final DefaultClientFactory clientFactory;

    @Tool(description = "Search for products by name or keywords")
    public String searchProducts(
        @ToolParam(description = "Search query") String query,
        @ToolParam(description = "Page number", required = false) Integer page,
        @ToolParam(description = "Page size", required = false) Integer size
    ) {
        try {
            // Create Feign client
            ProductClient client = clientFactory.createClient(
                ProductClient.class,
                "product-service"
            );

            // Call real API
            var response = client.searchProducts(
                query,
                page != null ? page : 0,
                size != null ? size : 10
            );

            // Convert to JSON string
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return String.format("""
                {
                    "error": "Failed to search products: %s"
                }
                """, e.getMessage());
        }
    }
}
```

---

## Testing Tools

### Test 1: Unit Test

```java
package com.theblood.springfood.chat.mcp.tools;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ProductToolsTest {

    @Test
    void testSearchProducts() {
        ProductTools tools = new ProductTools();
        String result = tools.searchProducts("iPhone", 0, 10);
        
        assertThat(result).contains("iPhone");
        assertThat(result).contains("products");
    }
}
```

### Test 2: Integration Test với ChatClient

```java
@SpringBootTest
class ToolIntegrationTest {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private ProductTools productTools;

    @Test
    void testToolCalling() {
        ChatClient chatClient = chatClientBuilder.build();

        String response = chatClient.prompt()
            .user("Tìm sản phẩm iPhone cho tôi")
            .tools(productTools)
            .call()
            .content();

        assertThat(response).containsIgnoringCase("iPhone");
    }
}
```

### Test 3: Manual Test với curl

```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "message": "Tìm sản phẩm iPhone cho tôi"
  }'
```

---

## Troubleshooting

### Lỗi: Tool không được gọi

**Nguyên nhân**: Description không rõ ràng

**Giải pháp**: Viết description chi tiết hơn
```java
@Tool(description = "Search for products in the catalog by product name, category, or keywords. " +
                    "Returns a paginated list of matching products with their details.")
```

### Lỗi: Parameter không đúng

**Nguyên nhân**: @ToolParam description không rõ

**Giải pháp**: Thêm format example
```java
@ToolParam(description = "Product ID (numeric, e.g., 123)")
```

### Lỗi: Tool return null

**Nguyên nhân**: Exception trong tool không được handle

**Giải pháp**: Wrap trong try-catch
```java
@Tool(description = "...")
public String myTool(String param) {
    try {
        // Your logic
        return result;
    } catch (Exception e) {
        return String.format("{\"error\": \"%s\"}", e.getMessage());
    }
}
```

---

## Best Practices

1. **Description rõ ràng**: AI dựa vào description để quyết định gọi tool
2. **Return JSON**: Dễ parse và structured
3. **Handle errors**: Luôn return something, không throw exception
4. **Keep it simple**: Mỗi tool làm 1 việc cụ thể
5. **Use @ToolParam**: Giúp AI hiểu parameter
6. **Test thoroughly**: Test cả success và error cases

---

## Next Steps

1. Tạo tool đầu tiên với @Tool annotation
2. Test với mock data
3. Tích hợp với client module
4. Add error handling
5. Add logging
6. Deploy và monitor
