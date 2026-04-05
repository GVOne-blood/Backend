


**Ví dụ thực tế:**
```
Cart của user có:
- 2 sản phẩm từ Shop A
- 3 sản phẩm từ Shop B
→ Khi checkout tạo 2 orders riêng biệt
```

### B. Product Availability & Stock
```java
// CartItem cần thêm:
private Integer availableStock;  // Số lượng còn trong kho (snapshot)
private Boolean isAvailable;     // Sản phẩm còn bán không
private String unavailableReason; // Lý do không khả dụng (nếu có)
```

**Lý do:**
- User thêm vào giỏ hôm nay, checkout ngày mai → sản phẩm có thể hết hàng
- Frontend cần hiển thị warning nếu stock < quantity
- Prevent checkout nếu sản phẩm không còn available

### C. Pricing & Promotions
```java
// CartItem cần thêm:
private BigDecimal originalPrice;    // Giá gốc
private BigDecimal discountAmount;   // Số tiền giảm
private BigDecimal finalPrice;       // Giá sau giảm (price * quantity - discount)
private String promotionId;          // ID chương trình khuyến mãi (nếu có)
private String promotionName;        // Tên khuyến mãi
```

**Lý do:**
- Frontend cần hiển thị giá gốc và giá sale
- Tính toán discount cho từng item
- Track promotion để validate khi checkout

### D. Product Variants/Attributes
```java
// CartItem cần thêm:
private Map<String, String> attributes; // VD: {"Color": "Red", "Size": "XL"}
// HOẶC
private String variantName;  // VD: "Màu Đỏ - Size XL"
```

**Lý do:**
- Product có nhiều biến thể (màu sắc, kích thước)
- Frontend cần hiển thị variant user đã chọn
- Validate variant còn tồn tại khi checkout

### E. Selection & Actions
```java
// CartItem cần thêm:
private Boolean selected = true;  // User có chọn item này để checkout không
```

**Lý do:**
- User có thể chọn một số items để checkout, bỏ qua items khác
- Frontend cần checkbox để select/deselect
- Tính totalPrice chỉ với selected items

## 2. Cart Level - Cần bổ sung

### A. Shop Grouping
```java
// Cart cần thêm method helper:
public Map<UUID, List<CartItem>> groupByShop() {
    return items.stream()
        .collect(Collectors.groupingBy(CartItem::getShopId));
}
```

### B. Selection Summary
```java
// Cart cần thêm:
private Integer selectedItemsCount;      // Số items được chọn
private BigDecimal selectedItemsTotal;   // Tổng tiền items được chọn
```

### C. Validation Flags
```java
// Cart cần thêm:
private Boolean hasUnavailableItems;  // Có item nào không available
private Boolean hasInsufficientStock; // Có item nào vượt quá stock
private LocalDateTime lastValidated;  // Lần cuối validate với product service
```

## 3. Response DTO cho Frontend

### CartResponse
```java
@Data
public class CartResponse {
    private String userId;
    private BigDecimal totalPrice;
    private BigDecimal selectedTotal;  // Tổng tiền items được chọn
    private Integer totalItems;
    private Integer selectedItems;
    
    // Group items by shop
    private List<ShopCartGroup> shopGroups;
    
    // Validation warnings
    private List<String> warnings;  // VD: "Sản phẩm X đã hết hàng"
    private Boolean canCheckout;    // Có thể checkout không
    
    private LocalDateTime updatedAt;
}

@Data
public class ShopCartGroup {
    private UUID shopId;
    private String shopName;
    private String shopAvatar;
    private List<CartItemResponse> items;
    private BigDecimal shopTotal;  // Tổng tiền items của shop này
    private Integer itemCount;
}

@Data
public class CartItemResponse {
    private String sku;
    private UUID productId;
    private String productName;
    private String productImage;
    
    // Shop info
    private UUID shopId;
    private String shopName;
    
    // Pricing
    private BigDecimal originalPrice;
    private BigDecimal price;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;
    private String promotionName;
    
    // Quantity & Stock
    private Integer quantity;
    private Integer availableStock;
    private Boolean hasEnoughStock;  // quantity <= availableStock
    
    // Availability
    private Boolean isAvailable;
    private String unavailableReason;
    
    // Variant
    private String variantName;
    private Map<String, String> attributes;
    
    // Selection
    private Boolean selected;
    
    private LocalDateTime addedAt;
}
```

