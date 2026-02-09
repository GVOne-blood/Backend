# Docker Setup Guide - SpringFood Microservices

## 📦 Services Included

- **PostgreSQL** (port 5432) - Database cho các services
- **Redis** (port 6379) - Cache và session storage
- **MinIO** (port 9000, 9001) - Object storage cho images
- **Kafka** (port 9092) - Message broker
- **Zookeeper** (port 2181) - Kafka coordination

## 🚀 Quick Start

### 1. Start All Services

```bash
docker-compose up -d
```

### 2. Check Status

```bash
docker-compose ps
```

### 3. View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f minio
docker-compose logs -f postgres
```

### 4. Stop All Services

```bash
docker-compose down
```

### 5. Stop and Remove Volumes (Clean slate)

```bash
docker-compose down -v
```

## 🔧 Service Details

### MinIO Object Storage

- **API Endpoint:** http://localhost:9000
- **Console UI:** http://localhost:9001
- **Username:** minioadmin
- **Password:** minioadmin
- **Bucket:** images (auto-created)

**Access Console:**

1. Mở browser: http://localhost:9001
2. Login với minioadmin/minioadmin
3. Bucket "images" đã được tạo sẵn với public access

### PostgreSQL

- **Host:** localhost
- **Port:** 5432
- **Username:** postgres
- **Password:** 123456
- **Databases:**
    - identity_service
    - product_service
    - cart_service
    - order_service
    - payment_service
    - shop_service

### Redis

- **Host:** localhost
- **Port:** 6379
- **Password:** 123456

**Test connection:**

```bash
docker exec -it springfood-redis redis-cli -a 123456
> ping
PONG
```

### Kafka

- **Bootstrap Server:** localhost:9092
- **Zookeeper:** localhost:2181

**Create topic:**

```bash
docker exec -it springfood-kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic my-topic \
  --partitions 1 \
  --replication-factor 1
```

## 🔍 Troubleshooting

### MinIO không start

```bash
# Check logs
docker-compose logs minio

# Restart MinIO
docker-compose restart minio
```

### PostgreSQL connection refused

```bash
# Wait for health check
docker-compose ps

# Check if ready
docker exec -it springfood-postgres pg_isready -U postgres
```

### Clear all data and restart

```bash
docker-compose down -v
docker-compose up -d
```

## 📝 Configuration in Application

Các services đã được config sẵn trong `application.yaml`:

```yaml
# PostgreSQL
spring.datasource.url: jdbc:postgresql://localhost:5432/product_service

# Redis
spring.redis.host: localhost
spring.redis.port: 6379
spring.redis.password: ${DEFAULT_DATABASE_PASSWORD}

# MinIO
minio.url: http://localhost:9000
minio.access-key: minioadmin
minio.secret-key: minioadmin
minio.bucket-name: images

# Kafka
spring.kafka.bootstrap-servers: localhost:9092
```

## 🎯 Next Steps

1. Start Docker services: `docker-compose up -d`
2. Wait for all services to be healthy (30-60 seconds)
3. Run Spring Boot applications
4. Access MinIO Console: http://localhost:9001

## 💡 Tips

- MinIO bucket "images" được tạo tự động với public read access
- PostgreSQL databases được tạo tự động khi start
- Redis password: 123456 (match với config trong application.yaml)
- Tất cả data được persist trong Docker volumes
