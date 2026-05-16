# Thông Tin Đăng Nhập - Nhân Viên Shop

## Tổng Quan

Hệ thống đã tạo **91 tài khoản nhân viên** cho 27 shop (mỗi shop có 2-5 nhân viên).

## Thông Tin Đăng Nhập

### Định Dạng Email
```
employee{số}@springfood.vn
```

### Mật Khẩu (Chung cho tất cả nhân viên)
```
Employee123!
```

### Danh Sách Email
- employee1@springfood.vn
- employee2@springfood.vn
- employee3@springfood.vn
- ...
- employee91@springfood.vn

## Vai Trò Nhân Viên

Mỗi shop có các vai trò sau (ngẫu nhiên 2-5 vai trò):

| Vai Trò | Phòng Ban | Lương Cơ Bản | Hoa Hồng |
|---------|-----------|--------------|----------|
| **Manager** | Management | 15M - 25M VND | 5% - 10% |
| **Staff** | Operations | 8M - 12M VND | 2% - 5% |
| **Cashier** | Finance | 7M - 10M VND | 1% - 3% |
| **Delivery** | Logistics | 6M - 9M VND | 3% - 6% |

## Thông Tin Bổ Sung

### Trạng Thái
- Tất cả nhân viên có status: `ACTIVE`
- Email verified: `true`
- Phone verified: `true`

### Liên Kết
- Mỗi nhân viên được gán vào 1 shop cụ thể (field `shop_id` trong bảng `user`)
- Thông tin chi tiết về vai trò, lương, ca làm việc được lưu trong bảng `shop_members`

### Authentication Role
- Tất cả nhân viên shop có role `STAFF` trong hệ thống authentication
- Vai trò cụ thể (Manager, Cashier, etc.) được lưu trong bảng `shop_members.role_name`

## Ví Dụ Đăng Nhập

```json
{
  "email": "employee1@springfood.vn",
  "password": "Employee123!"
}
```

## Kiểm Tra Dữ Liệu

### Query để xem tất cả nhân viên
```sql
SELECT 
  u.email,
  u.first_name,
  u.last_name,
  s.shop_name,
  sm.role_name,
  sm.department,
  sm.base_salary,
  sm.work_schedule
FROM springfood_authentication.user u
JOIN springfood_shop.shop_members sm ON u.user_id = sm.user_id
JOIN springfood_shop.shops s ON sm.shop_id = s.shop_id
ORDER BY s.shop_name, sm.role_name;
```

### Query để xem nhân viên của 1 shop cụ thể
```sql
SELECT 
  u.email,
  u.first_name || ' ' || u.last_name as full_name,
  sm.role_name,
  sm.department,
  sm.base_salary,
  sm.commission,
  sm.work_schedule,
  sm.join_date
FROM springfood_authentication.user u
JOIN springfood_shop.shop_members sm ON u.user_id = sm.user_id
WHERE sm.shop_id = 'YOUR_SHOP_ID_HERE'
ORDER BY sm.role_name;
```

## Lưu Ý Bảo Mật

⚠️ **QUAN TRỌNG**: Đây là dữ liệu seed cho môi trường development/testing.

- **KHÔNG** sử dụng mật khẩu này trong production
- **PHẢI** thay đổi mật khẩu sau lần đăng nhập đầu tiên trong production
- **NÊN** implement password reset flow cho production environment
