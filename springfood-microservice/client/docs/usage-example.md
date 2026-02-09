# PartyMemberClient Usage Example

## Example Service Implementation

```java
package com.example.myservice.service;

import api.com.theblood.springfood.client.ClientResponse;
import autoconf.com.theblood.springfood.client.ClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MyBusinessService {

    private final PartyMemberClient partyMemberClient;

    // Option 1: Direct injection (if using component scanning)
    @Autowired
    public MyBusinessService(PartyMemberClient partyMemberClient) {
        this.partyMemberClient = partyMemberClient;
    }

    // Option 2: Using ClientFactory
    @Autowired
    private ClientFactory clientFactory;

    public PartyMemberDto getPartyMemberById(String id) {
        try {
            // Call the remote service
            ClientResponse<PartyMemberDto> response = partyMemberClient.getPartyMember(id);

            // Check if successful
            if (response.isSuccess()) {
                return response.getData();
            } else {
                // Handle error
                throw new RuntimeException("Failed to get party member: " + response.getError());
            }
        } catch (Exception e) {
            // Handle exception (circuit breaker might open, retry might happen automatically)
            throw new RuntimeException("Error calling party member service", e);
        }
    }

    // Using ClientFactory to get client dynamically
    public PartyMemberDto getPartyMemberUsingFactory(String id) {
        // Get client from factory (useful for dynamic protocol switching)
        PartyMemberClient client = clientFactory.getClient(PartyMemberClient.class);

        ClientResponse<PartyMemberDto> response = client.getPartyMember(id);

        if (response.isSuccess()) {
            return response.getData();
        } else {
            throw new RuntimeException("Failed to get party member: " + response.getError());
        }
    }
}
```

## REST Controller Example

```java
package com.example.myservice.controller;

import com.example.myservice.service.MyBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/my-service")
public class MyServiceController {

    @Autowired
    private MyBusinessService businessService;

    @GetMapping("/party-member/{id}")
    public PartyMemberDto getPartyMember(@PathVariable String id) {
        return businessService.getPartyMemberById(id);
    }
}
```

## Configuration Class (Optional)

If you need custom configuration:

```java
package com.example.myservice.config;

import api.com.theblood.springfood.client.BaseClient.Protocol;
import autoconf.com.theblood.springfood.client.ClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig {

    @Bean
    public PartyMemberClient partyMemberClient(ClientFactory clientFactory) {
        // You can specify the protocol explicitly
        return clientFactory.getClient(PartyMemberClient.class, Protocol.REST);

        // Or use the default protocol from configuration
        // return clientFactory.getClient(PartyMemberClient.class);
    }
}
```

## Complete Application Properties Example

```yaml
# application.yml
spring:
  application:
    name: my-service

server:
  port: 8080

dvs:
  client:
    enabled: true
    default-protocol: rest
    defaults:
      connect-timeout: 5s
      read-timeout: 10s
      resilience:
        circuit-breaker:
          enabled: true
          failure-rate-threshold: 50
          wait-duration-in-open-state: 5s
          sliding-window-size: 10
        retry:
          enabled: true
          max-attempts: 3
          wait-duration: 1s
          exponential-backoff: true
    services:
      party-member-service:
        enabled: true
        protocol: rest  # or grpc
        host: party-member-service  # Use service name if using service discovery
        port: 8081
        # For Kubernetes/Docker environments
        # host: party-member-service.default.svc.cluster.local

        # Override default resilience settings if needed
        resilience:
          circuit-breaker:
            failure-rate-threshold: 60
          retry:
            max-attempts: 5
```

## Using with Spring Cloud Service Discovery

If using Eureka, Consul, or Kubernetes service discovery:

```yaml
dvs:
  client:
    services:
      party-member-service:
        # Just use the service name, discovery will resolve the actual URL
        host: party-member-service
        # No port needed if using service discovery
```

## Switching Between REST and gRPC

### Option 1: Change Configuration

```yaml
dvs:
  client:
    services:
      party-member-service:
        protocol: grpc  # Change from rest to grpc
```

### Option 2: Use Environment Variables

```yaml
dvs:
  client:
    services:
      party-member-service:
        protocol: ${PARTY_MEMBER_PROTOCOL:rest}
```

### Option 3: Programmatic Switch

```java

@Service
public class MyService {
    @Autowired
    private ClientFactory clientFactory;

    public void callWithRest() {
        PartyMemberClient restClient = clientFactory.getClient(
                PartyMemberClient.class,
                Protocol.REST
        );
        // Use REST client
    }

    public void callWithGrpc() {
        PartyMemberClient grpcClient = clientFactory.getClient(
                PartyMemberClient.class,
                Protocol.GRPC
        );
        // Use gRPC client
    }
}
```

## Error Handling

```java

@Service
public class MyBusinessService {

    @Autowired
    private PartyMemberClient partyMemberClient;

    public PartyMemberDto getPartyMemberWithErrorHandling(String id) {
        try {
            ClientResponse<PartyMemberDto> response = partyMemberClient.getPartyMember(id);

            if (response.isSuccess()) {
                return response.getData();
            } else {
                // Check error type
                if (response.getError() != null) {
                    if (response.getError().contains("404")) {
                        throw new NotFoundException("Party member not found: " + id);
                    } else if (response.getError().contains("403")) {
                        throw new ForbiddenException("Access denied");
                    }
                }
                throw new ServiceException("Service error: " + response.getError());
            }
        } catch (Exception e) {
            // Circuit breaker might be open
            if (e.getMessage().contains("CircuitBreaker")) {
                throw new ServiceUnavailableException("Party member service is temporarily unavailable");
            }
            throw new ServiceException("Failed to get party member", e);
        }
    }
}
```

## Features Automatically Included

When you use PartyMemberClient, you automatically get:

1. **Circuit Breaker**: Protects against cascading failures
2. **Retry**: Automatic retry for transient failures (only for idempotent operations like GET)
3. **Timeout Handling**: Configurable timeouts
4. **User Context Propagation**: Authentication/authorization headers are automatically forwarded
5. **Metrics**: Request metrics are automatically collected
6. **Logging**: Request/response logging based on configuration
7. **Load Balancing**: If using service discovery

## Testing

```java

@SpringBootTest
class MyBusinessServiceTest {

    @MockBean
    private PartyMemberClient partyMemberClient;

    @Autowired
    private MyBusinessService businessService;

    @Test
    void testGetPartyMember() {
        // Given
        String id = "123";
        PartyMemberDto expected = new PartyMemberDto(
                id, "John Doe", "john@example.com",
                "123456789", "IT", "Developer",
                "ACTIVE", 1234567890L, 1234567890L
        );

        ClientResponse<PartyMemberDto> response = ClientResponse.<PartyMemberDto>builder()
                .success(true)
                .data(expected)
                .build();

        when(partyMemberClient.getPartyMember(id)).thenReturn(response);

        // When
        PartyMemberDto result = businessService.getPartyMemberById(id);

        // Then
        assertEquals(expected, result);
        verify(partyMemberClient).getPartyMember(id);
    }
}
```

## Summary

1. Add client module dependency
2. Configure the party-member-service in your application.yml
3. Inject PartyMemberClient using @Autowired or get it from ClientFactory
4. Call methods like `getPartyMember(id)` which returns `ClientResponse<PartyMemberDto>`
5. Handle the response and errors appropriately

The client module handles all the complexity of REST/gRPC communication, resilience patterns, and cross-cutting concerns
automatically.
