# Shop Schema - Tổng Quan Dữ Liệu Seed

## File SQL
`02_springfood_shop_seed_data.sql`

## Tổng Quan

File SQL này chứa dữ liệu seed cho **3 bảng** trong schema `springfood_shop`:

| Bảng | Số Bản Ghi | Mô Tả |
|------|------------|-------|
| **shops** | 27 | Thông tin các shop/cửa hàng |
| **shop_members** | 91 | Nhân viên của các shop (2-5 nhân viên/shop) |
| **shop_wallets** | 27 | Ví điện tử của mỗi shop (1 ví/shop) |

**Tổng cộng**: 145 records

## Chi Tiết Các Bảng

### 1. shops (27 records)

Danh sách 27 shop bao gồm các thương hiệu nổi tiếng:

**Đồ uống:**
- Gong Cha, The Coffee House, Highlands Coffee
- Phúc Long Coffee & Tea, Ding Tea, TocoToco
- Mixue, Cà Phê Cộng

**Món chính:**
- Phở Hà Nội 24h, Phở Bò Tái Lăn, Bún Chả Hà Nội
- Bún Bò Huế Mẹ Liên, Phở Gà Tân Định
- Cơm Tấm Sài Gòn, Cơm Niêu Singapore, Cơm Gà Xối Mỡ Hội An

**Món ăn vặt:**
- Bánh Mì Hòa Mã, Chè Thái Lan, Kem Tràng Tiền

**Món nướng:**
- Quán Nướng Sài Gòn, BBQ Garden
- Hải Sản Nướng Biển Đông, Nem Nướng Nha Trang

**Món khác:**
- Bánh Xèo Miền Tây, Lẩu Thái Hải Sản
- Xôi Xéo Hà Nội, Cháo Lòng Bà Hoa

**Thông tin shop:**
- Status: Tất cả `ACTIVE`
- Avg star: 3.5 - 5.0
- Địa chỉ: Phân bố ở TP.HCM, Hà Nội, Đà Nẵng, Nha Trang, Huế, Cần Thơ
- Email: `{shopname}@springfood.vn`
- Phone: Số điện thoại Việt Nam thực tế

### 2. shop_members (91 records)

**Phân bố nhân viên:**
- Mỗi shop có 2-5 nhân viên
- Tổng cộng 91 nhân viên cho 27 shop

**Vai trò nhân viên:**

| Vai Trò | Phòng Ban | Lương Cơ Bản (VND) | Hoa Hồng |
|---------|-----------|-------------------|----------|
| Manager | Management | 15M - 25M | 5% - 10% |
| Staff | Operations | 8M - 12M | 2% - 5% |
| Cashier | Finance | 7M - 10M | 1% - 3% |
| Delivery | Logistics | 6M - 9M | 3% - 6% |

**Thông tin bổ sung:**
- Join date: Ngẫu nhiên trong 2 năm qua
- Status: Tất cả `ACTIVE`
- Work schedule: 5 loại ca làm việc khác nhau
- Salary type: `MONTHLY`

**Tài khoản nhân viên:**
- 91 tài khoản user mới được tạo
- Email: `employee{1-91}@springfood.vn`
- Password: `Employee123!`
- Role: `STAFF` (trong authentication system)
- Mỗi nhân viên được link với 1 shop cụ thể

### 3. shop_wallets (27 records)

**Mỗi shop có 1 ví điện tử:**

| Field | Giá Trị | Mô Tả |
|-------|---------|-------|
| balance | 0 - 50,000,000 VND | Số dư khả dụng |
| pending_amount | 0 - 5,000,000 VND | Số tiền đang chờ xử lý |
| locked_amount | 0 - 1,000,000 VND | Số tiền bị khóa |

**Mục đích:**
- Quản lý doanh thu từ đơn hàng
- Theo dõi số dư và giao dịch
- Xử lý thanh toán cho shop

## Quan Hệ Giữa Các Bảng

```
shops (27)
  ├── shop_members (91) - FK: shop_id
  │   └── user (91 new employees) - FK: user_id
  └── shop_wallets (27) - FK: shop_id
```

## Cập Nhật Authentication Schema

