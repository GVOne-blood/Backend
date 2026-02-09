---
description: How to build and run the SpringFood microservices project
---

# Build and Run SpringFood Microservices

## Prerequisites
- Docker Desktop running
- Java 17 installed
- Maven installed (or use ./mvnw)

## Steps

### 1. Start Docker Infrastructure
// turbo
```bash
cd f:\Document\TASC\Backend\springfood-microservice
docker-compose up -d
```

### 2. Wait for services to be healthy (30-60 seconds)
```bash
docker-compose ps
```

### 3. Build the entire project
```bash
cd f:\Document\TASC\Backend\springfood-microservice
.\mvnw clean install -DskipTests
```

### 4. Run Eureka Server (MUST be first)
```bash
cd f:\Document\TASC\Backend\springfood-microservice\eureka-server
.\mvnw spring-boot:run
```
Wait until you see "Started EurekaServerApplication"

### 5. Run API Gateway
```bash
cd f:\Document\TASC\Backend\springfood-microservice\api-gateway
.\mvnw spring-boot:run
```

### 6. Run Identity Service
```bash
cd f:\Document\TASC\Backend\springfood-microservice\identity-service
.\mvnw spring-boot:run
```

### 7. Run other services as needed
```bash
# Product Service
cd product-service && .\mvnw spring-boot:run

# Order Service
cd order-service && .\mvnw spring-boot:run

# Cart Service
cd cart-service && .\mvnw spring-boot:run

# Payment Service
cd payment-service && .\mvnw spring-boot:run

# Shop Service
cd shop-service && .\mvnw spring-boot:run
```

## Verification
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- MinIO Console: http://localhost:9001 (minioadmin/minioadmin)

## Troubleshooting
- If services fail to register with Eureka, restart them
- Check Docker logs: `docker-compose logs -f [service]`
- Verify database connections in application.yaml
