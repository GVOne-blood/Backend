# Static & Final
## [1. Thế nào là static ? Phương thức, thuộc tính khai báo bằng từ khóa static được sử dụng khi nào ? Làm thế nào để truy cập được tới phương thức, thuộc tính static](#static)
## 2. Thế nào là final ? Khai báo 1 biến final khác gì với static, biến khai báo bằng final có thể chỉnh sửa được không ? 

[chú thích]()
---
### Static 
> Trong Java, từ khóa static được dùng để quản lý bộ nhớ. Về cơ bản, static có nghĩa là một thành phần (biến, phương thức,...) thuộc về lớp (class), chứ không thuộc về một đối tượng (instance) cụ thể nào.

Ta có một lớp Student (Sinh viên):

Mỗi sinh viên có name (tên) và id (mã số) riêng. Đây là các thành phần non-static (instance members) vì chúng thuộc về từng đối tượng sinh viên.
Tuy nhiên, tất cả sinh viên đều học chung một trường, ví dụ "Đại học Bách Khoa". Tên trường này có thể được xem là một thuộc tính chung cho cả lớp, ta có thể dùng static để gán giá trị mặc định cho tên trường. 

Phạm vi: Static có thể áp dụng cho 4 loại thành phần trong Java: 

- Biến / Thuộc tính
- Hàm / Phương thức
- Khối mã
- Lớp lồng nhau(Nested classes)

Các thành phần static được khởi tạo khi thành phần bao bọc nó (nếu có) được nạp vào bộ nhớ

![static](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/Screenshot%202025-08-25%20231509.png)

Lưu ý việc xử dụng method static : 
- Phương thức static chỉ có thể truy cập trực tiếp các thành phần static khác (biến static, phương thức static).

- Nó không thể truy cập các thành phần non-static (biến instance, phương thức instance) một cách trực tiếp.

- Nó không thể sử dụng từ khóa this hoặc super, vì this tham chiếu đến đối tượng hiện tại, mà phương thức static không gắn với đối tượng nào cả.

---

### Final
>Từ khóa final trong Java là một công cụ sửa đổi (modifier) có thể được áp dụng cho biến, phương thức và lớp. Ý nghĩa cốt lõi của final là "không thể thay đổi". Tuy nhiên, "không thể thay đổi" có ý nghĩa khác nhau tùy thuộc vào ngữ cảnh nó được sử dụng

Phạm vi: 

- Biến / Thuộc tính
- Hàm / Phương thức
- Lớp

Thành phần có final không thể bị ghi đè bởi một biến hay phương thức hay lớp khác

Lưu ý rằng **một biến kiểu Object vẫn có thể thay đổi được giá trị nội tại của nó dù có final**, bản chất từ khóa final chỉ làm cho tham chiếu của biến đó immutable - nghĩa là không thể trỏ 1 biến tham chiếu khác vào biến này

![objject with final](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/Screenshot%202025-08-25%20233641.png)

Khi kết hợp 1 biến giữa static và final, ta có một hằng số (constant)
