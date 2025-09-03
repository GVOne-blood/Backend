# Sync & Async

## [1. Phân biệt synchronous vs asynchronous](#synchronous_vs_asynchronous)

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
- Các ứng dụng cần khả năng đáp ứng và phản hồi cao, các tác vụ tốn thời gian sẽ được chạy dưới nền để đảm bảo giao diện
  mượt mà
- Xử lý song song các tác vụ độc lập, giảm đáng kế thời gian khi xử lý tuần tự

## Synchronous vs Asynchronous

| Tiêu chí                              | Synchronous (Đồng bộ)                                                                              | Asynchronous (Bất đồng bộ)                                                                                          |
|:--------------------------------------|:---------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------|
| **Luồng thực thi**                    | **Tuần tự (Sequential)**. <br> Tác vụ B chỉ bắt đầu sau khi tác vụ A hoàn thành.                   | **Song song (Concurrent)**. <br> Tác vụ B có thể bắt đầu và chạy song song trong khi tác vụ A đang xử lý.           |
| **Hành vi cốt lõi**                   | **Blocking (Bị chặn)**. <br> Luồng gọi phải đứng chờ cho đến khi tác vụ được gọi trả về kết quả.   | **Non-Blocking (Không bị chặn)**. <br> Luồng gọi tiếp tục công việc khác ngay sau khi nộp tác vụ.                   |
| **Hiệu suất & Tài nguyên**            | Lãng phí tài nguyên CPU trong khi chờ I/O. Cần nhiều luồng để xử lý nhiều yêu cầu đồng thời.       | Tận dụng hiệu quả tài nguyên CPU. Một số ít luồng có thể xử lý một số lượng lớn các yêu cầu đồng thời.              |
| **Độ phức tạp**                       | <span style="color:green">**Đơn giản**</span>. <br> Luồng mã lệnh tuần tự, dễ viết, đọc và gỡ lỗi. | <span style="color:orange">**Phức tạp**</span>. <br> Khó lập luận về luồng điều khiển, xử lý lỗi và gỡ lỗi.         |
| **Cơ chế xử lý kết quả**              | Kết quả được trả về trực tiếp từ lời gọi phương thức.                                              | Kết quả được xử lý sau này thông qua các cơ chế như **Callback**, **`Future`**, **`Promise`**, hoặc **Event Loop**. |
| **Khả năng đáp ứng (Responsiveness)** | **Thấp**. <br> Nếu luồng chính (UI thread) bị chặn, toàn bộ ứng dụng sẽ bị "đóng băng".            | **Cao**. <br> Luồng chính không bao giờ bị chặn, đảm bảo trải nghiệm người dùng mượt mà.                            |
| **Trường hợp sử dụng**                | ✅ Các tác vụ ngắn, phụ thuộc lẫn nhau.<br>✅ Các script đơn giản, logic nghiệp vụ tuần tự.          | ✅ Các tác vụ I/O kéo dài (mạng, file, database).<br>✅ Các ứng dụng cần khả năng mở rộng và đáp ứng cao.             |

## Đặt vấn đề

### Race Condition

> Race Condition xảy ra khi nhiều thread cùng truy cập và thao tác trên một tài nguyên được chia sẻ như biến, đối tượng,
> file,... dẫn đến kết quả của chương trình bị phụ thuộc vào thứ tự thực hiện thao tác của các luồng

Tại sao lại xảy ra Race Condition ?

Bản chất đa luồng là việc CPU thực hiện Concurrency rồi Context Switch trên 1 Core CPU duy nhất và nhanh đến mức ta cảm
tưởng các luồng đang chạy trên nhiều luồng của CPU
Context Switch sẽ lưu trạng thái hiện tại của luồng (biến, giá trị này kia,...). Cơ chế nào mà luồng đang chạy mà lại có
luồng khác xen vào thì là do thằng lập lịch của CPU
Lưu xong thì nó cho phép luồng kia vào chạy, chính vì vậy 1 phần đã sinh ra Race Condition và cả Memory Visibility

Ví dụ :

```

public class Code {

    private static int cnt = 0;

    public static void main(String[] args) throws InterruptedException {
        int numThreads = 200;
        int incrementsPerThread = 100;

        List<Thread> threads = new ArrayList<>(numThreads);

        for (int i = 0; i < numThreads; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    cnt++; // Chỉ tăng thôi để thấy rõ lỗi
                }
            });
            threads.add(thread);
            thread.start();
        }

        // Đảm bảo luồng main chờ TẤT CẢ các luồng con chạy xong
        for (Thread t : threads) {
            t.join();
        }

        System.out.println("Expected count: " + (numThreads * incrementsPerThread));
        System.out.println("Actual count  : " + cnt);
    }
}
```

Chúng ta mong đợi khi biến cnt chạy qua 200 thread với mỗi lần chạy qua 1 thread, giá trị cnt được cộng thêm 100. Kết
quả đúng sẽ là : **20.000**

