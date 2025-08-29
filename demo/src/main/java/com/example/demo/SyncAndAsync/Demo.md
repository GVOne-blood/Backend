# Sync & Async

## 1. Phân biệt synchronous vs asynchronous
## 2. Phân biệt trường hợp sử dụng, ưu nhược điểm của async và sync
## 3. Tìm hiểu từ khóa : synchronized trong java

[Chú thích](#chú-thích)

## Sync

Trường hợp sử dụng
- Các tác vụ phụ thuộc chặt chẽ với nhau và tuần tự 
- Các tác vụ mà CPU không có thời gian nghỉ ngơi 
- Các script cơ bản, tác vụ Batch, CLI tools,...
- Quá trình khởi tạo ứng dụng
...

Ưu điểm 
- Code dễ viết, dễ đọc, dễ debug
- Dễ quản lý luồng dữ liệu

Nhược điểm
- Hiệu năng kém với nhiều tác vụ phải chờ đợi
- Khả năng mở rộng kém vì bản chất các task phụ thuộc vào task trước đó
- Nếu luồng chính thực hiện tác vụ đồng bộ kéo dài, UI đóng băng sẽ gây UX tệ cho người dùng

## Async

Trường hợp sử dụng
- Các tác vụ nặng về IO vì bản chất các tác vụ này cần chờ phản hồi từ bên thứ 3
- Các ứng dụng cần khả năng đáp ứng và phản hồi cao, các tác vụ tốn thời gian sẽ được chạy dưới nền để đảm bảo giao diện mượt mà
- Xử lý song song các tác vụ độc lập, giảm đáng kế thời gian khi xử lý tuần tự




## Synchronous vs Asynchronous


| Tiêu chí             | Synchronous (Đồng bộ)                                                                          | Asynchronous (Bất đồng bộ)                                                                                 |
| :-------------------- | :--------------------------------------------------------------------------------------------- | :--------------------------------------------------------------------------------------------------------- |
| **Luồng thực thi**    | **Tuần tự (Sequential)**. <br> Tác vụ B chỉ bắt đầu sau khi tác vụ A hoàn thành.                | **Song song (Concurrent)**. <br> Tác vụ B có thể bắt đầu và chạy song song trong khi tác vụ A đang xử lý.      |
| **Hành vi cốt lõi**   | **Blocking (Bị chặn)**. <br> Luồng gọi phải đứng chờ cho đến khi tác vụ được gọi trả về kết quả. | **Non-Blocking (Không bị chặn)**. <br> Luồng gọi tiếp tục công việc khác ngay sau khi nộp tác vụ.           |
| **Hiệu suất & Tài nguyên** | Lãng phí tài nguyên CPU trong khi chờ I/O. Cần nhiều luồng để xử lý nhiều yêu cầu đồng thời.    | Tận dụng hiệu quả tài nguyên CPU. Một số ít luồng có thể xử lý một số lượng lớn các yêu cầu đồng thời.      |
| **Độ phức tạp**       | <span style="color:green">**Đơn giản**</span>. <br> Luồng mã lệnh tuần tự, dễ viết, đọc và gỡ lỗi. | <span style="color:orange">**Phức tạp**</span>. <br> Khó lập luận về luồng điều khiển, xử lý lỗi và gỡ lỗi. |
| **Cơ chế xử lý kết quả** | Kết quả được trả về trực tiếp từ lời gọi phương thức.                                           | Kết quả được xử lý sau này thông qua các cơ chế như **Callback**, **`Future`**, **`Promise`**, hoặc **Event Loop**. |
| **Khả năng đáp ứng (Responsiveness)** | **Thấp**. <br> Nếu luồng chính (UI thread) bị chặn, toàn bộ ứng dụng sẽ bị "đóng băng". | **Cao**. <br> Luồng chính không bao giờ bị chặn, đảm bảo trải nghiệm người dùng mượt mà.                    |
| **Trường hợp sử dụng** | ✅ Các tác vụ ngắn, phụ thuộc lẫn nhau.<br>✅ Các script đơn giản, logic nghiệp vụ tuần tự.      | ✅ Các tác vụ I/O kéo dài (mạng, file, database).<br>✅ Các ứng dụng cần khả năng mở rộng và đáp ứng cao. |


### Chú thích