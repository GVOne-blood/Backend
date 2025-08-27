# Design Pattern 

## 1. Tìm hiểu về mục đích, cách thức triển khai, ưu điểm của:
## [Singleton](#singleton)
## Factory method

[Chú thích](#chú-thích)
### Chú thích

## Singleton Pattern
> Singleton thuộc nhóm Creational Patterns, nó đảm bảo rằng chỉ một lớp chỉ có duy nhất một instance và cung cấp một điểm truy cập toàn cục đến instance đó
>> Nguyên tắc triển khai:
>>> - **Private Constructor**: Ngăn chặn việc khởi tạo đối tượng từ bên ngoài lớp bằng toán tử new.
>>> - **Private Static Instance Variable**: Một biến static để lưu trữ thể hiện duy nhất của lớp.
>>> - **Public Static Method (getInstance())**: Một phương thức static công khai để làm điểm truy cập toàn cục. Phương thức này sẽ chịu trách nhiệm khởi tạo đối tượng (nếu chưa có) và trả về nó.

Mục đích 

Triển khai Singleton trong Java:
- Eager Initialization : Đối tượng được khởi tạo ngay khi class được nạp, an toàn trong multithread nhưng gây lãng phí tài nguyên nếu đối tượng không được dùng tới

![eager](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/250.png)

- Lazy Initialization : Đối tượng chỉ được tạo khi getInstance() được gọi lần đầu tiên, cũng thread-safe nhưng lại có sync 

![lazy](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/251.png)

- Double Check Locking : một kỹ thuật giảm tác động của sync

![double check](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/252.png)

- Bill Pugh Initialization : Lớp static bên trong chỉ được nạp khi gọi đến getInstance() lần đầu tiên nên khá toàn diện 

![bill pugh](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/255.png)

Ưu điểm 
- Đảm bảo tính nhất quán: chỉ có một instance nên những thay đổi sẽ được phản ánh ngay lên trên toàn bộ ứng dụng
- Tối ưu hóa tài nguyên và hiệu năng: Cũng chính vì chỉ có 1 instance nên singleton đảm bảo các đối tượng nặng (file config, connection,...) chỉ khởi tạo 1 lần duy nhất. Ngoài ra việc hỗ trợ Lazy Init - đối tượng chỉ được khởi tạo khi được yêu cầu nên thời gian khởi động ứng dụng cũng được cải thiện
- Truy cập toàn cục: Vì cung cấp 1 public static method nên singleton có thể được truy cập ở bất kỳ đâu trong ứng dụng
## Factory method


