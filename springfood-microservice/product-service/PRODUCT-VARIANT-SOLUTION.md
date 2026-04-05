# Product Variant & Attributes Solution

## Vấn đề

- Product hiện tại có `sku` nhưng chưa có định nghĩa cụ thể cho variants
- Mỗi loại sản phẩm có attributes khác nhau (áo: màu+size, laptop: RAM+SSD, thực phẩm: khối lượng)
- Shop owner cần tự định nghĩa attributes linh hoạt
- Không thể dùng relational columns cố định

## Giải pháp: PostgreSQL JSONB (RECOMMENDED)

### Tại sao KHÔNG nên thêm MongoDB?

❌ **Không nên:**
- Thêm complexity (2 databases trong 1 service)
- Phải maintain 2 connections
- Transaction giữa PostgreSQL và MongoDB rất khó
- Product đã có relationships với PostgreSQL tables

✅ **Nên dùng PostgreSQL JSONB:**
- PostgreSQL đã support JSONB từ lâu
- Performance tốt, có indexing
- Flexible như NoSQL nhưng vẫn ACID
- Query được với JSON operators
- Product service đã dùng JSONB cho `images` field

## Architecture

```
products (PostgreSQL)
├── id (UUID)
├── shop_id (UUID)
├── name (VARCHAR)
├── sku (VARCHAR) - Base SKU
├── description (TEXT)
├── price (DECIMAL) - Base price
├── quantity (INT) - Total stock
├── images (JSONB) - Đã có
└── ... other fields

product_variants (PostgreSQL - NEW TABLE)
├── id (UUID)
├── product_id (UUID) FK -> products
├── sku (VARCHAR) UNIQUE - Variant SKU
├── variant_name (VARCHAR) - "Màu Đỏ - Size XL"
├── attributes (JSONB) - {"color": "red", "size": "xl"}
├── price (DECIMAL) - Variant price (override base)
├── stock (INT) - Variant stock
├── is_available (BOOLEAN)
├── created_at
└── updated_at

product_attribute_templates (PostgreSQL - NEW TABLE)
├── id (UUID)
├── shop_id (UUID) FK -> shops
├── category_id (UUID) FK -> categories (optional)
├── template_name (VARCHAR) - "Áo thun", "Laptop"
├── attributes_schema (JSONB) - Schema definition
├── created_at
└── updated_at
```

## 1. Product Variant Entity

```java
@Entity
@Table(name = "product_variants")
public class ProductVariant extends AbstractEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "sku", unique = true, nullable = false)
    private String sku;  // VD: "LAPTOP-001-RAM16-SSD512"
    
    @Column(name = "variant_name")
    private String variantName;  // VD: "RAM 16GB - SSD 512GB"
    
    // JSONB field - flexible attributes
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes", columnDefinition = "jsonb")
    private Map<String, Object> attributes;
    // VD: {"ram": "16GB", "ssd": "512GB", "color": "Silver"}
    
    @Column(name = "price", precision = 15, scale = 2)
    private BigDecimal price;  // Override base product price
    
    @Column(name = "stock", nullable = false)
    private Integer stock = 0;
    
    @Column(name = "is_available")
    private Boolean isAvailable = true;
    
    @Column(name = "image_url")
    private String imageUrl;  // Variant-specific image (optional)
}
```

## 2. Attribute Template Entity

Shop owner định nghĩa template cho từng loại sản phẩm:

```java
@Entity
@Table(name = "product_attribute_templates")
public class ProductAttributeTemplate extends AbstractEntity {
    
    @Column(name = "shop_id")
    private UUID shopId;
    
    @Column(name = "category_id")
    private UUID categoryId;  // Optional: template cho category
    
    @Column(name = "template_name")
    private String templateName;  // "Áo thun", "Laptop", "Điện thoại"
    
    // Schema definition
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes_schema", columnDefinition = "jsonb")
    private AttributeSchema attributesSchema;
}

// Schema structure
@Data
public class AttributeSchema {
    private List<AttributeDefinition> attributes;
}

@Data
public class AttributeDefinition {
    private String key;           // "color", "size", "ram"
    private String label;         // "Màu sắc", "Kích thước", "RAM"
    private String type;          // "select", "text", "number"
    private List<String> options; // ["Đỏ", "Xanh", "Vàng"] for select
    private Boolean required;
    private Integer displayOrder;
}
```

### Example Template JSON:

```json
{
  "attributes": [
    {
      "key": "color",
      "label": "Màu sắc",
      "type": "select",
      "options": ["Đỏ", "Xanh", "Vàng", "Đen", "Trắng"],
      "required": true,
      "displayOrder": 1
    },
    {
      "key": "size",
      "label": "Kích thước",
      "type": "select",
      "options": ["S", "M", "L", "XL", "XXL"],
      "required": true,
      "displayOrder": 2
    },
    {
      "key": "material",
      "label": "Chất liệu",
      "type": "text",
      "required": false,
      "displayOrder": 3
    }
  ]
}
```

## 3. Product Entity Update

```java
@Entity
@Table(name = "products")
public class Product extends AbstractEntity {
    
    // ... existing fields ...
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductVariant> variants = new ArrayList<>();
    
    @Column(name = "has_variants")
    private Boolean hasVariants = false;
    
    @Column(name = "attribute_template_id")
    private UUID attributeTemplateId;  // Reference to template
    
    // Helper methods
    public Integer getTotalStock() {
        if (hasVariants) {
            return variants.stream()
                .mapToInt(ProductVariant::getStock)
                .sum();
        }
        return quantity;
    }
    
    public BigDecimal getMinPrice() {
        if (hasVariants && !variants.isEmpty()) {
            return variants.stream()
                .map(ProductVariant::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(price);
        }
        return price;
    }
}
```

