# 🔍 Product Search API Guide

## ✅ Status: ENABLED & WORKING

API tìm kiếm sản phẩm đã được kích hoạt và sẵn sàng sử dụng!

---

## 📡 Available Endpoints

### 1. **GET /products/search/price** - Tìm Theo Khoảng Giá

**URL:** `GET /api/products/search/price`

**Query Parameters:**
- `from` (required): Giá tối thiểu (String, sẽ convert sang BigDecimal)
- `to` (required): Giá tối đa (String, sẽ convert sang BigDecimal)
- `page` (optional): Số trang (default: 0)
- `size` (optional): Số items/trang (default: 5)
- `sort` (optional): Sắp xếp (default: "id,ASC")

**Example Requests:**
```bash
# Tìm sản phẩm từ 10,000 đến 50,000
GET /api/products/search/price?from=10000&to=50000

# Với pagination
GET /api/products/search/price?from=10000&to=50000&page=0&size=10

# Với sorting
GET /api/products/search/price?from=10000&to=50000&sort=price,DESC
```

**Response:**
```json
{
  "code": 200,
  "message": "Search successfully",
  "data": {
    "content": [
      {
        "id": "uuid",
        "name": "Product Name",
        "price": "25000",
        "description": "...",
        "images": "[...]",
        "quantity": 100,
        "averageRating": 4.5,
        "totalFeedbacks": 10
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 5
    },
    "totalElements": 25,
    "totalPages": 5
  }
}
```

**Validation:**
- ✅ `from` phải là số hợp lệ
- ✅ `to` phải là số hợp lệ
- ✅ `from` phải <= `to`
- ❌ Nếu vi phạm: `400 Bad Request` với message "Data sai" hoặc "From must be not greater than to"

---

### 2. **GET /products/search** - Tìm Kiếm Động (Dynamic Search)

**URL:** `GET /api/products/search`

**Query Parameters:**
- `{field}={operation}{value}`: Dynamic search criteria
- `page` (optional): Số trang (default: 0)
- `size` (optional): Số items/trang (default: 10)
- `sort` (optional): Sắp xếp (default: "id,ASC")

**Supported Operations:**

| Operator | Meaning | Example | SQL Equivalent |
|----------|---------|---------|----------------|
| `=` | Equals | `quantity==100` | `quantity = 100` |
| `!=` | Not equals | `quantity=!=0` | `quantity != 0` |
| `>` | Greater than | `price=>10000` | `price > 10000` |
| `>=` | Greater than or equal | `price=>=10000` | `price >= 10000` |
| `<` | Less than | `price=<50000` | `price < 50000` |
| `<=` | Less than or equal | `price=<=50000` | `price <= 50000` |
| `:` | Contains (LIKE) | `name=:laptop` | `LOWER(name) LIKE '%laptop%'` |
| `~` | Between (range) | `price=~10000-50000` | `price BETWEEN 10000 AND 50000` |

**Searchable Fields:**
- `name` (String) - Tên sản phẩm
- `description` (String) - Mô tả
- `price` (Number) - Giá
- `quantity` (Number) - Số lượng
- `sku` (String) - Mã SKU
- `averageRating` (Number) - Đánh giá trung bình
- `totalFeedbacks` (Number) - Số lượng feedback

**Example Requests:**

```bash
# 1. Tìm sản phẩm có tên chứa "laptop"
GET /api/products/search?name=:laptop

# 2. Tìm sản phẩm giá từ 10,000 đến 50,000
GET /api/products/search?price=~10000-50000

# 3. Tìm sản phẩm giá > 10,000 VÀ quantity > 0
GET /api/products/search?price=>10000&quantity=>0

# 4. Tìm sản phẩm có rating >= 4.0
GET /api/products/search?averageRating=>=4.0

# 5. Tìm sản phẩm tên chứa "phone" VÀ giá <= 20,000,000
GET /api/products/search?name=:phone&price=<=20000000

# 6. Tìm sản phẩm quantity != 0 (còn hàng)
GET /api/products/search?quantity=!=0

# 7. Complex search với pagination
GET /api/products/search?name=:laptop&price=>10000&price=<50000&quantity=>0&page=0&size=20&sort=price,ASC

# 8. Tìm sản phẩm có nhiều feedback
GET /api/products/search?totalFeedbacks=>10&averageRating=>=4.0
```

**Response:**
```json
{
  "code": 200,
  "message": "Search products successfully",
  "data": {
    "content": [
      {
        "id": "uuid",
        "name": "Laptop Dell XPS 13",
        "price": "25000000",
        "description": "High-performance laptop",
        "images": "[...]",
        "quantity": 15,
        "averageRating": 4.5,
        "totalFeedbacks": 23
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 45,
    "totalPages": 5
  }
}
```

