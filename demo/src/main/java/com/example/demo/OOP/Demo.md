# Objecct-oriented programming

## 1. [Nêu ra các tính chất quan trọng của hướng đối tượng](#tính-đóng-gói-encapsulation)
## 2. [Access modifier trong java có những loại nào ? Nêu đặc điểm của từng loại](#1-access-modifiers)
## 3. Phân biệt class và instance
## 4. Phân biệt Abstract và Interface , Nêu trường hợp sử dụng cụ thể. Nếu 2 interface hoặc 1 abstract và 1 interface có 1 function cùng tên, có thể cùng hoặc khác kiểu trả về cùng được kế thừa bởi một class, chuyện gì sẽ xảy ra?
## 5. Thế nào là Overriding và Overloading
## 6. Một function có access modifier là private or static có thể overriding được không?
## 7. Một phương thức final có thể kế thừa được không ?
## 8. Phân biệt hai từ khóa This và Super

[Chú thích](#chú-thích)
---
Trong Java, cần khẳng định OOP không phải là một danh sách các "tính năng" (như class, extends, private). Đó là một mô hình tư duy để giải quyết sự phức tạp trong việc xây dựng phần mềm. Nguồn gốc của nó (từ Simula và Smalltalk) là nỗ lực mô phỏng thế giới thực, nơi các thực thể độc lập (đối tượng) có trạng thái và hành vi riêng, tương tác với nhau thông qua các thông điệp. Mục tiêu cuối cùng là quản lý sự phụ thuộc (dependency management) và kiểm soát sự phức tạp (complexity control) khi hệ thống phát triển.

## Class
> Class đóng nhiều vai trò cùng lúc. Nó là một thành phần của hệ thống kiểu, cũng là nơi để chưa đựng hành vi và là một metadata cho JVM
## Tính đóng gói (Encapsulation)
>Một module có thể che giấu những quyết định thiết kế có thể thay đổi trong tương lai, thông tin cần che giấu (đóng gói) không chỉ là dữ liệu, mà còn là sự tồn tại của nó, cấu trúc và thuật toán xử lý nó. 

Mục đích: 

- Che giấu thông tin, ngăn chặn việc truy cập trực tiếp đối tượng từ bên ngoài, đảm bảo tính vẹn toàn của đối tượng
- Dễ bảo trì và nâng cấp khi thay đổi cấu trúc bên trong module

### 1. Access Modifiers
>>Cấp độ truy cập là công cụ thiết kế cơ bản, quyết định điều gì là công khai và điều gì cần che giấu với các mức độ khác nhau

Phân loại:
- **public**: Phạm vi truy cập rộng nhất (toàn project). Public là "cánh cửa" duy nhất mà bên ngoài nên sử dụng để tương tác với một đối tượng đóng gói, biến hay phương thức, class public ít thay đổi một cách tùy tiện. Tuy nhiên, việc lạm dụng public có thể phơi bày chi tiết triển khai của đối tượng và tạo ra tight coupling
- **protected**: Phạm vi truy cập bởi các lớp trong cùng package và các subclass ngay cả khi chúng ở khác package. Ứng dụng kinh điển của nó là triển khai chi tiết hành vi của lớp cha (hành vi của lớp cha là khung). Tuy nhiên tight coupling, bản chất yếu hơn private khiến protected cần sử dụng thận trọng
- **default**: Phạm vi truy cập là các lớp khác trong package. Ứng dụng mạnh với package-private - cho phép xây dựng tập hợp các lớp liên kết chặt chẽ với nhau mà không cần phơi bày các tương tác nội bộ của chúng ra bên ngoài
- **private**: Phạm vi truy cập trong lớp đó, lớp con cũng không thể truy cập trực tiếp. Vì không phụ thuộc nên thành phần private có thể tự do thay đổi mà không phá vỡ các phần khác của hệ thống.
## Tính kế thừa (Inheritance)
>Kế thừa cho phép một lớp con mới thừa hưởng lại thuộc tính và phương thức của lớp đã tồn tại (super-class). Mối quan hệ này được gọi là IS-A

Mục đích:
- Tính tái sử dụng cao: không cần viết lại các thuộc tính và phương thức của lớp cha
- Cấu trúc, tổ chức dễ dàng: Kế thừa tạo ra một hệ thống cây phân cấp tự nhiên giữa các lớp, việc quản lý và hiểu chương trình cao hơn

Có 2 loại kế thừa: 
- Kế thừa interface (một lớp implements 1 interface): Là nền tảng cho tính đa hình và loose coupling (thiết kế lỏng lẻo)
- Kế thừa triển khai (một lớp extends từ 1 lớp khác): Tái sử dụng mã nguồn, nhưng sự thay đổi lớp cha có thể phá vỡ lớp con, đồng thời tạo ra liên kết chặt chẽ giữa 2 lớp (tight coupling)

Nguyên lý thiết kế hiện đại luôn ưu tiên HAS-A hơn do sự linh hoạt, cho phép thay đổi hành vi lúc chạy và tránh được hệ thống phân cấp phức tạp
## Tính đa hình (Encapsulation)
>Đa hình cho phép một đối tượng có thể thực hiện một hành động theo nhiều cách khác nhau, thể hiện qua **Overloading** và **Overriding**

Mục đích:
- Cung cấp khả năng mở rộng chương trình 

Đa hình về mặt kiến trúc là cơ chế cho phép sự đảo ngược phụ thuộc (Dependency Inversion Principle - DIP). Giữa một module cấp cao và một module cấp thấp, module cao tương tác với một interface trừu tượng, lúc runtime, module cấp thấp sẽ được "tiêm" (inject) vào để thực hiện hành vi. Đây là cơ chế cốt lõi của Dependency Injection - trái tim của Spring framework.
## Tính trừu tượng (Abstraction)
>Trừu tượng là quá trình ẩn đi những chi tiết triển khai phức tạp và chỉ hiển thị chức năng cần thiết, nó tập trung vào "what" thay vì "how"

Mục đích:
- Đơn giản hóa sự phức tạp: Việc chia một hệ thống thành các phần nhỏ giúp ta dễ hiểu và dễ quản lý
- Linh hoạt: Cho phép thay đổi cách thức hoạt động bên trong 1 hệ thống mà không ảnh hưởng đến các thành phần khác sử dụng nó
- Cung cấp một bộ quy tắc chung cho các lớp thực thi phải tuân theo
 

## Tính toàn năng của OOP
OOP không toàn năng, một số hạn chế phổ biến như:
- Mutable State in multithread: Cốt lõi OOP là các đối tượng đóng gói trạng thái và thay đổi nó qua thời gian. Trong môi trường đa luồng, các vấn đề như race-conditions, deadlocks,...OOP không thể giải quyết triệt để so với Lập trình Hàm (Functional Programming - FP)
- Object-Relational Impedance Mismatch: Mô hình đối tượng trong bộ nhớ là một đồ thị các đối tượng liên kết với nhau, trong khi mô hình dữ  l iệu trong RDB là các bảng 2 chiều. Sự khác biệt này dẫn đến sự ra đời của các công cụ như ORM (Hibernate, JPA) để chuyển đổi qua lại
- Unnecessary Complexity: OOP có xu hướng mô hình hóa mọi thứ thành object dẫn đến việc các tác vụ đơn giản cũng được đối tượng hóa gây rườm rà. Lambda, Stream API là các yếu tố lập trình hàm được thêm để giải quyết vấn đề này.


### Chú thích
