# Queue Interface
## [1. Nêu các đặc điểm của Queue Interface, Dequeue Interface](#queue)
## 2. Kể ra các class triển khai từ Queue Interface, Dequeue Interface , phân biệt trường hợp sử dụng tương ứng

[Chú thích](#chú-thích)
## Queue
> Queue là một cấu trúc dữ liệu tuân theo nguyên tắc FIFO (First In First Out), nghĩa là phần tử được thêm vào đầu tiên sẽ được lấy ra đầu tiên. Queue Interface trong Java cung cấp các phương thức để thêm, loại bỏ và kiểm tra các phần tử trong hàng đợi.


| Thao tác                           | Ném ra Ngoại lệ ⚠️ | Trả về Giá trị Đặc biệt ✅ | Mô tả chi tiết                                                                                                                                           |
|:-----------------------------------|:-------------------|:--------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Thêm phần tử**<br>(vào cuối)     | `add(e)`           | `offer(e)`                | Thêm một phần tử vào cuối hàng đợi. `add()` sẽ ném `IllegalStateException` nếu hàng đợi đầy (đối với hàng đợi có giới hạn). `offer()` sẽ trả về `false`. |
| **Lấy và Xóa**<br>(từ đầu)         | `remove()`         | `poll()`                  | Lấy và xóa phần tử ở đầu hàng đợi. `remove()` sẽ ném `NoSuchElementException` nếu hàng đợi rỗng. `poll()` sẽ trả về `null`.                              |
| **Chỉ Lấy**<br>(từ đầu, không xóa) | `element()`        | `peek()`                  | Lấy phần tử ở đầu hàng đợi nhưng không xóa nó. `element()` sẽ ném `NoSuchElementException` nếu hàng đợi rỗng. `peek()` sẽ trả về `null`.                 |

### Các lớp triển khai của Queue Interface
---

## Deque
> Deque (Double-Ended Queue) là một cấu trúc dữ liệu cho phép thêm và loại bỏ phần tử từ cả hai đầu của hàng đợi. Deque Interface trong Java cung cấp các phương thức để thao tác với hàng đợi hai đầu này.

| Thao tác                   | Phương thức (Thao tác ở **Đầu** - First/Head)                              | Phương thức (Thao tác ở **Cuối** - Last/Tail)                            |
|:---------------------------|:---------------------------------------------------------------------------|:-------------------------------------------------------------------------|
| **Thêm phần tử**           | `addFirst(e)` (Ném Exception) <br> `offerFirst(e)` (Trả về boolean)        | `addLast(e)` (Ném Exception) <br> `offerLast(e)` (Trả về boolean)        |
| **Lấy và Xóa**             | `removeFirst()` (Ném Exception) <br> `pollFirst()` (Trả về `null`/giá trị) | `removeLast()` (Ném Exception) <br> `pollLast()` (Trả về `null`/giá trị) |
| **Chỉ Lấy**<br>(không xóa) | `getFirst()` (Ném Exception) <br> `peekFirst()` (Trả về `null`/giá trị)    | `getLast()` (Ném Exception) <br> `peekLast()` (Trả về `null`/giá trị)    |


Các phương thức của Queue được ánh xạ tới các phương thức của Deque như sau:
- `add(e)` tương đương `addLast(e)`
- `remove()` tương đương `removeFirst()`
- `poll()` tương đương `pollFirst()`
- `element()` tương đương `getFirst()`
- `peek()` tương đương `peekFirst()`


### Chú thích