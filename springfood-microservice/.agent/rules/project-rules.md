# Project Rules - SpringFood Microservice

## 📋 Project Overview
This is a Spring Boot Microservices project for a food e-commerce platform.

## 🏗️ Architecture
- **Framework**: Spring Boot 3.5.6, Spring Cloud 2025.0.0
- **Java Version**: 17
- **Database**: PostgreSQL (Database per Service pattern)
- **Cache**: Redis
- **Messaging**: Apache Kafka
- **Storage**: MinIO
- **Service Discovery**: Eureka

## 🧩 Services
| Service | Port | Description |
|---------|------|-------------|
| eureka-server | 8761 | Service Discovery |
| api-gateway | 8080 | API Gateway |
| identity-service | 8081 | Authentication & User Management |
| product-service | - | Product Management |
| order-service | - | Order Management |
| cart-service | - | Shopping Cart |
| payment-service | - | Payment Processing |
| shop-service | - | Shop Management |
| notification | - | Notifications |
| statistical-report | - | Reports |

## 📦 Common Modules
- `common` - Shared DTOs, Enums, Exceptions, Utilities
- `common-security` - Shared Security Configuration
- `grpc` - gRPC Protobuf definitions
- `client` - OpenFeign clients
- `minio` - MinIO client library

## 💡 Coding Guidelines

### Package Structure
```
service-name/
├── config/        # Spring Configuration
├── controller/    # REST Controllers
├── service/       # Business Logic
├── repository/    # JPA Repositories
├── model/domain/  # Entities
├── dto/           # Data Transfer Objects
├── mapper/        # MapStruct Mappers
├── exception/     # Custom Exceptions
└── kafka/         # Kafka Producers/Consumers
```

### Naming Conventions
- Entities: PascalCase (e.g., `Product`, `OrderItem`)
- DTOs: {Entity}Request, {Entity}Response
- Services: {Entity}Service
- Controllers: {Entity}Controller
- Repositories: {Entity}Repository

### Database Conventions
- Table names: snake_case
- Primary key: `{table_name}_id` (UUID)
- Foreign keys: Reference with `_id` suffix
- Timestamps: `created_at`, `updated_at`

## 🔐 Security
- JWT-based authentication
- RBAC (Role-Based Access Control)
- Roles: ADMIN, CUSTOMER, SHOP_OWNER, STAFF

## 🐳 Docker Commands
```bash
# Start infrastructure
docker-compose up -d

# Stop all
docker-compose down

# Clean restart
docker-compose down -v && docker-compose up -d
```

## 🚀 Run Order
1. Docker services (PostgreSQL, Redis, Kafka, MinIO)
2. Eureka Server
3. API Gateway
4. Other services
