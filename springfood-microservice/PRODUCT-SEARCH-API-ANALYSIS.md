# Phân Tích API Tìm Kiếm Sản Phẩm - SpringFood Backend

## 📋 Tổng Quan

API tìm kiếm sản phẩm trong SpringFood hiện tại **CHƯA ĐƯỢC TRIỂN KHAI** (đã bị comment out). Tuy nhiên, có thể thấy rõ kiến trúc và thiết kế đã được chuẩn bị sẵn.

---

## 🔍 Các API Endpoints Hiện Có

### 1. **GET /products/** - Lấy Tất Cả Sản Phẩm
```java
@GetMapping("/")
public ResponseEntity<ResponseData<Page<ProductDetail>>> getAllProducts(
    @PageableDefault(size = 10, page = 0) Pageable pageable
)
```

**Đặc điểm:**
- ✅ **Pagination**: Hỗ trợ phân trang (mặc định 10 items/page)
- ✅ **Caching**: Không có caching ở endpoint này
- ✅ **Performance**: Query trực tiếp từ DB qua JPA Projection
- 📊 **Use Case**: Hiển thị danh sách sản phẩm cơ bản

**Implementation:**
```java
// ProductServiceImpl.java
@Override
public Page<ProductDetail> getAllProductDetails(Pageable pageable) {
    Page<ProductProjection> projections = productRepository.findListProduct(pageable);
    return projections.map(productMapper::toProductDetail);
}
```

**Query:**
```java
@Query("SELECT p FROM Product p")
Page<ProductProjection> findListProduct(Pageable pageable);
```

---

### 2. **GET /products/{id}** - Lấy Chi Tiết Sản Phẩm
```java
@GetMapping("/{id}")
public ResponseEntity<ResponseData<ProductDetail>> getProductById(@PathVariable("id") UUID id)
```

**Đặc điểm:**
- ✅ **Redis Caching**: Cache product detail với key `product_detail:{productId}`
- ✅ **Async Processing**: Sử dụng ExecutorService để fetch và cache
- ✅ **Timeout**: 10 giây timeout cho cache task
- ✅ **Fallback**: Nếu cache fail, throw exception
- 🔄 **Side Effect**: Tự động trigger cache related products (async)

**Cache Flow:**
```
1. Check Redis cache → Hit? Return
2. Cache miss → Submit task to ExecutorService
3. Wait 10s for task completion
4. Read from cache again
5. Async: Trigger related products caching
```

---

### 3. **GET /products/related/{id}** - Sản Phẩm Liên Quan (Có Pagination)
```java
@GetMapping("/related/{id}")
public ResponseEntity<ResponseData<Page<ProductDetail>>> getRelatedProducts(
    @PathVariable("id") UUID id, 
    @PageableDefault(size = 10, page = 0) Pageable pageable
)
```

**Đặc điểm:**
- ✅ **Redis Caching**: Cache list UUID với key `related_products:{productId}`
- ✅ **Category-Based**: Tìm sản phẩm cùng category
- ✅ **Async Task**: Sử dụng `ProductCacheService.submitProductRelateTask()`
- ✅ **Timeout**: 10 giây timeout
- ✅ **Fallback**: Nếu cache fail, query trực tiếp DB
- ⏱️ **Cache TTL**: 1 giờ

**Algorithm:**
```
1. Get product categories
2. Check Redis: related_products:{productId}
3. If miss:
   - Submit async task to find related products
   - Wait 10s
   - Read from cache
4. If still miss:
   - Fallback: Direct DB query
   - Cache result for 1 hour
```

**Query (Fallback):**
```java
@Query("SELECT DISTINCT p " +
       "FROM Product p " +
       "JOIN p.productCategories pc1 " +
       "JOIN pc1.categories c " +
       "WHERE c.name IN (SELECT pc2.categories.name FROM ProductCategory pc2 WHERE pc2.product.id = :productId) " +
       "AND p.id != :productId")
Page<ProductProjection> findAllProductsByCategoryName(UUID productId, Pageable pageable);
```

---

### 4. **GET /products/randomRelated/{id}** - Sản Phẩm Liên Quan Ngẫu Nhiên
```java
@GetMapping("/randomRelated/{id}")
public ResponseEntity<ResponseData<List<ProductDetail>>> getRelatedProducts(@PathVariable("id") UUID id)
```

**Đặc điểm:**
- ✅ **No Pagination**: Trả về List thay vì Page
- ✅ **Random**: Sử dụng `ORDER BY RANDOM()` trong SQL
- ✅ **Fixed Limit**: Cố định 20 sản phẩm
- ❌ **No Caching**: Không có Redis cache
- 🎲 **Use Case**: Hiển thị "Có thể bạn cũng thích" với random order