**Error Response:**
```json
{
  "code": 400,
  "message": "Search failed: Invalid field name or operation",
  "data": null
}
```

---

## 🔧 Technical Implementation

### Architecture

```
ProductResource (Controller)
    ↓
ProductService (Interface)
    ↓
ProductServiceImpl (Implementation)
    ↓
SearchSpecification (JPA Specification Builder)
    ↓
ProductRepository (JpaSpecificationExecutor)
    ↓
Database (PostgreSQL)
```

### Key Components

**1. SearchCriteria.java**
```java
public class SearchCriteria {
    String keyword;    // Field name (e.g., "price", "name")
    String operation;  // Operator (e.g., "=", ">", ":")
    String value;      // Search value
}
```

**2. SearchSpecification.java**
- Interface với static methods
- Build JPA Specification từ SearchCriteria
- Hỗ trợ nested fields với join (e.g., "category.name")
- Auto-detect numeric vs string fields
- Case-insensitive LIKE search

**3. ProductRepository**
```java
public interface ProductRepository extends
        JpaRepository<Product, UUID>,
        JpaSpecificationExecutor<Product> // ← Enables Specification queries
```

### Search Flow

```
1. Request: GET /api/products/search?name=:laptop&price=>10000

2. Parse Query Params:
   - name=:laptop → SearchCriteria(keyword="name", operation=":", value="laptop")
   - price=>10000 → SearchCriteria(keyword="price", operation=">", value="10000")

3. Build Specifications:
   - Spec1: LOWER(name) LIKE '%laptop%'
   - Spec2: price > 10000
   - Combined: Spec1 AND Spec2

4. Execute Query:
   - productRepository.findAll(combinedSpec, pageable)

5. Map to DTO:
   - Product → ProductDetail

6. Return Response
```

---

## 🎯 Use Cases

### E-commerce Frontend

**1. Search Bar**
```javascript
// User types "laptop"
fetch('/api/products/search?name=:laptop')
```

**2. Price Filter**
```javascript
// User selects price range 10M - 50M
fetch('/api/products/search?price=~10000000-50000000')
```

**3. Multi-Filter**
```javascript
// User filters: category + price + rating
fetch('/api/products/search?name=:laptop&price=<30000000&averageRating=>=4.0')
```

**4. Sort by Price**
```javascript
// User sorts by price ascending
fetch('/api/products/search?name=:laptop&sort=price,ASC')
```

**5. In-Stock Only**
```javascript
// Show only available products
fetch('/api/products/search?quantity=>0')
```

---

## 🚀 Performance Considerations

### Recommended Database Indexes

```sql
-- Price range queries
CREATE INDEX idx_product_price ON products(price);

-- Quantity filtering
CREATE INDEX idx_product_quantity ON products(quantity);

-- Rating filtering
CREATE INDEX idx_product_rating ON products(average_rating);

-- Full-text search (PostgreSQL)
CREATE INDEX idx_product_name_gin ON products USING gin(to_tsvector('english', name));
CREATE INDEX idx_product_description_gin ON products USING gin(to_tsvector('english', description));

-- Composite index for common filters
CREATE INDEX idx_product_price_quantity ON products(price, quantity) WHERE quantity > 0;
```

### Caching Strategy

**Current:** No caching for search results

**Recommended:**
```java
// Cache search results for 5 minutes
String cacheKey = "search:" + generateCacheKey(params, pageable);
redisServiceWrapper.setValue(cacheKey, results);
redisServiceWrapper.setTimeout(cacheKey, 5, TimeUnit.MINUTES);
```

---

## ⚠️ Limitations & Known Issues

### Current Limitations

1. **No Full-Text Search**
   - Operator `:` uses SQL LIKE → slow with large datasets
   - No fuzzy matching, typo tolerance
   - No relevance scoring

2. **No Faceted Search**
   - Cannot get aggregations (e.g., "50 products in this price range")
   - No filter counts

3. **AND Logic Only**
   - All criteria combined with AND
   - No support for OR conditions
   - Example: Cannot search "name contains 'laptop' OR 'notebook'"

4. **No Nested Field Search**
   - Cannot search by category name directly
   - Would need: `productCategories.categories.name=:electronics`
   - Currently throws exception

5. **Case-Sensitive for Exact Match**
   - `name==Laptop` won't match "laptop"
   - Only `:` operator is case-insensitive

### Workarounds

**1. OR Logic:**
```bash
# Instead of: name=:laptop OR name=:notebook
# Use two separate requests and merge results client-side
```

**2. Category Search:**
```bash
# Use dedicated endpoint (if exists)
GET /api/products/category/{categoryName}
```

**3. Full-Text Search:**
```bash
# Use `:` operator for basic contains
GET /api/products/search?name=:laptop
# Or implement Elasticsearch for advanced search
```

---

## 🔐 Security

