# Microservice Client Library

A flexible, protocol-agnostic client library for microservices communication supporting both REST (Feign) and gRPC protocols with zero configuration required.

## Features

- **Protocol Agnostic**: Seamlessly switch between REST and gRPC protocols through configuration
- **Auto-Configuration**: Zero configuration required - just add as dependency
- **Resilience Patterns**: Built-in support for Circuit Breaker, Retry, Bulkhead, and Rate Limiting
- **User Context Propagation**: Automatic propagation of user context (authentication, tenant, etc.)
- **Service Discovery**: Integration with Consul, Eureka, and Kubernetes
- **Observability**: Metrics, tracing, and logging out of the box
- **Type Safety**: Strongly typed client interfaces with compile-time safety
- **Dynamic Client Creation**: Factory pattern for runtime client creation
- **Automatic Client Injection**: Use @InjectClient annotation for easy client injection

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>com.viettel.dvs</groupId>
    <artifactId>microservice-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Configure Services

```yaml
dvs:
  client:
    enabled: true
    default-protocol: rest  # or grpc
    
    services:
      party-member-service:
        protocol: rest
        url: http://localhost:8081
        base-path: /api
        
      author-service:
        protocol: grpc
        host: localhost
        port: 9090
```

### 3. Use Clients

```java
@Service
public class MyService {
    
    @Autowired
    private PartyMemberClient partyMemberClient;
    
    // Or use @InjectClient for automatic injection
    @InjectClient
    private AuthorClient authorClient;
    
    @InjectClient(protocol = "grpc")
    private PaymentClient paymentClient;
    
    public void doSomething() {
        // REST call
        ClientResponse<PartyMemberDto> response = partyMemberClient.getPartyMember("123");
        
        if (response.isSuccess()) {
            PartyMemberDto member = response.getBody();
            // Process member
        }
        
        // Create with request wrapper
        ClientRequest<CreatePartyMemberRequest> request = ClientRequest.of(
            new CreatePartyMemberRequest("John Doe", "john@example.com", "123456789", "IT", "Developer")
        );
        
        ClientResponse<PartyMemberDto> created = partyMemberClient.createPartyMember(request);
    }
}
```

## Creating a New Client

### 1. Define Client Interface

```java
@ServiceClient(value = "order-service", path = "/api/orders")
public interface OrderClient extends BaseClient {
    
    @ClientMethod(
        httpMethod = "GET",
        path = "/{id}",
        grpcMethod = "GetOrder",
        idempotent = true
    )
    ClientResponse<OrderDto> getOrder(String id);
    
    @ClientMethod(
        httpMethod = "POST",
        path = "",
        grpcMethod = "CreateOrder"
    )
    ClientResponse<OrderDto> createOrder(ClientRequest<CreateOrderRequest> request);
    
    // DTOs
    record OrderDto(String id, String customerId, BigDecimal total, String status) {}
    record CreateOrderRequest(String customerId, List<OrderItem> items) {}
    record OrderItem(String productId, int quantity, BigDecimal price) {}
}
```

### Circuit Breaker Configuration

```yaml
microservice:
  client:
    circuit-breaker:
      enabled: true
      failure-rate-threshold: 50
      slow-call-rate-threshold: 100
      slow-call-duration-threshold: 60000
      sliding-window-size: 100
      minimum-number-of-calls: 20
```

### Metrics Configuration

```yaml
microservice:
  client:
    metrics:
      enabled: true
      prefix: microservice.client
      include-method-name: true
      include-service-name: true
      include-protocol: true
```

### Tracing Configuration

```yaml
microservice:
  client:
    tracing:
      enabled: true
      sampling-rate: 1.0
      propagate-headers: true
      service-name: microservice-client
```

## Service Interfaces

The module provides three example service interfaces:

### UserServiceClient

```java
public interface UserServiceClient {
    User createUser(User user);
    Optional<User> getUserById(String userId);
    User updateUser(String userId, User user);
    void deleteUser(String userId);
    List<User> listUsers(int page, int size);
    List<User> searchUsersByEmail(String email);
    // Plus async versions of all methods
}
```

