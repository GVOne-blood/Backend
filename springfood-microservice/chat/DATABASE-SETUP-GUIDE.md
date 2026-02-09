# 🗄️ Hướng Dẫn Cấu Hình Database Schema cho Chat Service

## 📋 Tổng Quan

SpringFood sử dụng **PostgreSQL Schemas** để tách biệt data giữa các microservices trong cùng 1 database instance.

```
┌─────────────────────────────────────────────────────────────────┐
│                    DATABASE: springfood                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │   SCHEMA    │  │   SCHEMA    │  │   SCHEMA    │  ...         │
│  │   identity  │  │   product   │  │    chat     │              │
│  │             │  │             │  │             │              │
│  │ - users     │  │ - products  │  │ - convers.  │              │
│  │ - roles     │  │ - categories│  │ - messages  │              │
│  │ - ...       │  │ - ...       │  │ - ...       │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔧 Cấu Hình Đã Thực Hiện

### 1. docker-compose.yml
```yaml
postgres:
  environment:
    POSTGRES_DB: springfood  # Database chính
```

### 2. init-db.sql (Tạo schemas)
```sql
CREATE SCHEMA IF NOT EXISTS chat;
-- ... other schemas
```

### 3. application-dev.yml (Chat Service)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/springfood?currentSchema=chat
    username: postgres
    password: 123456
  jpa:
    properties:
      hibernate:
        default_schema: chat
  liquibase:
    default-schema: chat
    liquibase-schema: chat
```

---

## 🚀 Các Bước Để Tables Được Tạo Đúng Schema

### Bước 1: Khởi động Database
```bash
# Từ thư mục gốc project
docker-compose up -d postgres
```

### Bước 2: Kiểm tra schemas đã được tạo
```bash
docker exec -it springfood-postgres psql -U postgres -d springfood -c "\dn"
```

Output mong đợi:
```
      List of schemas
    Name     |  Owner   
-------------+----------
 action_log  | postgres
 cart        | postgres
 chat        | postgres     <-- Schema cho chat service
 identity    | postgres
 media       | postgres
 notification| postgres
 orders      | postgres
 payment     | postgres
 product     | postgres
 public      | postgres
 shop        | postgres
 statistical | postgres
```

### Bước 3: Chạy Chat Service
```bash
cd chat
./mvnw spring-boot:run
```

### Bước 4: Xác nhận tables được tạo trong schema chat
```bash
docker exec -it springfood-postgres psql -U postgres -d springfood -c "\dt chat.*"
```

Output mong đợi:
```
               List of relations
 Schema |          Name           | Type  |  Owner   
--------+-------------------------+-------+----------
 chat   | conversation            | table | postgres
 chat   | conversation_participant| table | postgres
 chat   | conversation_settings   | table | postgres
 chat   | message                 | table | postgres
 chat   | message_attachment      | table | postgres
 chat   | message_reaction        | table | postgres
 chat   | message_read_receipt    | table | postgres
 chat   | ...                     | table | postgres
```

---

## 📝 Sử Dụng JDL File

### Option A: Dùng JHipster CLI (Khuyến nghị)
```bash
cd chat

# Import JDL để generate entities
jhipster jdl chat-service.jdl
```

JHipster sẽ tự động:
1. Generate entity classes từ JDL
2. Tạo Liquibase changelog files
3. Khi chạy app, Liquibase sẽ tạo tables trong schema `chat`

### Option B: Dùng Hibernate DDL Auto (Đã cấu hình)

Trong `application.yml`:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # Tự động tạo/update tables
```

> ⚠️ **Lưu ý**: `ddl-auto: update` chỉ nên dùng cho development. Production nên dùng `validate` và quản lý schema bằng Liquibase.

---

## 🔍 Troubleshooting

### Problem 1: Tables được tạo trong schema `public` thay vì `chat`

**Nguyên nhân**: Thiếu cấu hình `default_schema`

**Giải pháp**: Kiểm tra file `application-dev.yml`:
```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_schema: chat  # Phải có dòng này!
```

### Problem 2: Liquibase changelog tables ở sai schema

**Giải pháp**: Thêm cấu hình:
```yaml
spring:
  liquibase:
    default-schema: chat
    liquibase-schema: chat  # Changelog tables cũng ở schema chat
```

### Problem 3: Schema không tồn tại

**Giải pháp**: Chạy lệnh SQL thủ công:
```bash
docker exec -it springfood-postgres psql -U postgres -d springfood

# Trong psql console:
CREATE SCHEMA IF NOT EXISTS chat;
\q
```

Hoặc restart Docker với volume mới:
```bash
docker-compose down -v
docker-compose up -d
```

---

## 📊 Kiểm Tra Chi Tiết Tables

```bash
# Xem tất cả tables trong schema chat
docker exec -it springfood-postgres psql -U postgres -d springfood -c "
SELECT table_name, table_type 
FROM information_schema.tables 
WHERE table_schema = 'chat' 
ORDER BY table_name;
"

# Xem cấu trúc 1 table cụ thể
docker exec -it springfood-postgres psql -U postgres -d springfood -c "\d chat.conversation"
```

---

## ⚡ Quick Commands

```bash
# Start all infrastructure
docker-compose up -d

# Check if postgres is ready
docker exec -it springfood-postgres pg_isready -U postgres

# List all schemas
docker exec -it springfood-postgres psql -U postgres -d springfood -c "\dn"

# List tables in chat schema
docker exec -it springfood-postgres psql -U postgres -d springfood -c "\dt chat.*"

# Connect to psql interactively
docker exec -it springfood-postgres psql -U postgres -d springfood

# Run chat service
cd chat && ./mvnw spring-boot:run
```
