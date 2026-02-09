---
description: How to create a new microservice in the SpringFood project
---

# Create a New Microservice

## Prerequisites
- Existing SpringFood project structure understood
- Maven parent pom.xml configured

## Steps

### 1. Create service directory structure
```bash
mkdir new-service
cd new-service
```

### 2. Create pom.xml
Copy from existing service (e.g., cart-service) and modify:
- artifactId
- name
- dependencies specific to this service

### 3. Create source structure
```
new-service/
├── src/
│   ├── main/
│   │   ├── java/com/theblood/newservice/
│   │   │   ├── NewServiceApplication.java
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── model/
│   │   │   └── dto/
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
│       └── java/com/theblood/newservice/
└── pom.xml
```

### 4. Add to parent pom.xml
Add module in `springfood-microservice/pom.xml`:
```xml
<modules>
    ...
    <module>new-service</module>
</modules>
```

### 5. Configure application.yaml
```yaml
server:
  port: 808X  # Choose unique port

spring:
  application:
    name: new-service
  datasource:
    url: ${DEFAULT_DATABASE_URL}
    username: ${DEFAULT_DATABASE_USERNAME}
    password: ${DEFAULT_DATABASE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### 6. Add route in API Gateway
Edit `api-gateway/src/main/resources/application.yaml`:
```yaml
- id: new-service
  uri: lb://NEW-SERVICE
  predicates:
    - Path=/api/v1/new/**
  filters:
    - StripPrefix=2
```

### 7. Create database (if needed)
Add to `init-db.sql`:
```sql
CREATE DATABASE IF NOT EXISTS new_service;
```

### 8. Build and test
```bash
cd new-service
.\mvnw spring-boot:run
```

## Verification
- Check Eureka Dashboard for service registration
- Test API through Gateway: http://localhost:8080/api/v1/new/...