**Query:**
```sql
SELECT p.product_id, p.name, p.description, p.price, p.images, p.quantity, p.msg, p.exp
FROM products p
WHERE p.category_id IN (:categoryIds)
  AND p.product_id != :productId
ORDER BY RANDOM()
LIMIT 20
```

---

## 🚫 API Tìm Kiếm Đã Bị Comment Out

### 1. **GET /products/search/price** - Tìm Theo Khoảng Giá

```java
// @GetMapping("/search/price")
// public ResponseEntity<ResponseData<Page<ProductDetail>>> searchByPrice(
//     @RequestParam String from,
//     @RequestParam String to,
//     @PageableDefault(page = 0, size = 5, sort = "product_id", direction = Sort.Direction.ASC) Pageable pageable
// )
```

**Thiết kế ban đầu:**
- 🔢 **Input**: `from` và `to` (String → BigDecimal)
- ✅ **Validation**: Kiểm tra `from <= to`
- 📊 **Pagination**: Mặc định 5 items/page
- 🔧 **Implementation**: Có 2 cách (đều đã comment):
  - **Specification API** (được chọn)
  - **Named JDBC Template**

**Code (commented):**
```java
// @Override
// public Page<ProductDetail> findByPrice(String from, String to, Pageable pageable) {
//     BigDecimal priceFrom = BigDecimal.valueOf(Double.parseDouble(from));
//     BigDecimal priceTo = BigDecimal.valueOf(Double.parseDouble(to));
//     
//     if (priceFrom.compareTo(priceTo) > 0) 
//         throw new InvalidDataException("From must be not greater than to");
//     
//     // Using Specification
//     Specification<Product> spec = SearchSpecification.between(SearchKeyword.price.name(), priceFrom, priceTo);
//     Page<Product> res = productRepository.findAll(spec, pageable);
//     return res.map(productMapper::toProductDetail);
// }
```

---

### 2. **GET /products/search** - Tìm Kiếm Động (Dynamic Search)

```java
// @GetMapping("/search")
// public ResponseEntity<ResponseData<Page<ProductDetail>>> searchProducts(
//     @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
//     @RequestParam Map<String, String> criteria
// )
```

**Thiết kế ban đầu:**
- 🎯 **Dynamic Criteria**: Nhận `Map<String, String>` với format `field=operation+value`
- 🔧 **Flexible Operations**: Hỗ trợ nhiều toán tử
- 📊 **Pagination**: Mặc định 10 items/page
- 🔗 **AND Logic**: Tất cả criteria được kết hợp bằng AND

**Supported Operations:**
| Operator | Meaning | Example |
|----------|---------|---------|
| `=` | Equals | `status==ACTIVE` |
| `!=` | Not equals | `status=!=INACTIVE` |
| `>` | Greater than | `quantity=>10` |
| `>=` | Greater than or equal | `price=>=50` |
| `<` | Less than | `quantity=<100` |
| `<=` | Less than or equal | `price=<=200` |
| `:` | Contains (string) | `name=:laptop` |
| `~` | Range (planned) | `price=~50-200` |

**Example Requests:**
```
GET /products/search?quantity=>10&price=<=100
GET /products/search?name=:laptop&status==ACTIVE
GET /products/search?price=>=50&price=<=200&quantity=>0
```

**Implementation (commented):**
```java
// @PreAuthorize("hasRole('ADMIN')")
// public Page<ProductDetail> search(Pageable pageable, Map<String, String> params) {
//     if (params.isEmpty()) {
//         return productRepository.findAll(pageable).map(productMapper::toProductDetail);
//     }
//     
//     // Regex pattern: ^(!=|<=|>=|[:=<>~])(.+)$
//     Pattern pattern = Pattern.compile("^(!=|<=|>=|[:=<>~])(.+)$");
//     
//     List<SearchCriteria> searchParams = new ArrayList<>();
//     Specification<Product> spec = null;
//     
//     for (Map.Entry<String, String> entry : params.entrySet()) {
//         String key = entry.getKey();
//         String value = entry.getValue();
//         
//         // Skip pagination params
//         if (key.equals("page") || key.equals("size") || key.equals("sort")) continue;
//         
//         Matcher matcher = pattern.matcher(value);
//         if (matcher.matches()) {
//             SearchCriteria searchParam = new SearchCriteria();
//             searchParam.setKeyword(key);
//             searchParam.setOperation(matcher.group(1));
//             searchParam.setValue(matcher.group(2));
//             searchParams.add(searchParam);
//         }
//     }
//     
//     // Build AND specifications
//     for (SearchCriteria searchParam : searchParams) {
//         Specification<Product> currentSpec = SearchSpecification.buildSpecification(searchParam);
//         if (spec == null) {
//             spec = currentSpec;
//         } else {
//             spec = spec.and(currentSpec);
//         }
//     }
//     
//     Page<Product> products = productRepository.findAll(spec, pageable);
//     return products.map(productMapper::toProductDetail);
// }
```

