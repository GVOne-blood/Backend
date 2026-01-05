# Database Schema Documentation

## 📊 Tổng quan

Dự án SpringFood sử dụng kiến trúc **Database per Service** với PostgreSQL cho mỗi microservice.

**Databases:**
- `identity_service` - User, Role, Permission, Address
- `product_service` - Product, Category, Feedback
- `order_service` - Order, OrderItem
- `payment_service` - Payment, PaymentTransaction
- `shop_service` - Shop, ShopMember

---

## 👤 Identity Service Database

### **User Table**

```sql
CREATE TABLE "user" (
    user_id UUID PRIMARY KEY,
    username VARCHAR(255) UNIQUE,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL,  -- ACTIVE, INACTIVE, BANNED
    phone VARCHAR(20) UNIQUE,
    dob DATE,
    password VARCHAR(255) NOT NULL,
    avatar VARCHAR(255),
    gender VARCHAR(10),  -- MALE, FEMALE, OTHER
    last_login_at TIMESTAMP,
    phone_verified BOOLEAN DEFAULT FALSE,
    email_verified BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    address TEXT,  -- Legacy field
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**Relationships:**
- `OneToMany` → UserHasRole
- `OneToMany` → Address
- `OneToMany` → Token

**Enums:**
- `UserStatus`: ACTIVE, INACTIVE, BANNED, PENDING
- `Gender`: MALE, FEMALE, OTHER

---

### **Address Table**

```sql
CREATE TABLE address (
    address_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    recipient_name VARCHAR(100),
    phone_number VARCHAR(20),
    street_address TEXT,
    ward VARCHAR(100),
    district VARCHAR(100),
    city VARCHAR(100),
    province VARCHAR(100),
    is_default BOOLEAN DEFAULT FALSE,
    label VARCHAR(50),  -- "Nhà", "Công ty"
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "user"(user_id)
);
```

---

### **Role & Permission Tables**

```sql
CREATE TABLE role (
    role_id UUID PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,  -- ADMIN, CUSTOMER, SHOP_OWNER, STAFF
    description TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE permission (
    permission_id UUID PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,  -- product:read, order:write
    description TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE user_has_role (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    created_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "user"(user_id),
    FOREIGN KEY (role_id) REFERENCES role(role_id)
);

CREATE TABLE role_has_permission (
    id UUID PRIMARY KEY,
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    created_at TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES role(role_id),
    FOREIGN KEY (permission_id) REFERENCES permission(permission_id)
);
```

**RBAC Model:**
- User → UserHasRole → Role → RoleHasPermission → Permission
- Roles: ADMIN, CUSTOMER, SHOP_OWNER, STAFF
- Permissions: product:read, product:write, order:read, order:write, etc.

---

### **Token Table**

```sql
CREATE TABLE token (
    token_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_value TEXT NOT NULL,
    token_type VARCHAR(20),  -- ACCESS, REFRESH, RESET
    expires_at TIMESTAMP,
    is_revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "user"(user_id)
);
```

---

## 🛍️ Product Service Database

### **Product Table**

```sql
CREATE TABLE products (
    product_id UUID PRIMARY KEY,
    shop_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    sku VARCHAR(100) UNIQUE,
    description TEXT,
    msg DATE,  -- Manufacturing date
    exp DATE,  -- Expiration date
    product_status VARCHAR(20),  -- AVAILABLE, OUT_OF_STOCK, DISCONTINUED
    price DECIMAL(15,2) NOT NULL,
    wholesale_price DECIMAL(15,2),
    avg_rate DECIMAL(3,2) DEFAULT 0,
    quantity INTEGER DEFAULT 0,
    images JSONB,  -- Array of image URLs
    total_feedbacks BIGINT DEFAULT 0,
    average_rating DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**Relationships:**
- `ManyToOne` → Shop (via shop_id, cross-service reference)
- `OneToMany` → Feedback
- `OneToMany` → ProductSale
- `OneToMany` → ProductCategory

**Enums:**
- `ProductStatus`: AVAILABLE, OUT_OF_STOCK, DISCONTINUED, PENDING

**Images Field (JSONB):**
```json
["http://minio:9000/images/product1.jpg", "http://minio:9000/images/product2.jpg"]
```

---

### **Categories Table**

```sql
CREATE TABLE categories (
    category_id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_category_id UUID,
    image_url VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (parent_category_id) REFERENCES categories(category_id)
);
```

**Self-referencing:** Supports category hierarchy (parent-child)

---

### **ProductCategory Table (Many-to-Many)**

```sql
CREATE TABLE product_category (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    category_id UUID NOT NULL,
    created_at TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);
```

---

### **Feedback Table**

```sql
CREATE TABLE feedback (
    feedback_id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    user_id UUID NOT NULL,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);
```

---

### **Sale & ProductSale Tables**

```sql
CREATE TABLE sale (
    sale_id UUID PRIMARY KEY,
    name VARCHAR(100),
    discount_percentage DECIMAL(5,2),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    created_at TIMESTAMP
);

CREATE TABLE product_sale (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    sale_id UUID NOT NULL,
    created_at TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    FOREIGN KEY (sale_id) REFERENCES sale(sale_id)
);
```

---

## 📦 Order Service Database

### **Order Table**

```sql
CREATE TABLE orders (
    order_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    shop_id UUID NOT NULL,
    shipper_id UUID,
    payment_transaction_id UUID,  -- Reference ID for payment
    order_status VARCHAR(20),  -- PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED
    customer_notes TEXT,
    delivered_at TIMESTAMP,
    subtotal_amount DECIMAL(15,2),
    shipping_fee DECIMAL(15,2),
    discount_amount DECIMAL(15,2),
    final_price DECIMAL(15,2),
    shipping_address_street VARCHAR(255),
    shipping_address_ward VARCHAR(100),
    shipping_address_city VARCHAR(100),
    shipping_address_details TEXT,
    payment_method_name VARCHAR(20),  -- COD, VNPAY, MOMO, ZALOPAY
    paid_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**Cross-service References:**
- `user_id` → identity-service.user
- `shop_id` → shop-service.shop
- `payment_transaction_id` → payment-service.payment_transactions

**Enums:**
- `OrderStatus`: PENDING, CONFIRMED, PROCESSING, SHIPPING, DELIVERED, CANCELLED, RETURNED
- `PaymentMethod`: COD, VNPAY, MOMO, ZALOPAY

---

### **OrderItem Table**

```sql
CREATE TABLE order_item (
    order_item_id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    product_name VARCHAR(255),
    product_image VARCHAR(255),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    total_price DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);
```

---

## 💳 Payment Service Database

### **Payment Table**

```sql
CREATE TABLE payment (
    payment_id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    payment_method VARCHAR(20),
    amount DECIMAL(15,2),
    status VARCHAR(20),  -- PENDING, SUCCESS, FAILED
    transaction_id VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

---

### **PaymentTransactions Table**

```sql
CREATE TABLE payment_transactions (
    transaction_id UUID PRIMARY KEY,
    reference_id VARCHAR(255) UNIQUE,  -- Used to group multiple orders
    payment_method VARCHAR(20),
    total_amount DECIMAL(15,2),
    status VARCHAR(20),
    payment_url TEXT,
    paid_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**Note:** Multiple orders can share the same `reference_id` for batch payment.

---

## 🏪 Shop Service Database

### **Shop Table**

```sql
CREATE TABLE shops (
    shop_id UUID PRIMARY KEY,
    shop_name VARCHAR(255) UNIQUE NOT NULL,
    logo VARCHAR(255),
    introduction TEXT,
    shop_status VARCHAR(20) NOT NULL,  -- ACTIVE, INACTIVE, SUSPENDED
    total_products INTEGER DEFAULT 0,
    total_sold INTEGER DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**Enums:**
- `ShopStatus`: ACTIVE, INACTIVE, SUSPENDED, PENDING_APPROVAL

---

### **ShopMember Table**

```sql
CREATE TABLE shop_member (
    id UUID PRIMARY KEY,
    shop_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(20),  -- OWNER, MANAGER, STAFF
    joined_at TIMESTAMP,
    created_at TIMESTAMP,
    FOREIGN KEY (shop_id) REFERENCES shops(shop_id)
);
```

---

## 🔄 Outbox Pattern (Event Sourcing)

Mỗi service có bảng `outbox_message` để implement Transactional Outbox Pattern:

```sql
CREATE TABLE outbox_message (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(100),  -- ORDER, PRODUCT, PAYMENT
    aggregate_id VARCHAR(255),
    event_type VARCHAR(100),  -- ORDER_CREATED, PRODUCT_UPDATED
    payload TEXT,  -- JSON
    created_at TIMESTAMP,
    processed_at TIMESTAMP,
    status VARCHAR(20)  -- PENDING, PROCESSED, FAILED
);
```

**Purpose:** Ensure reliable event publishing to Kafka for inter-service communication.

---

## 📊 Database Relationships Summary

### **Cross-Service References (Eventual Consistency)**

```
identity-service.user.user_id
  ↓ (referenced by)
  - order-service.orders.user_id
  - product-service.feedback.user_id
  - shop-service.shop_member.user_id

shop-service.shop.shop_id
  ↓ (referenced by)
  - product-service.products.shop_id
  - order-service.orders.shop_id

product-service.products.product_id
  ↓ (referenced by)
  - order-service.order_item.product_id

order-service.orders.order_id
  ↓ (referenced by)
  - payment-service.payment.order_id
```

**Note:** Cross-service references are NOT enforced by foreign keys. Services communicate via:
- REST APIs (synchronous)
- Kafka events (asynchronous)
- gRPC (inter-service calls)

---

## 🔐 Security & Audit

### **AbstractEntity (Base Class)**

All entities extend `AbstractEntity`:

```java
@MappedSuperclass
public abstract class AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**Audit Fields:**
- `created_at` - Auto-populated on insert
- `updated_at` - Auto-updated on modification

---

## 📈 Indexes & Performance

**Recommended Indexes:**

```sql
-- User lookups
CREATE INDEX idx_user_email ON "user"(email);
CREATE INDEX idx_user_username ON "user"(username);
CREATE INDEX idx_user_phone ON "user"(phone);

-- Product searches
CREATE INDEX idx_product_shop ON products(shop_id);
CREATE INDEX idx_product_status ON products(product_status);
CREATE INDEX idx_product_name ON products(name);

-- Order queries
CREATE INDEX idx_order_user ON orders(user_id);
CREATE INDEX idx_order_shop ON orders(shop_id);
CREATE INDEX idx_order_status ON orders(order_status);
CREATE INDEX idx_order_created ON orders(created_at DESC);

-- Address lookups
CREATE INDEX idx_address_user ON address(user_id);
CREATE INDEX idx_address_default ON address(user_id, is_default);
```

---

## 🚀 Migration Strategy

**Tools:** Flyway / Liquibase (not currently configured)

**Current:** JPA `ddl-auto: update` (development only)

**Production:** Should use versioned migrations

---

## 📝 Data Consistency

**Strategies:**
1. **Saga Pattern** - For distributed transactions (Order → Payment → Inventory)
2. **Outbox Pattern** - For reliable event publishing
3. **Eventual Consistency** - Cross-service data sync via Kafka
4. **Idempotency** - All event handlers are idempotent

---

## 🔍 Query Patterns

### **Common Queries:**

```sql
-- Get user with roles and permissions
SELECT u.*, r.name as role_name, p.name as permission_name
FROM "user" u
JOIN user_has_role uhr ON u.user_id = uhr.user_id
JOIN role r ON uhr.role_id = r.role_id
JOIN role_has_permission rhp ON r.role_id = rhp.role_id
JOIN permission p ON rhp.permission_id = p.permission_id
WHERE u.user_id = ?;

-- Get products with categories
SELECT p.*, c.name as category_name
FROM products p
JOIN product_category pc ON p.product_id = pc.product_id
JOIN categories c ON pc.category_id = c.category_id
WHERE p.shop_id = ?;

-- Get order with items
SELECT o.*, oi.product_name, oi.quantity, oi.unit_price
FROM orders o
JOIN order_item oi ON o.order_id = oi.order_id
WHERE o.user_id = ?
ORDER BY o.created_at DESC;
```

---

## 📚 References

- **JPA Entities:** See `*/model/*.java` files
- **Database Init:** `init-db.sql`
- **Docker Compose:** `docker-compose.yml`
