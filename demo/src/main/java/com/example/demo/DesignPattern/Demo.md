# Design Pattern

## 1. Tìm hiểu về mục đích, cách thức triển khai, ưu điểm của:

## [Singleton](#singleton)

## Factory method

## Saga pattern

[Chú thích](#chú-thích)

### Chú thích

## Singleton Pattern

> Singleton thuộc nhóm Creational Patterns, nó đảm bảo rằng chỉ một lớp chỉ có duy nhất một instance và cung cấp một
> điểm truy cập toàn cục đến instance đó
>> Nguyên tắc triển khai:
>>> - **Private Constructor**: Ngăn chặn việc khởi tạo đối tượng từ bên ngoài lớp bằng toán tử new.
>>> - **Private Static Instance Variable**: Một biến static để lưu trữ thể hiện duy nhất của lớp.
>>> - **Public Static Method (getInstance())**: Một phương thức static công khai để làm điểm truy cập toàn cục. Phương
      thức này sẽ chịu trách nhiệm khởi tạo đối tượng (nếu chưa có) và trả về nó.

Mục đích

Triển khai Singleton trong Java:

- Eager Initialization : Đối tượng được khởi tạo ngay khi class được nạp, an toàn trong multithread nhưng gây lãng phí
  tài nguyên nếu đối tượng không được dùng tới

![eager](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/250.png)

- Lazy Initialization : Đối tượng chỉ được tạo khi getInstance() được gọi lần đầu tiên, cũng thread-safe nhưng lại có
  sync

![lazy](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/251.png)

- Double Check Locking : một kỹ thuật giảm tác động của sync

![double check](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/252.png)

- Bill Pugh Initialization : Lớp static bên trong chỉ được nạp khi gọi đến getInstance() lần đầu tiên nên khá toàn diện

![bill pugh](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/255.png)

Ưu điểm

- Đảm bảo tính nhất quán: chỉ có một instance nên những thay đổi sẽ được phản ánh ngay lên trên toàn bộ ứng dụng
- Tối ưu hóa tài nguyên và hiệu năng: Cũng chính vì chỉ có 1 instance nên singleton đảm bảo các đối tượng nặng (file
  config, connection,...) chỉ khởi tạo 1 lần duy nhất. Ngoài ra việc hỗ trợ Lazy Init - đối tượng chỉ được khởi tạo khi
  được yêu cầu nên thời gian khởi động ứng dụng cũng được cải thiện
- Truy cập toàn cục: Vì cung cấp 1 public static method nên singleton có thể được truy cập ở bất kỳ đâu trong ứng dụng

## Factory method

> Factory Method là một mẫu thiết kế thuộc nhóm Creational Patterns. Nó định nghĩa một interface để tạo ra các đối
> tượng, nhưng để các lớp con quyết định lớp cụ thể nào sẽ được khởi tạo. Factory Method cho phép một lớp ủy thác việc
> khởi tạo đối tượng cho các lớp con của nó.
>> Nguyên tắc triển khai:
>>> - Một interface hoặc abstract class cho các đối tượng mà factory method sẽ tạo ra (Product)
>>> - Các class là thể hiện của các đối tượng sẽ implements interface hoặc abstract (Concrete Product)
>>> - Khai báo 1 phương thức trừu tượng trả về 1 đối tượng kiểu Product (Creator)
>>> - Method ghi đè factory method để trả về 1 thể hiện của 1 ConcreteProduct cụ thể (Concrete Creator)

Mục đích

![eg](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/407.png)

Ưu điểm

- Tăng tính mở rộng: Đôi khi ta không biết một lớp cha có thể có thêm lớp con hay không, khi mở rộng chỉ cần thêm class
  implements product và thêm case 1 lần duy nhất
- Giảm sự phụ thuộc giữa các module: bản chất Factory là cô lập sự phụ thuộc vào một chỗ, là loose coupling
- Che giấu thông tin khởi tạo
- Dễ quản lý

## Saga pattern

Trong kiến trúc ứng dụng nguyên khối, transaction được xử lý trên 1 DB duy nhất, tuân thủ theo nguyên tắc ACID, điều này
đảm bảo tính nhất quán cao cẩu dữ liệu

Trong microservice,mỗi service lại có nhiều instance, mỗi service lại có một DB riêng, các transaction chỉ có thể đúng
trên 1 DB, vì thế việc dùng `@Transactional` trên microservice là không khả thi khi logic bị tách biệt giữa các service

Saga pattern ra đời để cung cấp cơ chế giải quyết vấn đề này
> Một saga là một chuỗi các giao dịch cục bộ (Local transaction), mỗi giao dịch cục bộ là một loạt các hành động được
> thực hiện trong 1 service duy nhất. Với mỗi giao dịch cục bộ có khả năng thay đổi dữ liệu, phải tồn tại 1 giao dịch bù
> trừ tương ứng (Ví dụ có giao dịch thanh toán đơn hàng thì Rollback của nó là hoàn tiền khi đơn hàng không thành công
> đến
> tay user)
> Như vậy có thể hiểu giao dịch bù trừ là một hành động để hoàn tác lại những gì mà giao dịch cục bộ đã làm

Nếu một bước tỏng saga thất bại, nó sẽ thực hiện các giao dịch bù trừ theo thứ tự ngược lại từ chỗ nó thất bại về trước
để data được rollback về dạng ban đầu
Phân loại saga :

### Choreography Saga (Saga base on event)

> Mô hình này không có một service trung tâm, các service giao tiếp với nhau một cách phi tập trung thông qua việc phát
> và lắng nghe sự kiện

Luồng hoạt động chính :

* Happy case:

1. Service bắt đầu luồng thực hiện 1 giao dịch cục bộ để cập nhật/tạo mới data, sau khi giao dịch thành công, nó phát ra
   một sự kiện gửi lên một Message Broker như Kafka, RabitMQ,...
2. Service tiếp theo trong chuỗi nghiệp vụ (đã được đăng ký để lắng nghe sự kiện của message broker) sẽ hứng sự kiện và
   sẽ thực hiện giao dịch cục bộ của mình, nếu giao dịch thành công, nó lại phát ra 1 sự kiện gửi lên MB
3. Quy trình sẽ lặp lại tương tự cho đến khi service đích COMMIT giao dịch cục bộ cuả mình và gửi sự kiện cuối cùng

* Unhappy case:

1. Service đang thực hiện transaction thì gặp lỗi, nó sẽ bắn một sự kiện báo thất bại lên MQ
2. Các service đã đăng ký và ở đăng trước service này sẽ nhận được message và tiến hành giao dịch bù trừ để rollback

Ưu điểm :

- Loose Coupling : Các service chỉ cần gửi và lắng nghe event, không cần quan tâm thằng nào sẽ nghe event đó, tất cả
  event được chạy trên 1 pipeline
- Không có điểm lỗi đơn : Vì không có service nào trực tiếp điều phối, nên khi một service hỏng thì rollback hoặc tìm
  instance khác thôi chứ không hỏng toàn hệ thống giao dịch (nhưng con kafka hỏng thì lại là chuyện khác)
- Dễ dàng mở rộng : Khi cần thêm 1 service mới vào chuỗi nghiệp vụ, chỉ cần đăng ký lắng nghe event và triển khai giao
  dịch cục bộ của mình

### Orchestration Saga (Saga base on choreography)

> Trong mô hình này, 1 service trung tâm sẽ thực hiện điều phối (Orchestrator). Nó chịu trách nhiệm quản lý toàn bộ
> luồng Saga, ra lệnh cho các service khác và xử lý phản hồi

Luồng hoạt động:

* Happy case:

1. Service bắt đầu giao dịch sẽ tạo 1 Saga Orchestrator(Có thể là 1 service riêng hoặc 1 component trong service đó)
2. Orchestrator bắt đầu saga, nó gửi yêu cầu thực hiện giao dịch đến service đầu tiên, service nhận lệnh, thực hiện gioa
   dịch cục bộ của mình
3. Sau khi thực hiện xong giao dịch, service tạo 1 tín hiệu phản hồi và gửi nó về Orchestrator, orches lúc này nhận phản
   hồi và cập nhật trạng thái saga
4. Orchestrator tiếp tục gửi yêu cầu đến service tiếp theo trong chuỗi giao dịch, cứ thế cho đến khi nhận được thông báo
   thành công của service cuối cùng

* Unhappy case:

1. Một service gửi về phản hồi không thực hiện thành công giao dịch cục bộ, orchestrator sẽ biết được những service nào
   đã thực hiện thành công ở đằng trước.
2. Orchestrator bắt đầu quá trình bù trừ bằng cách gửi yêu cầu thực hiện rollback đến các service đã thành công trước đó
3. Các service nhận lệnh và rollback đến khi roll đến service khởi đầu giao dịch

Ưu điểm :

- Logic tập trung và rõ ràng : Toàn bộ quy trình nghiệp vụ,xử lý lỗi và ra lệnh đều tập trung tại 1 orchestrator, dễ
  quản lý và gỡ lỗi
- Các service chỉ cần cung cấp các api để thực thi lệnh hoặc bù trừ
- Tránh được việc service lắng nghe sự kiện của nhau tạo thành 1 vòng loop
- Orchestrator có thể lưu trạng thái của saga vào CSDL, dễ dàng theo dõi và phục hồi nếu có sự cố

Trường hợp sử dụng :

- Quy trình nghiệp vụ phức tạp, nhiều bước