File `01_springfood_authentication_seed_data.sql` đã được cập nhật:

**Trước:**
- 10 users (1 admin, 2 shop_owner, 3 staff, 4 customer)

**Sau:**
- 101 users (10 original + 91 employees)
- 101 user-role assignments

**Nhân viên mới:**
- Email: employee1@springfood.vn đến employee91@springfood.vn
- Password: Employee123!
- Role: STAFF
- Mỗi nhân viên có shop_id link đến shop của họ

## Cách Sử Dụng

### 1. Cleanup (nếu cần)
```sql
-- Chạy cleanup script trước
\i 00_cleanup_all_data.sql
```

### 2. Import dữ liệu
```sql
-- Import authentication data (bao gồm 91 employee users)
\i 01_springfood_authentication_seed_data.sql

-- Import shop data (shops, members, wallets)
\i 02_springfood_shop_seed_data.sql
```

### 3. Verify
```sql
-- Kiểm tra số lượng records
SELECT 'shops' as table_name, COUNT(*) FROM springfood_shop.shops
UNION ALL
SELECT 'shop_members', COUNT(*) FROM springfood_shop.shop_members
UNION ALL
SELECT 'shop_wallets', COUNT(*) FROM springfood_shop.shop_wallets
UNION ALL
SELECT 'employee_users', COUNT(*) FROM springfood_authentication.user 
WHERE email LIKE 'employee%@springfood.vn';
```

## Queries Hữu Ích

### Xem nhân viên của 1 shop
```sql
SELECT 
  s.shop_name,
  u.email,
  u.first_name || ' ' || u.last_name as full_name,
  sm.role_name,
  sm.department,
  sm.base_salary,
  sm.commission,
  sm.work_schedule
FROM springfood_shop.shop_members sm
JOIN springfood_authentication.user u ON sm.user_id = u.user_id
JOIN springfood_shop.shops s ON sm.shop_id = s.shop_id
WHERE s.shop_name = 'Gong Cha'
ORDER BY sm.role_name;
```

### Xem ví của các shop
```sql
SELECT 
  s.shop_name,
  sw.balance,
  sw.pending_amount,
  sw.locked_amount,
  (sw.balance + sw.pending_amount + sw.locked_amount) as total_amount
FROM springfood_shop.shop_wallets sw
JOIN springfood_shop.shops s ON sw.shop_id = s.shop_id
ORDER BY sw.balance DESC;
```

### Thống kê nhân viên theo vai trò
```sql
SELECT 
  role_name,
  COUNT(*) as count,
  AVG(base_salary) as avg_salary,
  AVG(commission) as avg_commission
FROM springfood_shop.shop_members
GROUP BY role_name
ORDER BY avg_salary DESC;
```

### Xem shop có nhiều nhân viên nhất
```sql
SELECT 
  s.shop_name,
  COUNT(sm.shop_member_id) as employee_count,
  STRING_AGG(sm.role_name, ', ') as roles
FROM springfood_shop.shops s
LEFT JOIN springfood_shop.shop_members sm ON s.shop_id = sm.shop_id
GROUP BY s.shop_id, s.shop_name
ORDER BY employee_count DESC;
```

## Lưu Ý

1. **Foreign Keys**: shop_members.user_id phải tồn tại trong authentication.user
2. **Shop Assignment**: Mỗi employee user có shop_id trong bảng user
3. **Role Mapping**: 
   - Authentication role: `STAFF` (cho tất cả nhân viên)
   - Shop role: `Manager`, `Staff`, `Cashier`, `Delivery` (trong shop_members)
4. **Passwords**: Tất cả employee passwords là `Employee123!` (BCrypt hashed)
5. **Status**: Tất cả records có status `ACTIVE`

## Tài Liệu Liên Quan

- [EMPLOYEE-CREDENTIALS.md](./EMPLOYEE-CREDENTIALS.md) - Thông tin đăng nhập nhân viên
- [HUONG-DAN-CHAY-LAI.md](./HUONG-DAN-CHAY-LAI.md) - Hướng dẫn chạy lại seed data
- [00_cleanup_all_data.sql](./00_cleanup_all_data.sql) - Script cleanup