Chạy chương trình ta thu được:

```
Expected count: 20000
Actual count  : 19900 //wrong

```

Về cơ bản phép tính `cnt += 100` được xử lý trong **1 THREAD** như sau:

1. Đọc giá trị hiện tại của cnt ở RAM vào thanh ghi CPU
2. Tăng giá trị cnt
3. Ghi giá trị mới của cnt vào RAM

Trong multithread, ví dụ 200 threads, rủi ro sẽ xuất hiện:

`cnt = 0`

T1. Thread 1 => Lấy giá trị cnt từ RAM vào register

T1. Thread 2 => Lấy giá trị cnt từ RAM vào register

T2. Thread 1 => Tăng giá trị cnt (`cnt = 100`)

T2. Thread 2 => Tăng giá trị cnt (`cnt = 100`)

T3. Thread 1 & Thread 2 cùng trả về cho RAM `cnt = 100` - điều đáng lý là sai khi phải là `cnt = 200` vì đã chạy qua 2
luồng

Điều này gây ra **lỗi logic nghiêm trọng** trong các tác vụ đa luồng phức tạp hơn

`synchronized` sẽ tạo ra 1 critical section. Đây như một khóa, chỉ có duy nhất 1 thread vào được và khi vào, nó khiến
các hành động đọc, thay đổi, ghi (như ví dụ trên) không thể bị xen ngang.

### Memory Visibility

CPU hiện nay có Cache riêng, với hiệu năng ngon hơn RAM rất nhiều, nên nhiều khi khi thay đổi giá trị của biến hay thực
hiện tác vụ nào đó, nó thực hiện luôn trong cache, sau đó mới trả giá trị mới vêf cho RAM, dẫn đến việc có khoảng thời
gian RAM không hề biết về sự thay đổi này, khi một luồng khác cần đến, nó sẽ lấy giá trị **CŨ** của biến, tác vụ trong
RAM và vấn đề về logic xuất hiện

```

class StopThread {
    private static boolean stopRequested = false;

    public static void main(String[] args) throws InterruptedException {

        Thread backgroundThread = new Thread(() -> {
            int i = 0;
            while (!stopRequested) { // Luồng này liên tục kiểm tra biến stopRequested
                i++;
            }
            System.out.println("Background thread stopped.");
        });
            backgroundThread.start();

            Thread.sleep(1000); // Chờ 1 giây

            System.out.println("Requesting stop...");
            stopRequested = true; // Luồng main thay đổi giá trị
            backgroundThread.join();
            // System.out.println("Stopped");
    }
}
```

Trong thread `backgroundThread`, vòng `while` chỉ dừng lại khi `stopRequests = true`, điều mà sau khi chạy thread **1s**
ta đã gán `stopRequests = true`

Kết quả về lý thuyết : Vòng lặp sẽ dừng sau 1s và Print "BackgroupThread stopped"

Kết quả thực tế : Lặp vô tận

Việc này chứng minh logic gán `stopRequests = true` được thực hiện trong cache, và vòng lặp while mỗi lần lặp lại kiểm
tra giá trị của `stopRequests` ở trong RAM, nơi mà `stopRequests = false`

`synchronized` cũng hỗ trợ giải quyết vấn đề này, nhưng `volatile` có độ phức tạp thấp hơn

#### Synchronized

> synchronized là cơ chế đồng bộ hóa cốt lõi và lâu đời của Java. Nó ra đời để giải quyết 2 vấn đề chính về Tính nguyên
> tử (Automic)  & Điều kiện tranh đua (Race Condition) và Tính tường minh của bộ nhớ (Memory Visibility)

Cú pháp

Với các vấn đề về Race Condition, sync dùng cơ chế lock và blocking đã được đề cập đến ở phần trên.
Với Visibility, giải pháp của sync là thiết lập mối quan hệ trước sau:

- Khi một luồng thoát khỏi một khối synchronized, tất cả các thay đổi mà nó đã thực hiện sẽ được đẩy (flush) ra bộ nhớ
  chính (RAM).

- Khi một luồng khác vào cùng một khối synchronized đó, nó sẽ đọc lại (refresh) các biến từ bộ nhớ chính.

Điều này đảm bảo rằng các thay đổi được thực hiện bởi một luồng sẽ được các luồng khác nhìn thấy một cách nhất quán.

#### Volatile

> Volatile là một modifier cho biến, nó đảm bảo rằng
>> - Mọi thao tác ghi vào biến volatile sẽ được ghi thẳng vào RAM ngay lập tức
>> - Mọi thao tác đọc từ biến Volatile sẽ được đọc thẳng từ RAM

Cú pháp

```
public volatile static <variable>;
```

`synchronized` tốn chi phí hơn `volatile` ở việc vận hành cơ chế lock và khả năng blocking của nó

### Chú thích