---

## 🏗️ Kiến Trúc Tìm Kiếm (Đã Thiết Kế)

### 1. **JPA Specification Pattern**

Repository đã extend `JpaSpecificationExecutor`:
```java
public interface ProductRepository extends
        JpaRepository<Product, UUID>,
        JpaSpecificationExecutor<Product> // ← Hỗ trợ Specification
```

**Lợi ích:**
- ✅ Type-safe queries
- ✅ Dynamic query building
- ✅ Reusable specifications
- ✅ Composable với AND/OR

### 2. **SearchSpecification Class** (Missing)

Code reference đến class này nhưng **KHÔNG TỒN TẠI**:
```java
// Specification<Product> spec = SearchSpecification.between(...);
// Specification<Product> currentSpec = SearchSpecification.buildSpecification(searchParam);
```

**Folder trống:**
```
springfood-microservice/product-service/src/main/java/com/theblood/productservice/repository/specification/
└── (empty)
```

### 3. **SearchCriteria Class** (Missing)

Code reference đến class này nhưng **KHÔNG TỒN TẠI**:
```java
// SearchCriteria searchParam = new SearchCriteria();
// searchParam.setKeyword(key);
// searchParam.setOperation(matcher.group(1));
// searchParam.setValue(matcher.group(2));
```

---

## 🎯 Phân Tích Thiết Kế

### ✅ Điểm Mạnh

1. **Caching Strategy**
   - Redis caching cho product detail và related products
   - TTL 1 giờ cho related products
   - Async caching để không block request

2. **Async Processing**
   - Sử dụng ExecutorService cho upload và caching
   - Timeout protection (10s)
   - Fallback mechanism khi async fail

3. **Flexible Search Design**
   - Dynamic criteria với Map<String, String>
   - Hỗ trợ nhiều operators
   - Composable specifications (AND logic)

4. **Pagination**
   - Tất cả list endpoints đều có pagination
   - Configurable page size và sort

5. **Projection Pattern**
   - Sử dụng `ProductProjection` để chỉ select cần thiết
   - Giảm memory footprint
   - Tăng performance

### ⚠️ Điểm Yếu & Vấn Đề

1. **Search API Bị Disable**
   - Không có API tìm kiếm nào hoạt động
   - User không thể search theo tên, giá, category
   - Frontend phải implement client-side filtering

2. **Missing Classes**
   - `SearchSpecification` không tồn tại
   - `SearchCriteria` không tồn tại
   - Code sẽ compile error nếu uncomment

3. **No Full-Text Search**
   - Không có Elasticsearch/Solr integration
   - Operator `:` (contains) sẽ dùng SQL LIKE → slow với large dataset
   - Không hỗ trợ fuzzy search, typo tolerance

4. **No Faceted Search**
   - Không có aggregation/facets
   - User không thấy được "Có 50 sản phẩm trong khoảng giá này"
   - Không có filter counts

5. **Security Issue**
   - Search endpoint có `@PreAuthorize("hasRole('ADMIN')")`
   - **Chỉ ADMIN mới search được** → Sai logic
   - Customer không thể search sản phẩm

6. **No Search Analytics**
   - Không track search queries
   - Không có "popular searches"
   - Không có "did you mean?"

7. **Performance Concerns**
   - Related products query có thể slow với nhiều categories
   - `ORDER BY RANDOM()` không scale tốt
   - Không có index hints trong queries

8. **Cache Invalidation**
   - Related products cache 1 giờ → stale data
   - Khi update product, không invalidate related products cache
   - Có `productCacheManager.invalidateProductCache()` nhưng chỉ invalidate product detail

---

## 🔧 Recommendations

### 1. **Triển Khai Search API Cơ Bản**

**Priority: HIGH**

Tạo các class còn thiếu:

