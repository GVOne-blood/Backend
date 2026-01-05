# Product Data Import Guide

## 📦 Tổng quan

Script import **110+ sản phẩm thực tế** về đồ ăn, nước uống Việt Nam với **ảnh thật** từ Unsplash (miễn phí, chất lượng cao).

## 🍜 Danh mục sản phẩm (110+ món)

### Món Chính (Main Dishes) - 48 món
- **Cơm** (5 món): Cơm tấm, cơm gà, cơm chiên, cơm sườn...
- **Phở** (5 món): Phở bò tái, phở gà, phở đặc biệt...
- **Bún** (5 món): Bún bò Huế, bún chả, bún riêu...
- **Bánh Mì** (5 món): Bánh mì thịt, pate, xíu mại, trứng...
- **Mì & Mỳ** (10 món): Mì xào, mỳ Ý, ramen, udon, mì Hàn...
- **Lẩu** (8 món): Lẩu Thái, lẩu bò, lẩu hải sản, lẩu kim chi...
- **Món Đường Phố** (10 món): Hủ tiếu, bánh xèo, bánh cuốn, mì Quảng...

### Đồ Uống (Beverages) - 31 món
- **Cà Phê** (6 món): Đen, sữa, bạc xỉu, trứng, cappuccino, latte
- **Trà Sữa** (5 món): Trân châu đen, matcha, ô long, Thái, socola
- **Sinh Tố** (5 món): Bơ, dâu, xoài, sapoche, dứa
- **Nước Ép** (5 món): Cam, dưa hấu, ổi, cà rốt, táo
- **Đồ Uống Đặc Sản** (10 món): Trà đá, nước mía, sữa đậu nành, chanh dây...

### Đồ Ăn Nhanh (Fast Food) - 10 món
Burger bò, burger gà, pizza, gà rán, sandwich, hot dog, khoai tây chiên...

### Món Ăn Vặt (Snacks) - 6 món
Chả giò, nem chua rán, bánh tráng trộn, gỏi cuốn, bánh bao chiên, xúc xích

### Tráng Miệng (Desserts) - 16 món
- **Chè** (6 món): Chè Thái, khúc bạch, chè bưởi, sương sa...
- **Bánh Ngọt** (10 món): Tiramisu, cheesecake, croissant, macaron, donut...

## 📸 Nguồn ảnh

Tất cả ảnh từ **Unsplash** - miễn phí, chất lượng cao, không cần attribution:
- Ảnh thật về đồ ăn Việt Nam
- Độ phân giải cao (800px+)
- Không watermark
- Sử dụng thương mại được

## 🚀 Cách import

### Bước 1: Đảm bảo Docker đang chạy

```bash
docker ps
```

Phải thấy container `postgres` đang running.

### Bước 2: Chạy script import

**Windows:**
```bash
import-product-data.bat
```

**Linux/Mac:**
```bash
chmod +x import-product-data.sh
./import-product-data.sh
```

**Hoặc manual:**
```bash
docker exec -i postgres psql -U admin -d product_db < product-data-import.sql
```

### Bước 3: Verify

```sql
-- Connect to database
docker exec -it postgres psql -U admin -d product_db

-- Check products count
SELECT COUNT(*) FROM products;

-- Check categories
SELECT * FROM categories;

-- Check products by category
SELECT 
    c.category_name,
    COUNT(pc.product_id) as product_count
FROM categories c
LEFT JOIN product_categories pc ON c.category_name = pc.category_name
GROUP BY c.category_name;
```

## 📊 Database Schema

### Products Table
```sql
- product_id: UUID (PK)
- shop_id: UUID
- name: VARCHAR(255)
- sku: VARCHAR(100) UNIQUE
- description: TEXT
- product_status: VARCHAR(20) - AVAILABLE, OUT_OF_STOCK
- price: DECIMAL(15,2)
- wholesale_price: DECIMAL(15,2)
- quantity: INTEGER
- images: JSONB - Array of image URLs
- created_at, updated_at: TIMESTAMP
```

