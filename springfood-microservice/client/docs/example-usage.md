# Example Usage of Microservice Client Module

This guide demonstrates how to use the microservice client module in a real application.

## Example Application Structure

```java
package com.example.myapp;

import com.example.client.annotation.EnableMicroserviceClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableMicroserviceClient(
    defaultProtocol = EnableMicroserviceClient.Protocol.GRPC,
    enableCircuitBreaker = true,
    enableMetrics = true,
    enableTracing = true
)
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

## Example Service Implementation

```java
package com.example.myapp.service;

import com.example.client.api.UserServiceClient;
import com.example.client.api.OrderServiceClient;
import com.example.client.api.ProductServiceClient;
import com.example.client.factory.ClientProtocolFactory;
import com.example.client.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class ECommerceService {
    
    @Autowired
    private ClientProtocolFactory clientFactory;
    
    @Autowired
    private Map<String, CircuitBreaker> serviceCircuitBreakers;
    
    /**
     * Example: Create a new user account
     */
    public UserServiceClient.User createUserAccount(String username, String email, 
                                                   String firstName, String lastName) {
        UserServiceClient userClient = clientFactory.createUserServiceClient();
        CircuitBreaker circuitBreaker = serviceCircuitBreakers.get("user-service");
        
        return circuitBreaker.executeSupplier(() -> {
            UserServiceClient.User newUser = new UserServiceClient.User(
                username, email, firstName, lastName
            );
            
            log.info("Creating user account for: {}", username);
            UserServiceClient.User createdUser = userClient.createUser(newUser);
            log.info("User created successfully with ID: {}", createdUser.getId());
            
            return createdUser;
        });
    }
    
    /**
     * Example: Place an order with multiple products
     */
    public CompletableFuture<OrderServiceClient.Order> placeOrderAsync(
            String userId, List<OrderItem> items) {
        
        OrderServiceClient orderClient = clientFactory.createOrderServiceClient();
        ProductServiceClient productClient = clientFactory.createProductServiceClient();
        
        // First, validate all products exist and have sufficient inventory
        CompletableFuture<List<ProductServiceClient.Product>> productValidation = 
            CompletableFuture.allOf(
                items.stream()
                    .map(item -> productClient.getProductByIdAsync(item.getProductId())
                        .thenApply(opt -> opt.orElseThrow(() -> 
                            new IllegalArgumentException("Product not found: " + item.getProductId()))))
                    .toArray(CompletableFuture[]::new)
            ).thenApply(v -> 
                items.stream()
                    .map(item -> productClient.getProductById(item.getProductId()).get())
                    .collect(Collectors.toList())
            );
        
        // Then create the order
        return productValidation.thenCompose(products -> {
            OrderServiceClient.Order order = new OrderServiceClient.Order();
            order.setUserId(userId);
            
            List<OrderServiceClient.OrderItem> orderItems = items.stream()
                .map(item -> {
                    ProductServiceClient.Product product = products.stream()
                        .filter(p -> p.getId().equals(item.getProductId()))
                        .findFirst()
                        .orElseThrow();
                    
                    return new OrderServiceClient.OrderItem(
                        item.getProductId(),
                        product.getName(),
                        item.getQuantity(),
                        product.getPrice()
                    );
                })
                .collect(Collectors.toList());
            
            order.setItems(orderItems);
            order.setShippingAddress(items.get(0).getShippingAddress());
            order.setBillingAddress(items.get(0).getBillingAddress());
            
            return orderClient.createOrderAsync(order);
        });
    }
    
    /**
     * Example: Search for products and get inventory
     */
    public List<ProductInfo> searchProductsWithInventory(String searchTerm, int page, int size) {
        ProductServiceClient productClient = clientFactory.createProductServiceClient();
        
        try {
            List<ProductServiceClient.Product> products = 
                productClient.searchProducts(searchTerm, page, size);
            
            return products.stream()
                .map(product -> new ProductInfo(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    product.getInventory(),
                    product.getInventory() > 0 ? "In Stock" : "Out of Stock"
                ))
                .collect(Collectors.toList());
        } catch (ServiceUnavailableException e) {
            log.error("Product service is unavailable", e);
            // Return cached results or empty list
            return List.of();
        }
    }
    
    /**
     * Example: Protocol switching at runtime
     */
    public void demonstrateProtocolSwitching() {
        // Use gRPC for high-performance operations
        UserServiceClient grpcClient = clientFactory.createUserServiceClient(
            MicroserviceClientProperties.Protocol.GRPC
        );
        
        // Use Feign for compatibility with REST APIs
        UserServiceClient feignClient = clientFactory.createUserServiceClient(
            MicroserviceClientProperties.Protocol.FEIGN
        );
        
        // Both clients implement the same interface
        List<UserServiceClient.User> grpcUsers = grpcClient.listUsers(0, 10);
        List<UserServiceClient.User> feignUsers = feignClient.listUsers(0, 10);
    }
    
    /**
     * Example: Batch operations with error handling
     */
    public Map<String, PriceUpdateResult> batchUpdatePrices(
            Map<String, BigDecimal> priceUpdates) {
        
        ProductServiceClient productClient = clientFactory.createProductServiceClient();
        CircuitBreaker circuitBreaker = serviceCircuitBreakers.get("product-service");
        
        return circuitBreaker.executeSupplier(() -> {
            try {
                Map<String, Boolean> results = productClient.batchUpdatePrices(priceUpdates);
                
                return results.entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new PriceUpdateResult(
                            entry.getValue(),
                            entry.getValue() ? "Success" : "Failed"
                        )
                    ));
            } catch (Exception e) {
                log.error("Batch price update failed", e);
                // Return failure for all items
                return priceUpdates.keySet().stream()
                    .collect(Collectors.toMap(
                        productId -> productId,
                        productId -> new PriceUpdateResult(false, e.getMessage())
                    ));
            }
        });
    }
    
    // Helper classes
    public static class OrderItem {
        private String productId;
        private int quantity;
        private String shippingAddress;
        private String billingAddress;
        // getters/setters
    }
    
    public static class ProductInfo {
        private String id;
        private String name;
        private BigDecimal price;
        private int inventory;
        private String status;
        
        public ProductInfo(String id, String name, BigDecimal price, 
                          int inventory, String status) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.inventory = inventory;
            this.status = status;
        }
        // getters/setters
    }
    
    public static class PriceUpdateResult {
        private boolean success;
        private String message;
        
        public PriceUpdateResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        // getters/setters
    }
}
```

## Example REST Controller

```java
package com.example.myapp.controller;