### Current Status
- ✅ **No @PreAuthorize** - All users can search
- ✅ **Input validation** - Regex pattern prevents SQL injection
- ✅ **Parameterized queries** - JPA Specification uses prepared statements

### Recommendations

**1. Rate Limiting**
```java
@RateLimiter(name = "searchApi", fallbackMethod = "searchFallback")
public Page<ProductDetail> search(Pageable pageable, Map<String, String> params) {
    // ...
}
```

**2. Input Sanitization**
```java
// Limit search value length
if (value.length() > 100) {
    throw new InvalidDataException("Search value too long");
}
```

**3. Field Whitelist**
```java
private static final Set<String> ALLOWED_FIELDS = Set.of(
    "name", "description", "price", "quantity", "averageRating"
);

if (!ALLOWED_FIELDS.contains(keyword)) {
    throw new InvalidDataException("Invalid search field");
}
```

---

## 📊 Testing

### Manual Testing with cURL

```bash
# 1. Search by name
curl "http://localhost:8082/api/products/search?name=:laptop"

# 2. Search by price range
curl "http://localhost:8082/api/products/search?price=~10000-50000"

# 3. Multi-criteria search
curl "http://localhost:8082/api/products/search?name=:phone&price=<20000000&quantity=>0"

# 4. Search with pagination
curl "http://localhost:8082/api/products/search?name=:laptop&page=0&size=20&sort=price,ASC"

# 5. Price range endpoint
curl "http://localhost:8082/api/products/search/price?from=10000&to=50000"
```

### Postman Collection

```json
{
  "info": {
    "name": "Product Search API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Search by Name",
      "request": {
        "method": "GET",
        "url": {
          "raw": "{{baseUrl}}/products/search?name=:laptop",
          "host": ["{{baseUrl}}"],
          "path": ["products", "search"],
          "query": [
            {"key": "name", "value": ":laptop"}
          ]
        }
      }
    },
    {
      "name": "Search by Price Range",
      "request": {
        "method": "GET",
        "url": {
          "raw": "{{baseUrl}}/products/search?price=~10000-50000",
          "host": ["{{baseUrl}}"],
          "path": ["products", "search"],
          "query": [
            {"key": "price", "value": "~10000-50000"}
          ]
        }
      }
    }
  ]
}
```

---

## 🎓 Best Practices

### For Frontend Developers

**1. Debounce Search Input**
```javascript
const searchProducts = debounce((query) => {
  fetch(`/api/products/search?name=:${query}`)
}, 300); // Wait 300ms after user stops typing
```

**2. Build Query String Properly**
```javascript
const buildSearchUrl = (filters) => {
  const params = new URLSearchParams();
  
  if (filters.name) params.append('name', `:${filters.name}`);
  if (filters.minPrice) params.append('price', `>=${filters.minPrice}`);
  if (filters.maxPrice) params.append('price', `<=${filters.maxPrice}`);
  if (filters.inStock) params.append('quantity', '>0');
  
  return `/api/products/search?${params.toString()}`;
};
```

**3. Handle Pagination**
```javascript
const loadMore = (currentPage) => {
  fetch(`/api/products/search?name=:laptop&page=${currentPage}&size=20`)
    .then(res => res.json())
    .then(data => {
      appendProducts(data.content);
      if (currentPage < data.totalPages - 1) {
        showLoadMoreButton();
      }
    });
};
```

### For Backend Developers

**1. Add Logging**
```java
log.info("Search request: params={}, pageable={}", params, pageable);
log.info("Search results: totalElements={}, totalPages={}", 
         products.getTotalElements(), products.getTotalPages());
```

**2. Add Metrics**
```java
@Timed(value = "product.search", description = "Time taken to search products")
public Page<ProductDetail> search(Pageable pageable, Map<String, String> params) {
    // ...
}
```

**3. Validate Input**
```java
// Limit page size
if (pageable.getPageSize() > 100) {
    throw new InvalidDataException("Page size too large (max: 100)");
}
```

---

## 🔮 Future Enhancements

### Phase 1: Immediate Improvements
- [ ] Add field whitelist validation
- [ ] Add rate limiting
- [ ] Add search analytics logging
- [ ] Create database indexes

### Phase 2: Advanced Features
- [ ] Implement Elasticsearch for full-text search
- [ ] Add faceted search (aggregations)
- [ ] Add autocomplete/suggestions
- [ ] Add "did you mean?" for typos

### Phase 3: Optimization
- [ ] Add Redis caching for search results
- [ ] Implement search result ranking
- [ ] Add popular searches tracking
- [ ] Optimize query performance

---

## 📞 Support

**Issues?**
- Check logs: `product-service/logs/`
- Verify database connection
- Test with simple queries first
- Check field names match Product entity

**Questions?**
- Review SearchSpecification.java for supported operations
- Check Product entity for available fields
- Test with Postman collection