### Categories Table
```sql
- category_name: VARCHAR(100) (PK)
- slug: VARCHAR(100) UNIQUE
- description: TEXT
- is_active: BOOLEAN
- parent_id: VARCHAR(100) (FK to categories)
```

### Product_Categories Table (Many-to-Many)
```sql
- product_id: UUID (FK to products)
- category_name: VARCHAR(100) (FK to categories)
```

## 💰 Giá sản phẩm

Giá được set theo thị trường Việt Nam:
- **Món chính**: 30,000 - 75,000 VNĐ
- **Lẩu**: 180,000 - 320,000 VNĐ (2-4 người)
- **Đồ ăn nhanh**: 25,000 - 120,000 VNĐ
- **Cà phê**: 20,000 - 42,000 VNĐ
- **Trà sữa**: 30,000 - 40,000 VNĐ
- **Sinh tố**: 28,000 - 38,000 VNĐ
- **Nước ép**: 22,000 - 26,000 VNĐ
- **Đồ uống đặc sản**: 5,000 - 30,000 VNĐ
- **Món ăn vặt**: 5,000 - 15,000 VNĐ
- **Tráng miệng**: 15,000 - 45,000 VNĐ
- **Bánh ngọt**: 15,000 - 300,000 VNĐ

## 🔄 Update data

Nếu muốn update hoặc thêm sản phẩm:

1. Edit file `product-data-import.sql`
2. Chạy lại script import
3. Hoặc insert trực tiếp vào database

## 🧹 Clear data

Nếu muốn xóa hết và import lại:

```sql
-- Connect to database
docker exec -it postgres psql -U admin -d product_db

-- Clear all data
TRUNCATE TABLE product_categories, products, categories CASCADE;

-- Exit
\q

-- Re-import
import-product-data.bat
```

## 📝 Sample Product

```json
{
  "product_id": "uuid",
  "name": "Cơm Tấm Sườn Bì Chả",
  "sku": "COM-001",
  "description": "Cơm tấm truyền thống Sài Gòn với sườn nướng, bì, chả trứng",
  "price": 45000,
  "wholesale_price": 40000,
  "quantity": 100,
  "product_status": "AVAILABLE",
  "images": [
    "https://images.unsplash.com/photo-1626804475297-41608ea09aeb?w=800",
    "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=800"
  ],
  "categories": ["Cơm", "Món Chính"]
}
```

## 🎨 Frontend Integration

Products đã có ảnh thật, có thể hiển thị ngay:

```typescript
// Get products
this.productService.getProducts().subscribe(products => {
  products.forEach(product => {
    // Parse images from JSONB
    const images = JSON.parse(product.images);
    console.log('First image:', images[0]);
  });
});
```

## ⚠️ Lưu ý

1. **Images field là JSONB string**, cần parse:
   ```typescript
   const images = JSON.parse(product.images);
   ```

2. **Shop_id là random UUID**, trong production cần link đến shop thật

3. **Ảnh từ Unsplash** - Nếu muốn host riêng:
   - Download ảnh về
   - Upload lên MinIO
   - Update images field

4. **Category name là PK**, không phải UUID

## 🔗 Resources

- Unsplash: https://unsplash.com
- PostgreSQL Docs: https://www.postgresql.org/docs/
- Spring Data JPA: https://spring.io/projects/spring-data-jpa

## ✅ Checklist

- [x] 110+ products với data thực tế
- [x] 18 categories (6 main + 12 sub)
- [x] Ảnh thật từ Unsplash
- [x] Giá theo thị trường VN (5K - 320K)
- [x] Description chi tiết
- [x] SKU unique cho mỗi product
- [x] Quantity và wholesale price
- [x] Product-Category relationships
- [x] Verification queries với statistics
- [x] Đa dạng món ăn: Việt, Nhật, Hàn, Ý, Thái

## 🎉 Done!

Bây giờ bạn có thể:
1. ✅ Hiển thị products trên frontend
2. ✅ Filter theo category
3. ✅ Search products
4. ✅ Add to cart
5. ✅ Checkout

Happy coding! 🚀