import com.example.myapp.service.ECommerceService;
import com.example.client.api.UserServiceClient;
import com.example.client.api.OrderServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1")
public class ECommerceController {
    
    @Autowired
    private ECommerceService ecommerceService;
    
    @PostMapping("/users")
    public ResponseEntity<UserServiceClient.User> createUser(@RequestBody CreateUserRequest request) {
        UserServiceClient.User user = ecommerceService.createUserAccount(
            request.getUsername(),
            request.getEmail(),
            request.getFirstName(),
            request.getLastName()
        );
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/orders")
    public CompletableFuture<ResponseEntity<OrderServiceClient.Order>> createOrder(
            @RequestBody CreateOrderRequest request) {
        
        return ecommerceService.placeOrderAsync(request.getUserId(), request.getItems())
            .thenApply(ResponseEntity::ok);
    }
    
    @GetMapping("/products/search")
    public ResponseEntity<List<ECommerceService.ProductInfo>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        List<ECommerceService.ProductInfo> products = 
            ecommerceService.searchProductsWithInventory(query, page, size);
        return ResponseEntity.ok(products);
    }
    
    // Request DTOs
    public static class CreateUserRequest {
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        // getters/setters
    }
    
    public static class CreateOrderRequest {
        private String userId;
        private List<ECommerceService.OrderItem> items;
        // getters/setters
    }
}
```

## Example Configuration for Different Environments

### Development Environment (application-dev.yml)

```yaml
microservice:
  client:
    protocol: GRPC
    services:
      user-service:
        url: localhost:9090
        protocol: GRPC
      order-service:
        url: localhost:9091
        protocol: GRPC
      product-service:
        url: localhost:9092
        protocol: GRPC
    circuit-breaker:
      enabled: false  # Disable for development
    metrics:
      enabled: true
    tracing:
      enabled: true
      sampling-rate: 1.0  # Sample all requests in dev

logging:
  level:
    com.example.client: DEBUG
```

### Production Environment (application-prod.yml)

```yaml
microservice:
  client:
    protocol: GRPC
    services:
      user-service:
        url: user-service.prod.svc.cluster.local:9090
        protocol: GRPC
        load-balancing-enabled: true
      order-service:
        url: https://api.orders.example.com
        protocol: FEIGN  # Use REST for external service
        headers:
          Authorization: ${ORDER_SERVICE_API_KEY}
      product-service:
        url: product-service.prod.svc.cluster.local:9092
        protocol: GRPC
        load-balancing-enabled: true
    circuit-breaker:
      enabled: true
      failure-rate-threshold: 50
      minimum-number-of-calls: 100
    metrics:
      enabled: true
    tracing:
      enabled: true
      sampling-rate: 0.1  # Sample 10% in production

logging:
  level:
    com.example.client: INFO
```

## Testing Example

```java
package com.example.myapp.service;

import com.example.client.api.UserServiceClient;
import com.example.client.factory.ClientProtocolFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ECommerceServiceTest {
    
    @Mock
    private ClientProtocolFactory clientFactory;
    
    @Mock
    private UserServiceClient userServiceClient;
    
    @InjectMocks
    private ECommerceService ecommerceService;
    
    @Test
    void shouldCreateUserSuccessfully() {
        // Given
        when(clientFactory.createUserServiceClient()).thenReturn(userServiceClient);
        
        UserServiceClient.User inputUser = new UserServiceClient.User(
            "john_doe", "john@example.com", "John", "Doe"
        );
        UserServiceClient.User createdUser = new UserServiceClient.User(
            "john_doe", "john@example.com", "John", "Doe"
        );
        createdUser.setId("user-123");
        
        when(userServiceClient.createUser(any())).thenReturn(createdUser);
        
        // When
        UserServiceClient.User result = ecommerceService.createUserAccount(
            "john_doe", "john@example.com", "John", "Doe"
        );
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("user-123");
        assertThat(result.getUsername()).isEqualTo("john_doe");
    }
}
```

This example demonstrates:
- Basic CRUD operations
- Async operations
- Error handling with circuit breakers
- Protocol switching
- Batch operations
- Integration with Spring Boot REST controllers
- Configuration for different environments
- Unit testing with mocks