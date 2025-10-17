# Kafka

## Message queue là gì ? Ưu nhược điểm khi sử dụng message queue ? Khi nào thì cần dùng tới message queue

## Kafka : Yêu cầu tìm hiểu các khái niềm về: message, broker, produce, consumer, group, topic, partition.

[Chú thích](#chú-thích)

### Chú thích

## Kafka

> Kafka là 1 nền tảng truyền tải sự kiện phân tán, tích hợp của 1 hệ thống hàng đợi tin nhắn (message queue), 1 hệ thống
> lưu trữ
> phân tán (distributed
> log
> system) và 1 hệ thống xử lý luồng dữ liệu (stream processing system).

### Kiến trúc

#### Mô hình

Kafka hoạt động dựa trên mô hình publish-subscribe(pub-sub), trong đó các ứng dụng(service) bên producer sẽ gửi các
record hay message vào các topic, sau đó các consumer sẽ lấy và sử dụng chúng. Producer và Consumer hoàn toàn tách biệt
thông qua 1 hàng đợi trung gian là Kafka Broker.
Vì thế nên chúng có thể hoạt động độc lập và bâst đồng bộ với nhau.

#### Kiểu dữ liệu lưu trữ

Kiểu dữ liệu lưu trữ trong Kafka là commit-log - nó là 1 record nhật ký có thứ tự, chỉ cho phép ghi nối tiếp (append),
không cho ghi đè lên các sự kiện đã tồn tại. Các log trong Kafka là bất biến (immutable), không thể thay đổi hay xóa bỏ.

Mỗi bản ghi trong 1 partition được gán 1 mã định dang unique là offset - đóng vai trò như 1 con trỏ (hay 1 dấu trang),
cho phép consumer
theo dõi vị trí của mình trong partition. Nó đề phòng khi consumer gặp sự cố không đọc được, sau khi khởi động lại, nó
có thể
tiếp tục đọc từ offset cuối cùng đã đọc, tránh bị đọc lại hoặc bỏ sót data.

Việc tuân theo commit-log khiến kafka có hiệu suất cao ngay cả khi data được ghi trên đĩa, đơn giản vì I/O tuần tự sẽ
nhanh hơn I/O ngẫu nhiên. Tất nhiên việc message không bién mất sau khi được lấy ra như thằng RabitMQ sẽ cho kafka khả
năng backup dữ liệu cực mạnh.

#### Thành phần chính

- **Event/Message/Record** : Là đơn vị dữ liệu cơ bản của Kafka, bao gồm 1 key, 1 value, 1 timestamp và các header tùy
  chỉnh. Các key và val đều được lưu dưới dạng mảng byte, cho phép convert sang nhiều kiểu dữ liệu khác nhau
  Ví dụ :
    ```kafka
    {
      key: byte[],
      value: byte[],
      timestamp: long,
      headers: Map<String, byte[]>
    }
  {
    key: "userId",
    value: "{name: 'John', age: 30}",
    timestamp: 1633024800000,
    headers: {"source": "web"}
    }
    ```

- **Producer** : Là các ứng dụng client chịu trách nhiệm tạo và gửi các message vào các topic của kafka. Nó có thể quyết
  định message sẽ đi vào topic nào và chỉ định partition mục tiêu

- **Consumer** : Là các ứng dụng client đọc hoặc đăng ký nhận các luồng sự kiện từ các topic. Consumer có thể hoạt động
  độc lập hoặc theo nhóm (consumer group) để chia sẻ tải công việc và tăng khả năng mở rộng
- **Broker** : Là các server trong 1 cụm kafka, chịu trách nhiệm nhận, lưu trữ dữ liệu, xử lý yêu cầu đọc ghi từ client
  và quản lý việc sao chép dữ liệu giữa các partition. Mỗi broker có 1 ID duy nhất trong cụm
- **Cluster** : Là tập hợp các broker hoạt động cùng nhau để tăng khả năng chịu lỗi và mở rộng. Cluster có thể bao gồm
  từ vài
  broker đến hàng trăm broker. Nói chung là mọi thằng đều dùng cluster
- **Topic** : Là 1 danh mục logic để tổ chức và phân loại các message trong kafka. Mỗi topic có thể có nhiều partition
  để tăng
  khả năng song
  song và khả năng chịu lỗi. Nói chung nó là một thư mục để chứa các message
- **Partition** : Là các phân đoạn vật lý của 1 topic, mỗi partition là 1 commit-log độc lập, có thứ tự riêng, mỗi
  message trong partition có thứ tự nghiêm ngặt, nhưng các partition với nhau thì không có thứ tự. Mỗi partition có thể
  được lưu trữ trên các broker khác nhau để tăng khả năng mở rộng và chịu lỗi
- **Consumer Group** : Là 1 tập hợp các consumer hoạt động cùng nhau để đọc các message từ 1 hoặc nhiều topic. Mỗi
  consumer trong nhóm sẽ đọc các partition khác nhau của topic, đảm bảo rằng mỗi message chỉ được xử lý bởi 1 consumer
  trong nhóm. Từ đây sẽ có các kịch bản có thể xảy ra :
    - Nếu số consumer < số partition : Mỗi consumer sẽ đọc nhiều partition
    - Nếu số consumer = số partition : Mỗi consumer sẽ đọc 1 partition - đạt được max hiệu suất
    - Nếu số consumer > số partition : Một số consumer sẽ không đọc được partition nào, sẽ sinh ra những thằng consumer
      nhàn rỗi
- **Leader và Follower** : Mỗi partition có 1 broker đóng vai trò là leader, chịu trách nhiệm xử lý tất cả các yêu cầu
  đọc
  ghi từ client. Các broker khác đóng vai trò là follower, sao chép dữ liệu từ leader để đảm bảo tính sẵn sàng và chịu
  lỗi. Nếu
  leader gặp sự cố, 1 follower sẽ được bầu làm leader mới
- **Replication** : Là cơ chế sao chép dữ liệu giữa các broker để tăng tính sẵn sàng và chịu lỗi. Mỗi partition có thể
  có nhiều bản sao (replica) được lưu trữ trên các broker khác nhau. Khi 1 broker bị lỗi, các bản sao vẫn có thể phục vụ
  các yêu cầu đọc ghi
- **Retention** : Là chính sách lưu trữ dữ liệu trong kafka. Kafka cho phép cấu hình thời gian hoặc kích thước lưu trữ
  dữ liệu
  trong các topic. Khi dữ liệu vượt quá giới hạn này, các message cũ sẽ bị xóa bỏ để giải phóng không gian lưu trữ
- **Offset** : Là mã định danh duy nhất cho mỗi message trong 1 partition, nó đại diện cho vị trí của message trong
  chuỗi. Offset được sử dụng bởi consumer để theo dõi tiến trình đọc của mình
- **Zookeeper** : Là 1 dịch vụ quản lý cấu hình và điều phối phân tán, được sử dụng bởi kafka để theo dõi trạng thái của
  các broker, quản lý việc bầu chọn leader cho các partition. Ngày nay, kafka đã phát triển để không cần zookeeper nữa

Nếu một message có key muốn được đẩy vào, trình phân vùng mặc định của kafka sẽ dùng hàm hash trên key đó để xác định
partition
mà message sẽ được gửi đến. Điều này đảm bảo rằng tất cả các message với cùng một key sẽ luôn được gửi đến cùng một
partition, giữ nguyên thứ tự của chúng.

Nếu message không có key, trình phân vùng dùng các cơ chế như round-robin hoặc sticky để phân phối chúng

Khi số lượng consumer thay đổi (có thể consumer chết hoặc thêm mới), kafka sẽ rebalancing lại các partition cho các
consumer còn lại

#### Kafka Zookeeper và kẻ thay thế vĩ đại - KRaft

> Zookeeper là một dịch vụ quản lý cấu hình và điều phối phân tán, được sử dụng bởi Kafka để theo dõi trạng thái của các
> broker, quản lý việc bầu chọn leader cho các partition.

Zookeeper có trách nhiệm :

- Bầu cử 1 broker làm leader trong cluster
- Quản lý các followers trong cluster
- Cấu hình topic
- Kiểm soát danh sách truy cập

Tuy nhiên, việc sử dụng Zookeeper cũng mang lại một số thách thức như :

- Tăng độ phức tạp của hệ thống : Cần phải triển khai và quản lý một cụm Zookeeper riêng biệt
- Quá trình chuyển đổi leader có thể mất thời gian, ảnh hưởng đến hiệu suất của Kafka

Để giải quyết các vấn đề này, KRaft ra đời
> KRaft (Kafka Raft Metadata mode) là một chế độ mới trong Kafka, cho phép Kafka hoạt động mà không cần Zookeeper. KRaft
> sử dụng giao thức Raft để quản lý metadata và điều phối phân tán, giúp đơn giản hóa kiến trúc của Kafka và cải thiện
> hiệu suất.

Kiến trúc của KRaft hoạt động như sau :

- Một tập hợp con các brokers được chỉ định làm Controller
- Metadata không còn được lưu trữ bên ngoài mà lưu ngay trong kafka, trong 1 topic nội bộ có tên là __
  cluster_metadata (metadata log)
- Kiến trúc mới này sử dụng EventSourcing, controller đang hoạt động ghi các thay đổi dữ liệu vào log, các controller
  khác sẽ đọc log này để cập nhật trạng thái của mình

Ưu điểm :

- Không phải quản lý 2 hệ thống riêng biệt như trước
- Mở rộng hơn so với Zoo
- Quá trình truyền ngôi diễn ra nhanh vì chỉ cần cập nhật trong nội bộ kafka qua topic __cluster_metadata
- Giảm độ phức tạp của hệ thống

### Kafka streams

> Kafka Stream là 1 thư viện client (không phải cluster) để xây dựng các ứng dụng xử lý dữ liệu theo thời gian thực

Nguyên tắc cốt lõi là đọc dữ liệu từ các topic kafka đầu vào, áp dụng các phép biến đổi, ghi kết quả vào topic kafka đầu
ra,

## Microservice với Kafka
