# Primitive & Object Data Tpye
## 1. Phân biệt kiểu dữ liệu nguyên thủy và kiểu dữ liệu object.
## 2. Có thể chuyển đổi giữa hai kiểu dữ liệu này không ?
## 3. Có thể so sánh hai kiểu dữ liệu này với nhau không?
## 4. Giá trị khi khởi tạo biến với hai loại kiểu dữ liệu này là gì?

---
### I. Lý thuyết 
Trong Java, hệ thống kiểu dữ liệu được phân chia thành hai loại chính: kiểu dữ liệu nguyên thủy (Primitive Data Types) và kiểu dữ liệu đối tượng (Object Data Types), hay còn gọi là kiểu tham chiếu (Reference Types). Sự phân biệt này là nền tảng cốt lõi, ảnh hưởng sâu sắc đến cách Java quản lý bộ nhớ, hiệu suất và bản chất của lập trình hướng đối tượng.
#### 1. Kiểu dữ liệu nguyên thủy (Primitive Data Type)
> Là kiểu dữ liệu cơ bản được định nghĩa sẵn trong ngôn ngữ Java, chúng không phải là Object nên cũng không có các phương thức đi kèm. Các biến kiểu Primitive lưu trữ trực tiếp giá trị của chúng trong bộ nhớ stack.
Primitive DT được chia thành 4 kiểu cơ bản:
- Kiểu số nguyên

    | Kiểu | Kích thước | Miền giá trị |
    |:-----:|:----:|:-----:|
    | byte | 8-bit | [-128, 127] |
    | short | 16-bit | [-32768, 32767] |
    | int | 32-bit | [-2e31, 2e31 - 1] |
    | long | 64-bit | [-2e63, 2e63 - 1] |
- Kiểu số thực 
    | Kiểu | Kích thước | Chuẩn |
    |:-----:|:----:|:-----:|
    | float | 32-bit | IEEE 754 (single accurate) |
    | double | 64-bit | IEEE 754 |
- Kiểu ký tự : Kích thước 16-bit, biểu diễn 1 ký tự unicode
- Kiểu logic : boolean - true;false, kích thước mặc định: 1bit or 1byte
---
#### 2. Kiểu dữ liệu tham chiếu (Reference/Object Data Type)
> Là kiểu dữ liệu có sẵn trong java, được thể hiện dưới dạng các Object, nó không lưu trữ trực tiếp đối tượng mà nó đại diện, thay vào đó nó lưu trữ 1 tham chiếu đến vị trí của đối tượng trong bộ nhớ heap. Tất cả các class, interface, enum, array,... đều thuộc kiểu dữ liệu này


