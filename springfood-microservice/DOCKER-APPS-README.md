# Dockerizing 12 Spring Boot services

Toàn bộ stack giờ được chia thành 3 file compose:

| File | Mục đích |
|------|----------|
| `docker-compose.yml`        | Hạ tầng local cũ (Postgres, Redis, MinIO, Kafka, Mongo, ELK, Prometheus, Grafana, Carbone, Kafka UI). **Tùy chọn** — chỉ cần khi mày muốn chạy Mongo / Carbone / MinIO local. |
| `docker-compose.apps.yml`   | 12 service Spring Boot, build từ source bằng `Dockerfile.service`. Image production-ready. **Mặc định kết nối tới Postgres / Redis / Kafka / R2 / Brevo / Gemini trên cloud** thông qua `.env`. |
| `docker-compose.dev.yml`    | Override cho dev: bind-mount source + chạy `mvn spring-boot:run` (hot reload). Dùng `Dockerfile.dev`. |

> ⚠ **Hạ tầng = cloud, không phải local.** `.env` đang trỏ tới Neon (Postgres), Upstash (Redis), Aiven (Kafka), Cloudflare R2 (object storage), Brevo (email), Gemini (AI). Compose đã được sửa để KHÔNG hardcode hostname `postgres` / `redis` / `minio` / `mongodb` / `carbone` của container infra cũ. Nếu mày muốn bật infra local kèm theo, thêm `-f docker-compose.yml` vào lệnh.

## 12 service & port

| Service             | Module Maven         | HTTP | gRPC |
|---------------------|----------------------|------|------|
| Eureka Server       | `eureka-server`      | 8761 | —    |
| API Gateway         | `api-gateway`        | 8080 | —    |
| Authentication      | `authentication`     | 8081 | —    |
| Cart Service        | `cart-service`       | 8082 | —    |
| Order Service       | `order-service`      | 8083 | 9097 |
| Shop Service        | `shop-service`       | 8084 | 9090 |
| Product Service     | `product-service`    | 8085 | 9095 |
| Payment Service     | `payment-service`    | 8086 | 9096 |
| Action Log          | `action-log`         | 8094 | —    |
| Notification        | `notification`       | 8095 | —    |
| Media               | `media`              | 8099 | —    |
| Statistical Report  | `statistical-report` | 8090 | —    |
| Chat                | `chat`               | 9098 | —    |

> Port mapping bám theo `application.yaml` / `application-dev.yml` hiện tại của từng module.

## 1. Build & chạy production-style

```bat
:: Cloud-only (mặc định) — chỉ build 12 service, hạ tầng đọc từ .env
docker compose -f docker-compose.apps.yml up -d --build

:: Nếu muốn bật thêm Mongo/Carbone/MinIO local
docker compose -f docker-compose.yml -f docker-compose.apps.yml up -d --build

:: Theo dõi log
docker compose -f docker-compose.apps.yml logs -f api-gateway

:: Stop
docker compose -f docker-compose.apps.yml down
```

Mỗi service được build qua `Dockerfile.service` (multi-stage):
- Stage `build`: dùng `maven:3.9.9-eclipse-temurin-21`, chỉ build module được chỉ định bằng `-pl <module> -am`.
- Stage `runtime`: `eclipse-temurin:21-jre`, chạy với user `app` non-root.

Eureka, gRPC client URL và DNS service đều được override bằng env var để các container gọi nhau qua tên dịch vụ (ví dụ `static://shop-service:9090`).

## 2. Trả lời câu hỏi: "Sửa code thì compose có tự update không?"

Câu trả lời ngắn: **không** — với cấu hình production trong `docker-compose.apps.yml`, Docker chạy file jar đã đóng gói lúc `--build`. Khi bạn sửa code thì image cũ vẫn không đổi, container vẫn chạy jar cũ. Bạn phải build lại:

```bat
:: Rebuild + start lại 1 service
docker compose -f docker-compose.yml -f docker-compose.apps.yml up -d --build product-service

:: Hoặc rebuild tất cả
docker compose -f docker-compose.yml -f docker-compose.apps.yml up -d --build
```

Lý do: image production chỉ chứa file `app.jar` được tạo ở build time, không có Maven hay source code bên trong; đây là behavior chuẩn để image gọn và immutable.

## 3. Hot reload khi dev

```bat
:: Lần đầu build image dev nhỏ (chỉ chứa Maven + JDK)
docker compose -f docker-compose.apps.yml -f docker-compose.dev.yml build

:: Chạy
docker compose -f docker-compose.apps.yml -f docker-compose.dev.yml up
```

Với mode dev:
- Toàn bộ source code được **bind mount** từ host vào `/workspace` trong container.
- Container chạy `mvn -pl <module> -am spring-boot:run`.
- Cache Maven `.m2` dùng chung qua named volume `maven-cache`, lần build kế tiếp nhanh hơn nhiều.
- Khi bạn sửa file Java và compile (IntelliJ tự compile, hoặc chạy `mvn -pl <module> -am compile` ở host), **Spring Boot DevTools** trong container sẽ auto-restart phần classpath. Không cần `docker compose restart`.
- Khi sửa file `application.yml` hoặc resource khác → DevTools cũng reload tự động.

Khi nào bắt buộc rebuild?
- Sửa `pom.xml` (thêm/đổi dependency, plugin) → chạy lại `docker compose ... up --build` cho service tương ứng.
- Đổi base image hoặc `Dockerfile.dev` → cũng phải build lại image dev.

> Mẹo: nếu module bạn đang code chưa có dependency `spring-boot-devtools` ở scope runtime, thêm tạm vào `pom.xml` của module đó để tận dụng auto-restart:
>
> ```xml
> <dependency>
>     <groupId>org.springframework.boot</groupId>
>     <artifactId>spring-boot-devtools</artifactId>
>     <optional>true</optional>
> </dependency>
> ```

## 4. Lưu ý cấu hình hiện có

- `product-service/src/main/resources/application.yaml` đang hard-code `server.address: 127.0.0.1`. Compose đã override bằng `SERVER_ADDRESS=0.0.0.0` để service lắng nghe trên mọi interface (nếu không, container không thể nhận traffic từ network).
- Các gRPC client URL `static://localhost:9095/9090/...` được override sang DNS container (`static://shop-service:9090`,...). Eureka thì set `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/`.
- Truststore/keystore Kafka được mount từ `./deploy/messaging/certs` vào `/app/certs` trong container. Đường dẫn trong env `KAFKA_SSL_TRUSTSTORE_LOCATION` đã trỏ tới đúng vị trí trong container.
- File `.env` ở root được Docker Compose tự nạp; không hard-code secret trong YAML.

## 5. Lệnh hay dùng

```bat
:: Restart 1 service
docker compose -f docker-compose.yml -f docker-compose.apps.yml restart order-service

:: Tail log nhiều service
docker compose -f docker-compose.yml -f docker-compose.apps.yml logs -f api-gateway authentication

:: Vào shell container
docker compose -f docker-compose.yml -f docker-compose.apps.yml exec product-service sh

:: Build lại 1 service sau khi sửa pom
docker compose -f docker-compose.yml -f docker-compose.apps.yml build product-service
```