```java
// SearchCriteria.java
@Data
public class SearchCriteria {
    private String keyword;      // field name (e.g., "price", "name")
    private String operation;    // operator (e.g., "=", ">", ":")
    private Object value;        // search value
}

// SearchSpecification.java
public class SearchSpecification {
    
    public static Specification<Product> buildSpecification(SearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            String keyword = criteria.getKeyword();
            String operation = criteria.getOperation();
            Object value = criteria.getValue();
            
            switch (operation) {
                case "=":
                    return criteriaBuilder.equal(root.get(keyword), value);
                case "!=":
                    return criteriaBuilder.notEqual(root.get(keyword), value);
                case ">":
                    return criteriaBuilder.greaterThan(root.get(keyword), (Comparable) value);
                case ">=":
                    return criteriaBuilder.greaterThanOrEqualTo(root.get(keyword), (Comparable) value);
                case "<":
                    return criteriaBuilder.lessThan(root.get(keyword), (Comparable) value);
                case "<=":
                    return criteriaBuilder.lessThanOrEqualTo(root.get(keyword), (Comparable) value);
                case ":":
                    return criteriaBuilder.like(root.get(keyword), "%" + value + "%");
                default:
                    return null;
            }
        };
    }
    
    public static Specification<Product> between(String field, Comparable from, Comparable to) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.between(root.get(field), from, to);
    }
}
```

**Uncomment và fix security:**
```java
// Remove @PreAuthorize("hasRole('ADMIN')")
@GetMapping("/search")
public ResponseEntity<ResponseData<Page<ProductDetail>>> searchProducts(
    @PageableDefault(page = 0, size = 10) Pageable pageable,
    @RequestParam Map<String, String> criteria
) {
    // ... existing code
}
```

### 2. **Thêm Full-Text Search**

**Priority: MEDIUM**

**Option A: PostgreSQL Full-Text Search**
```java
@Query(value = """
    SELECT p.* FROM products p
    WHERE to_tsvector('english', p.name || ' ' || p.description) 
          @@ plainto_tsquery('english', :searchText)
    ORDER BY ts_rank(to_tsvector('english', p.name || ' ' || p.description), 
                     plainto_tsquery('english', :searchText)) DESC
    """, nativeQuery = true)
Page<Product> fullTextSearch(@Param("searchText") String searchText, Pageable pageable);
```

**Option B: Elasticsearch Integration**
```java
// Add dependency
// implementation 'org.springframework.boot:spring-boot-starter-data-elasticsearch'

@Document(indexName = "products")
public class ProductDocument {
    @Id
    private String id;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
    
    @Field(type = FieldType.Keyword)
    private List<String> categories;
    
    @Field(type = FieldType.Double)
    private BigDecimal price;
}

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {
    Page<ProductDocument> findByNameOrDescription(String name, String description, Pageable pageable);
}
```

### 3. **Cải Thiện Related Products**

**Priority: MEDIUM**

**Thêm scoring algorithm:**
```java
@Query(value = """
    SELECT p.*, 
           COUNT(DISTINCT pc.category_name) as category_match_count,
           ABS(p.price - :targetPrice) as price_diff
    FROM products p
    JOIN product_categories pc ON p.product_id = pc.product_id
    WHERE pc.category_name IN :categoryNames
      AND p.product_id != :productId
      AND p.product_status = 'ACTIVE'
    GROUP BY p.product_id
    ORDER BY category_match_count DESC, price_diff ASC
    LIMIT :limit
    """, nativeQuery = true)
List<Product> findSmartRelatedProducts(
    @Param("productId") UUID productId,
    @Param("categoryNames") List<String> categoryNames,
    @Param("targetPrice") BigDecimal targetPrice,
    @Param("limit") int limit
);
```

### 4. **Thêm Faceted Search**

**Priority: LOW**

```java
public class SearchResult {
    private Page<ProductDetail> products;
    private Map<String, Long> categoryFacets;
    private Map<String, Long> priceFacets;
    private Map<String, Long> brandFacets;
}

@Query(value = """
    SELECT c.category_name, COUNT(*) as count
    FROM products p
    JOIN product_categories pc ON p.product_id = pc.product_id
    JOIN categories c ON c.category_name = pc.category_name
    WHERE p.price BETWEEN :minPrice AND :maxPrice
    GROUP BY c.category_name
    """, nativeQuery = true)
List<Object[]> getCategoryFacets(@Param("minPrice") BigDecimal minPrice, 
                                  @Param("maxPrice") BigDecimal maxPrice);
```

### 5. **Cache Invalidation Strategy**

**Priority: HIGH**