### OrderServiceClient

```java
public interface OrderServiceClient {
    Order createOrder(Order order);
    Optional<Order> getOrderById(String orderId);
    Order updateOrder(String orderId, Order order);
    Order cancelOrder(String orderId);
    List<Order> listOrdersByUserId(String userId, int page, int size);
    BigDecimal calculateOrderTotal(String orderId);
    // Plus async versions of all methods
}
```

### ProductServiceClient

```java
public interface ProductServiceClient {
    Product createProduct(Product product);
    Optional<Product> getProductById(String productId);
    Product updateProduct(String productId, Product product);
    void deleteProduct(String productId);
    List<Product> searchProducts(String searchTerm, int page, int size);
    int updateInventory(String productId, int quantity);
    // Plus async versions of all methods
}
```

## Protocol Switching

### Programmatic Protocol Selection

```java
// Use specific protocol
UserServiceClient grpcClient = clientFactory.createUserServiceClient(Protocol.GRPC);
UserServiceClient feignClient = clientFactory.createUserServiceClient(Protocol.FEIGN);
```

### Configuration-based Protocol Selection

```yaml
# Set default protocol
microservice.client.protocol: FEIGN

# Override for specific service
microservice.client.services.user-service.protocol: GRPC
```

## Error Handling

The module provides custom exceptions for better error handling:

```java
try {
    User user = userClient.getUserById("123").orElseThrow();
} catch (ServiceUnavailableException e) {
    // Handle service unavailable
} catch (MicroserviceClientException e) {
    // Handle general client errors
}
```

## Metrics and Monitoring

Metrics are automatically collected and exposed via Micrometer:

- `microservice.client.calls.duration` - Call duration histogram
- `microservice.client.calls.success` - Successful call counter
- `microservice.client.calls.failure` - Failed call counter
- `microservice.client.errors` - Error counter by type
- `microservice.client.circuit_breaker.state_changes` - Circuit breaker state changes

Access metrics at `/actuator/metrics` or `/actuator/prometheus`.

## Advanced Usage

### Custom Headers

```yaml
microservice:
  client:
    services:
      user-service:
        headers:
          Authorization: Bearer token
          X-Request-ID: ${random.uuid}
```

### Load Balancing

```yaml
microservice:
  client:
    services:
      user-service:
        url: user-service  # Service name for discovery
        load-balancing-enabled: true
```

### Circuit Breaker per Service

```java
@Autowired
private Map<String, CircuitBreaker> serviceCircuitBreakers;

CircuitBreaker userServiceCB = serviceCircuitBreakers.get("user-service");
```

## Migration Guide

### From REST to gRPC

1. Change protocol in configuration:
   ```yaml
   microservice.client.services.user-service.protocol: GRPC
   ```

2. Update service URL to gRPC endpoint:
   ```yaml
   microservice.client.services.user-service.url: localhost:9090
   ```

### From gRPC to REST

1. Change protocol in configuration:
   ```yaml
   microservice.client.services.user-service.protocol: FEIGN
   ```

2. Update service URL to HTTP endpoint:
   ```yaml
   microservice.client.services.user-service.url: http://localhost:8080
   ```

## Performance Considerations

- **gRPC**: Better performance for high-throughput scenarios, binary protocol, HTTP/2
- **Feign**: Better for REST APIs, easier debugging, wider ecosystem support

### Performance Tuning

```yaml
# gRPC tuning
grpc:
  client:
    GLOBAL:
      negotiation-type: plaintext
      enable-keep-alive: true
      keep-alive-time: 30s

# Feign tuning
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 30000
  okhttp:
    enabled: true
```

## Troubleshooting

### Enable Debug Logging

```yaml
logging:
  level:
    com.example.client: DEBUG
    io.grpc: DEBUG
    feign: DEBUG
```

### Common Issues

1. **Service Unavailable**: Check service URL and network connectivity
2. **Protocol Mismatch**: Ensure client and server use the same protocol
3. **Timeout Errors**: Increase timeout values in configuration
4. **Circuit Breaker Open**: Check failure rate and adjust thresholds

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
