# What, how, why Role-Base Access Controller

## Tổng quan

> RBAC là một mô hình kiểm soát truy cập nơi quyền hạn được gán cho các roles (vai trò), và người dùng được gán vào các
> vai trò đó

Về cơ bản, RBAC có thêm bảng roles nằm giữa users và permission để giảm thiểu mối quan hệ giữa 2 bảng này khi không tận
dụng được tình trình hợp dữ liệu về permission.
Luồng logic : `User` => Gán `Roles` => `Roles` đã chứa sẵn `Permission`

RBAC giải quyết các vấn đề thực tế phát sinh :

- Đơn giản hóa quản lý và tăng khả năng mở rộng : Trong một hệ thống thông thường, việc phân quyền cho 1000 user với 100
  quyền khác nhau sẽ tạo nên 1 độ phức tạp lớn, thay vào đó, RBAC đưa ra `Roles`. Với 10 Roles, ta chỉ cần quản lý 1000
  user với 100 quyền thông qua 10 roles, mỗi roles chứa các permission nhất định
- Đảm bảo bảo mật và chính xác : Phân quyền theo Roles khiến 1 user chỉ có các quyền hạn tối thiểu cần có để thực hiện
  công việc của mình => tăng tính bảo mật
- Dễ dàng Auditing & Compliance : Thay vì kiểm soát hàng ngàn user với các permission, ta chỉ cần xem role nào có các
  permission đó sau đó lấy ra user
- Tách biệt logic nghiệp vụ khỏi logic phân quyền