```java
@Transactional
public Product updateProduct(UUID productId, ProductRequest productRequest) {
    Product updatedProduct = productRepository.save(productToUpdate);
    
    // Invalidate product detail cache
    productCacheManager.invalidateProductCache(productId);
    
    // Invalidate related products cache
    List<String> categoryNames = updatedProduct.getProductCategories()
        .stream()
        .map(pc -> pc.getCategories().getName())
        .collect(Collectors.toList());
    
    // Find all products in same categories and invalidate their related cache
    List<UUID> affectedProducts = productCategoryRepository
        .findProductIdsByCategoryNames(categoryNames);
    
    for (UUID affectedId : affectedProducts) {
        redisServiceWrapper.delete("related_products:" + affectedId);
    }
    
    return updatedProduct;
}
```

### 6. **Add Search Analytics**

**Priority: LOW**

```java
@Entity
public class SearchLog {
    @Id
    private UUID id;
    private String searchQuery;
    private Integer resultCount;
    private LocalDateTime searchedAt;
    private UUID userId;
}

// Track popular searches
@Scheduled(cron = "0 0 * * * *") // Every hour
public void updatePopularSearches() {
    List<String> popular = searchLogRepository.findTop10BySearchedAtAfter(
        LocalDateTime.now().minusDays(7)
    );
    redisServiceWrapper.setValue("popular_searches", popular);
}
```

---

## 📊 Performance Optimization

### 1. **Database Indexes**

```sql
-- Full-text search index
CREATE INDEX idx_product_name_gin ON products USING gin(to_tsvector('english', name));
CREATE INDEX idx_product_description_gin ON products USING gin(to_tsvector('english', description));

-- Price range queries
CREATE INDEX idx_product_price ON products(price);

-- Category filtering
CREATE INDEX idx_product_category ON product_categories(category_name, product_id);

-- Related products
CREATE INDEX idx_product_updated_at ON products(updated_at DESC);
```

### 2. **Query Optimization**

**Before:**
```java
// N+1 problem
Page<Product> products = productRepository.findAll(spec, pageable);
// Each product loads categories separately
```

**After:**
```java
@Query("SELECT DISTINCT p FROM Product p " +
       "LEFT JOIN FETCH p.productCategories pc " +
       "LEFT JOIN FETCH pc.categories " +
       "WHERE ...")
Page<Product> findAllWithCategories(Specification<Product> spec, Pageable pageable);
```

### 3. **Redis Caching Strategy**

```java
// Multi-level cache
// L1: Product detail (1 hour)
// L2: Search results (15 minutes)
// L3: Related products (1 hour)

public Page<ProductDetail> search(Pageable pageable, Map<String, String> params) {
    String cacheKey = "search:" + generateCacheKey(params, pageable);
    
    // Try cache first
    Object cached = redisServiceWrapper.getValue(cacheKey);
    if (cached != null) {
        return (Page<ProductDetail>) cached;
    }
    
    // Execute search
    Page<ProductDetail> results = executeSearch(pageable, params);
    
    // Cache for 15 minutes
    redisServiceWrapper.setValue(cacheKey, results);
    redisServiceWrapper.setTimeout(cacheKey, 15, TimeUnit.MINUTES);
    
    return results;
}
```

---

## 🎯 Kết Luận

### Tình Trạng Hiện Tại
- ❌ **Không có API search nào hoạt động**
- ❌ **Thiếu 2 class quan trọng**: `SearchSpecification`, `SearchCriteria`
- ✅ **Có sẵn infrastructure**: JpaSpecificationExecutor, Redis, Async processing
- ✅ **Có related products API** (hoạt động tốt với caching)

### Ưu Tiên Triển Khai

**Phase 1: Basic Search (1-2 days)**
1. Tạo `SearchSpecification` và `SearchCriteria`
2. Uncomment và fix `/products/search` endpoint
3. Remove `@PreAuthorize` để customer có thể search
4. Test với các operators cơ bản

**Phase 2: Full-Text Search (3-5 days)**
1. Implement PostgreSQL full-text search
2. Add search result ranking
3. Add "did you mean?" suggestions
4. Add search analytics logging

**Phase 3: Advanced Features (1 week)**
1. Faceted search với aggregations
2. Smart related products với scoring
3. Cache invalidation strategy
4. Performance optimization với indexes

**Phase 4: Elasticsearch (Optional, 1-2 weeks)**
1. Setup Elasticsearch cluster
2. Sync products to ES
3. Implement advanced search features
4. Add autocomplete/suggestions

### Rủi Ro
- ⚠️ **High**: Search API bị disable → Frontend không thể search
- ⚠️ **Medium**: Missing classes → Không thể uncomment code
- ⚠️ **Low**: Performance issues với large dataset (chưa có indexes)

### Khuyến Nghị
**Nên triển khai Phase 1 ngay lập tức** để có basic search functionality. Đây là tính năng core của một e-commerce platform.