## 4. API Examples

### Create Product with Variants

```java
POST /api/products

{
  "name": "Áo thun nam",
  "description": "Áo thun cotton cao cấp",
  "basePrice": 200000,
  "attributeTemplateId": "template-uuid",
  "variants": [
    {
      "sku": "SHIRT-001-RED-M",
      "variantName": "Đỏ - M",
      "attributes": {
        "color": "Đỏ",
        "size": "M"
      },
      "price": 200000,
      "stock": 50
    },
    {
      "sku": "SHIRT-001-RED-L",
      "variantName": "Đỏ - L",
      "attributes": {
        "color": "Đỏ",
        "size": "L"
      },
      "price": 200000,
      "stock": 30
    },
    {
      "sku": "SHIRT-001-BLUE-M",
      "variantName": "Xanh - M",
      "attributes": {
        "color": "Xanh",
        "size": "M"
      },
      "price": 200000,
      "stock": 40
    }
  ]
}
```

### Query Variants

```sql
-- Get all variants of a product
SELECT * FROM product_variants 
WHERE product_id = 'product-uuid';

-- Query by specific attribute (JSONB operators)
SELECT * FROM product_variants 
WHERE attributes->>'color' = 'Đỏ'
  AND attributes->>'size' = 'M';

-- Query with JSONB contains
SELECT * FROM product_variants 
WHERE attributes @> '{"color": "Đỏ", "size": "M"}';

-- Create index for better performance
CREATE INDEX idx_variant_attributes ON product_variants USING GIN (attributes);
```

## 5. Response DTO

```java
@Data
public class ProductDetailResponse {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal minPrice;  // Lowest variant price
    private BigDecimal maxPrice;  // Highest variant price
    private Integer totalStock;
    
    // Variants
    private Boolean hasVariants;
    private List<ProductVariantResponse> variants;
    
    // Attribute schema for frontend
    private AttributeSchema attributeSchema;
}

@Data
public class ProductVariantResponse {
    private UUID id;
    private String sku;
    private String variantName;
    private Map<String, Object> attributes;
    private BigDecimal price;
    private Integer stock;
    private Boolean isAvailable;
    private String imageUrl;
}
```

## 6. Frontend Integration

### Product Display Page

```javascript
// Frontend receives:
{
  "id": "product-uuid",
  "name": "Áo thun nam",
  "basePrice": 200000,
  "minPrice": 200000,
  "maxPrice": 220000,
  "hasVariants": true,
  "attributeSchema": {
    "attributes": [
      {
        "key": "color",
        "label": "Màu sắc",
        "type": "select",
        "options": ["Đỏ", "Xanh", "Vàng"]
      },
      {
        "key": "size",
        "label": "Kích thước",
        "type": "select",
        "options": ["S", "M", "L", "XL"]
      }
    ]
  },
  "variants": [
    {
      "sku": "SHIRT-001-RED-M",
      "variantName": "Đỏ - M",
      "attributes": {"color": "Đỏ", "size": "M"},
      "price": 200000,
      "stock": 50,
      "isAvailable": true
    }
  ]
}

// Frontend renders:
// - Dropdown/buttons for each attribute
// - When user selects color + size → find matching variant
// - Display variant price, stock, availability
```

## 7. Migration Strategy

### Phase 1: Add new tables
```sql
CREATE TABLE product_variants (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id),
    sku VARCHAR(255) UNIQUE NOT NULL,
    variant_name VARCHAR(255),
    attributes JSONB,
    price DECIMAL(15,2),
    stock INTEGER NOT NULL DEFAULT 0,
    is_available BOOLEAN DEFAULT true,
    image_url VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_variant_product ON product_variants(product_id);
CREATE INDEX idx_variant_sku ON product_variants(sku);
CREATE INDEX idx_variant_attributes ON product_variants USING GIN (attributes);

CREATE TABLE product_attribute_templates (
    id UUID PRIMARY KEY,
    shop_id UUID NOT NULL,
    category_id UUID,
    template_name VARCHAR(255),
    attributes_schema JSONB NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Phase 2: Migrate existing products
```java
// Products without variants keep working as-is
// New products can use variants
// Gradually migrate old products if needed
```

## 8. Advantages

✅ **Flexibility**: Shop owner tự định nghĩa attributes
✅ **Performance**: JSONB có indexing, query nhanh
✅ **ACID**: Vẫn có transactions
✅ **Simple**: Không cần thêm database
✅ **Scalable**: JSONB handle được millions of records
✅ **Type Safety**: Java entities + validation
✅ **Query Power**: SQL + JSON operators

## 9. Alternative: Pure JSONB in Product table

Nếu muốn đơn giản hơn, có thể lưu variants trực tiếp trong Product:

```java
@Entity
@Table(name = "products")
public class Product {
    // ... existing fields ...
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "variants", columnDefinition = "jsonb")
    private List<ProductVariantData> variants;
}
```

**Nhược điểm:**
- Không có foreign key constraints
- Khó query variants riêng lẻ
- Không có unique constraint cho variant SKU

**→ Recommend: Separate `product_variants` table**