## 4. Business Logic cần implement

### A. Add to Cart
```java
// Khi add item:
1. Validate product tồn tại (gọi product-service)
2. Validate shop tồn tại (gọi shop-service)
3. Check stock availability
4. Get current price & promotions
5. Snapshot product info (name, image, price, shop)
6. Merge nếu item đã tồn tại (cùng SKU)
```

### B. Update Quantity
```java
// Khi update quantity:
1. Validate quantity > 0
2. Check stock availability
3. Update finalPrice = price * quantity - discount
4. Recalculate cart totals
```

### C. Validate Cart (trước checkout)
```java
// Validate toàn bộ cart:
1. Check tất cả products còn available
2. Check stock đủ cho quantity
3. Validate prices (có thể đã thay đổi)
4. Check promotions còn valid
5. Return warnings nếu có vấn đề
```

### D. Select/Deselect Items
```java
// Toggle selection:
1. Update item.selected
2. Recalculate selectedItemsCount
3. Recalculate selectedItemsTotal
```

## 5. Frontend Requirements

### Cart Page cần hiển thị:

```
┌─────────────────────────────────────┐
│ Giỏ hàng của bạn (5 sản phẩm)      │
├─────────────────────────────────────┤
│ ☑ Shop A                            │
│   ☑ [IMG] Sản phẩm 1                │
│       Màu: Đỏ, Size: XL             │
│       ₫100,000 → ₫80,000 (-20%)     │
│       Số lượng: [- 2 +] (Còn 50)    │
│   ☑ [IMG] Sản phẩm 2                │
│       ₫50,000                        │
│       Số lượng: [- 1 +] (Còn 10)    │
│   Tổng Shop A: ₫210,000             │
├─────────────────────────────────────┤
│ ☑ Shop B                            │
│   ☐ [IMG] Sản phẩm 3 (Hết hàng)    │
│       ⚠️ Sản phẩm tạm hết hàng      │
│   Tổng Shop B: ₫0                   │
├─────────────────────────────────────┤
│ Tổng cộng (3 sản phẩm): ₫210,000   │
│ [Mua hàng]                          │
└─────────────────────────────────────┘
```

### Features cần có:
1. **Checkbox** để select/deselect items và shops
2. **Group by shop** với tên shop và tổng tiền mỗi shop
3. **Stock warning** khi quantity > availableStock
4. **Unavailable badge** cho items không còn bán
5. **Price display**: Giá gốc, giá sale, % giảm
6. **Variant info**: Hiển thị màu sắc, size đã chọn
7. **Quantity controls**: +/- với validation
8. **Remove button** cho từng item
9. **Total calculation**: Chỉ tính selected items
10. **Checkout button**: Disable nếu có warnings

## 6. API Endpoints cần có

```
GET    /api/cart              - Get user cart
POST   /api/cart/items        - Add item to cart
PUT    /api/cart/items/{sku}  - Update item quantity
DELETE /api/cart/items/{sku}  - Remove item
PATCH  /api/cart/items/{sku}/select - Toggle selection
POST   /api/cart/validate     - Validate cart before checkout
DELETE /api/cart              - Clear cart
POST   /api/cart/sync         - Sync cart with latest product data
```

## 7. Tóm tắt - Cần làm gì

### Priority 1 (CRITICAL):
- ✅ Thêm `shopId`, `shopName` vào CartItem
- ✅ Thêm `availableStock`, `isAvailable` vào CartItem
- ✅ Thêm `selected` flag vào CartItem
- ✅ Tạo CartResponse DTO với shop grouping

### Priority 2 (HIGH):
- ✅ Thêm pricing fields (originalPrice, discountAmount, finalPrice)
- ✅ Thêm variant/attributes info
- ✅ Implement validate cart logic
- ✅ Implement sync cart with product service

### Priority 3 (MEDIUM):
- ✅ Thêm promotion tracking
- ✅ Implement selection logic
- ✅ Add validation warnings

### Optional (NICE TO HAVE):
- Saved for later items
- Recently viewed products
- Recommended products based on cart
- Cart abandonment tracking
