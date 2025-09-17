# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

SpringFood is a Spring Boot-based food delivery application built with Java 17, using PostgreSQL as the database, JWT for authentication, and includes integrations with SendGrid for email notifications and Kafka for messaging.

## Common Development Commands

### Building and Running

```bash
# Run the application (Windows)
./mvnw.cmd spring-boot:run

# Run the application (Unix/Mac)
./mvnw spring-boot:run

# Build the project
./mvnw.cmd clean package

# Build without tests
./mvnw.cmd clean package -DskipTests

# Run with specific profile
./mvnw.cmd spring-boot:run -Dspring.profiles.active=dev
```

### Testing

```bash
# Run all tests
./mvnw.cmd test

# Run specific test class
./mvnw.cmd test -Dtest=SpringfoodApplicationTests

# Run tests with specific pattern
./mvnw.cmd test -Dtest="*ServiceTest"

# Run with test coverage (if configured)
./mvnw.cmd clean test jacoco:report
```

### Database Management

```bash
# Connect to PostgreSQL database
psql -h localhost -p 5432 -U postgres -d food_app

# For database migrations (when needed)
./mvnw.cmd flyway:migrate
```

## Architecture Overview

### Package Structure

The application follows a layered architecture pattern:

- **`controller`**: REST API endpoints organized by domain (AuthController, UserController, ProductController, CategoryController, OrderController)
- **`service`**: Business logic layer with interfaces and `Impl` implementations
- **`repository`**: JPA repositories for data access
- **`model`**: JPA entities representing database tables
- **`dto`**: Data Transfer Objects including request/response DTOs
- **`mapper`**: MapStruct mappers for entity-DTO conversions
- **`config`**: Configuration classes including SecurityConfig with JWT authentication
- **`exception`**: Global exception handling with GlobalHandleException
- **`common`**: Shared utilities, enums, and generators

### Security Architecture

The application implements JWT-based authentication with role-based access control:

- **PreFilter**: JWT validation filter that runs before Spring Security filters
- **SecurityConfig**: Defines authorization rules based on roles (ADMIN, CUSTOMER) and permissions
- **JwtService**: Handles JWT token generation, validation, and parsing
- **AuthService**: Manages authentication, registration, and password reset flows

Key security patterns:
- Stateless authentication using JWT tokens
- Separate tokens for access, refresh, and password reset
- Permission-based authorization for fine-grained access control
- CORS configuration for frontend integration

### Data Access Layer

The application uses Spring Data JPA with PostgreSQL:

- **Entities**: JPA entities with auditing enabled (@EnableJpaAuditing)
- **Repositories**: Extend JpaRepository for standard CRUD operations
- **Custom queries**: Using @Query annotations when needed
- **Database schema**: Managed through Hibernate with `ddl-auto: update`

### External Integrations

1. **SendGrid Email Service**:
   - Configured via environment variables (SENDGRID_API_KEY, SENDGRID_FROM_EMAIL)
   - Used for transactional emails (registration, password reset)

2. **Kafka Messaging**:
   - Spring Kafka integration for event-driven architecture
   - Potential use for order processing, notifications

3. **MongoDB BSON**:
   - ObjectId generation for unique identifiers

### API Documentation

The application uses SpringDoc OpenAPI (Swagger UI):
- Access Swagger UI at: http://localhost:8081/swagger-ui.html
- API docs available at: http://localhost:8081/v3/api-docs

## Environment Configuration

### Required Environment Variables

Create a `.env` file based on `.env.example`:
```
SENDGRID_API_KEY=your-sendgrid-api-key
SENDGRID_FROM_EMAIL=your-email@example.com
```

### Application Configuration

Key configuration in `application.yaml`:
- Server port: 8081
- Database: PostgreSQL on localhost:5432/food_app
- JWT token expiration times configured for access, refresh, and reset tokens
- Spring Security debug mode enabled (disable in production)
- DevTools with hot reload enabled for development

## Domain Model Overview

The application manages several key domains:

- **Users & Authentication**: User management with role-based access
- **Products & Categories**: Food items and their categorization
- **Orders**: Order processing and status management
- **Shops**: Restaurant/shop management
- **Payments & Wallets**: Payment processing
- **Addresses**: User delivery addresses
- **Feedback**: Customer reviews and ratings
- **Notifications**: User notifications system

## Development Workflow

1. **Feature Development**:
   - Create/modify entities in `model` package
   - Update repositories if custom queries needed
   - Implement service interfaces and implementations
   - Create DTOs for request/response
   - Add MapStruct mappers for conversions
   - Implement controller endpoints
   - Update security rules in SecurityConfig if needed

2. **API Testing**:
   - Use Swagger UI for quick API testing
   - Write integration tests for controllers
   - Write unit tests for services

3. **Database Changes**:
   - Entities automatically update schema with `ddl-auto: update`
   - For production, consider using migration tools like Flyway or Liquibase

## Key Technologies

- **Spring Boot 3.5.3**: Core framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: ORM and data access
- **PostgreSQL**: Primary database
- **JWT (jjwt 0.11.5)**: Token-based authentication
- **MapStruct 1.6.3**: DTO mapping
- **Lombok**: Boilerplate reduction
- **SpringDoc OpenAPI 2.6.0**: API documentation
- **SendGrid 4.9.3**: Email service
- **Spring Kafka**: Message broker integration
- **Thymeleaf**: Template engine for email templates
