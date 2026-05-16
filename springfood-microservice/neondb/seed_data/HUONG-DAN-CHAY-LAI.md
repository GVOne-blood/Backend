# Hướng Dẫn Chạy Lại Seed Data

## ⚠️ Vấn Đề

Bạn đã chạy file SQL cũ (thiếu 3 fields: `wholesale_price`, `total_feedbacks`, `average_rating`).

Nếu chạy lại file SQL mới trực tiếp, nó sẽ **KHÔNG GHI ĐÈ** vì có `ON CONFLICT DO NOTHING` → Dữ liệu cũ vẫn còn trong database.

## ✅ Giải Pháp: 2 Bước

### Bước 1: XÓA dữ liệu cũ

Chạy script cleanup trước:

```bash
psql "postgresql://neondb_owner:..." -f 00_cleanup_all_data.sql
```

Hoặc trong NeonDB SQL Editor:
1. Mở file `00_cleanup_all_data.sql`
2. Copy toàn bộ nội dung
3. Paste vào SQL Editor và chạy

**Script này sẽ xóa TẤT CẢ dữ liệu seed theo thứ tự ngược (Level 7 → Level 1) để tránh lỗi foreign key.**

### Bước 2: Chạy lại các file seed mới

Sau khi xóa xong, chạy lại theo thứ tự:

```bash
psql "postgresql://neondb_owner:..." -f 01_springfood_authentication_seed_data.sql
psql "postgresql://neondb_owner:..." -f 02_springfood_shop_seed_data.sql
psql "postgresql://neondb_owner:..." -f 03_springfood_product_seed_data.sql
psql "postgresql://neondb_owner:..." -f 04_springfood_order_seed_data.sql
```

Hoặc chạy master script:

```bash
psql "postgresql://neondb_owner:..." -f run_all_seeds.sql
```

## 🔍 Kiểm Tra Sau Khi Chạy

Uncomment các dòng cuối trong `00_cleanup_all_data.sql` để kiểm tra số lượng records:

```sql
SELECT 'products' as table_name, COUNT(*) as count 
FROM springfood_product.products;
```

Kết quả mong đợi: **165 products**

Kiểm tra 3 fields mới đã có dữ liệu:

```sql
SELECT 
  product_id,
  name,
  price,
  wholesale_price,    -- ✅ Phải có giá trị (70-90% của price)
  avg_rate,
  average_rating,     -- ✅ Phải có giá trị (= avg_rate)
  total_feedbacks     -- ✅ Phải có giá trị (0-100)
FROM springfood_product.products
LIMIT 5;
```

## 📝 Lưu Ý

- **QUAN TRỌNG**: Phải chạy cleanup TRƯỚC, rồi mới chạy seed data mới
- Cleanup script xóa theo thứ tự ngược để tránh lỗi foreign key
- Nếu bạn chỉ muốn xóa products thôi, có thể chỉnh sửa script cleanup (comment các dòng DELETE khác)
- Sau khi chạy xong, dữ liệu mới sẽ có đầy đủ 3 fields

## 🚀 Nhanh Nhất (NeonDB SQL Editor)

1. Copy nội dung `00_cleanup_all_data.sql` → Paste vào SQL Editor → Run
2. Copy nội dung `01_springfood_authentication_seed_data.sql` → Run
3. Copy nội dung `02_springfood_shop_seed_data.sql` → Run
4. Copy nội dung `03_springfood_product_seed_data.sql` → Run
5. Copy nội dung `04_springfood_order_seed_data.sql` → Run

Xong! ✅
