# List

## [1. Nêu ra các đặc điểm List Interface](#list-interface)

## 2. Kể ra các class triển khai từ List Interface

## 3. Phân biệt rõ trường hợp sử dụng của từng class đó

[Chú thích](#chú-thích)

## List interface

> Trong Java Collections Framework, List là một interface con của Collection. Nó định nghĩa một tập hợp các phần tử được sắp xếp theo thứ tự (ordered collection) và cho phép lưu trữ các phần tử trùng lặp (duplicates). Đặc điểm định danh quan trọng nhất của List là nó cung cấp khả năng kiểm soát chính xác vị trí chèn và truy cập các phần tử thông qua chỉ số (index) nguyên, tương tự như mảng.

Đặc điểm:

- List **duy trì thứ tự chèn của các phần tử**, khi duyệt qua list, nó sẽ trả về đúng danh sách phần tử theo thứ tự chèn
- Phần tử của List được **truy cập dựa trên chỉ số** (kế thừa từ mảng) thông qua phương thức
- Cho phép **null**
- Cho phép **trùng lặp** - 1 list có thể có nhiều phần tử trùng nhau

Các phương thức của List (So sánh với LinkedList)

| Phương thức                                            | Mô tả                                                                                                                                                           | Độ phức tạp (`ArrayList`) | Độ phức tạp (`LinkedList`) |
| :----------------------------------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------- | :------------------------ | :------------------------- |
| **Thao tác Truy cập (Access Operations)**              |                                                                                                                                                                 |                           |                            |
| `E get(int index)`                                     | Trả về phần tử tại vị trí chỉ định.                                                                                                                             | **O(1)**                  | O(n)                       |
| `E set(int index, E element)`                          | Thay thế phần tử tại vị trí chỉ định bằng phần tử mới. Trả về phần tử cũ.                                                                                       | **O(1)**                  | O(n)                       |
| **Thao tác Thêm/Sửa đổi (Modification Operations)**    |                                                                                                                                                                 |                           |                            |
| `boolean add(E e)`                                     | Thêm phần tử vào cuối danh sách.                                                                                                                                | O(1) trung bình           | **O(1)**                   |
| `void add(int index, E element)`                       | Chèn phần tử vào vị trí chỉ định.                                                                                                                               | O(n)                      | O(n) ¹                     |
| `boolean addAll(Collection<? extends E> c)`            | Thêm tất cả các phần tử trong collection vào cuối danh sách.                                                                                                    | O(n)                      | O(n)                       |
| `boolean addAll(int index, Collection<? extends E> c)` | Chèn tất cả các phần tử trong collection vào vị trí chỉ định.                                                                                                   | O(n)                      | O(n)                       |
| **Thao tác Xóa (Removal Operations)**                  |                                                                                                                                                                 |                           |                            |
| `E remove(int index)`                                  | Xóa phần tử tại vị trí chỉ định. Trả về phần tử đã bị xóa.                                                                                                      | O(n)                      | O(n) ¹                     |
| `boolean remove(Object o)`                             | Xóa lần xuất hiện đầu tiên của phần tử chỉ định khỏi danh sách.                                                                                                 | O(n)                      | O(n)                       |
| `void clear()`                                         | Xóa tất cả các phần tử khỏi danh sách.                                                                                                                          | O(n)                      | O(n)                       |
| **Thao tác Tìm kiếm (Search Operations)**              |                                                                                                                                                                 |                           |                            |
| `int indexOf(Object o)`                                | Trả về chỉ số của lần xuất hiện đầu tiên của phần tử chỉ định, hoặc -1 nếu không tìm thấy.                                                                      | O(n)                      | O(n)                       |
| `int lastIndexOf(Object o)`                            | Trả về chỉ số của lần xuất hiện cuối cùng của phần tử chỉ định, hoặc -1 nếu không tìm thấy.                                                                     | O(n)                      | O(n)                       |
| `boolean contains(Object o)`                           | Trả về `true` nếu danh sách chứa phần tử chỉ định.                                                                                                              | O(n)                      | O(n)                       |
| **Thao tác Lấy danh sách con (Sublist Operations)**    |                                                                                                                                                                 |                           |                            |
| `List<E> subList(int fromIndex, int toIndex)`          | Trả về một "view" của một phần danh sách, từ `fromIndex` (bao gồm) đến `toIndex` (không bao gồm). Thay đổi trên sublist sẽ ảnh hưởng đến list gốc và ngược lại. | O(1)                      | O(1)                       |
| **Thao tác Duyệt (Iteration Operations)**              |                                                                                                                                                                 |                           |                            |
| `Iterator<E> iterator()`                               | Trả về một iterator để duyệt qua các phần tử theo đúng thứ tự. Kế thừa từ `Iterable`.                                                                           | O(1)                      | O(1)                       |
| `ListIterator<E> listIterator()`                       | Trả về một list iterator để duyệt qua các phần tử (có thể duyệt hai chiều).                                                                                     | O(1)                      | O(1)                       |
| `ListIterator<E> listIterator(int index)`              | Trả về một list iterator bắt đầu từ vị trí chỉ định.                                                                                                            | O(1)                      | O(n)                       |
| **Thao tác khác (Other Operations)**                   |                                                                                                                                                                 |                           |                            |
| `int size()`                                           | Trả về số lượng phần tử trong danh sách. Kế thừa từ `Collection`.                                                                                               | O(1)                      | O(1)                       |
| `boolean isEmpty()`                                    | Trả về `true` nếu danh sách không có phần tử nào. Kế thừa từ `Collection`.                                                                                      | O(1)                      | O(1)                       |
| `Object[] toArray()`                                   | Trả về một mảng chứa tất cả các phần tử trong danh sách.                                                                                                        | O(n)                      | O(n)                       |
| `<T> T[] toArray(T[] a)`                               | Trả về một mảng chứa tất cả các phần tử; kiểu runtime của mảng trả về là của mảng được chỉ định.                                                                | O(n)                      | O(n)                       |

---

### Các triển khai của List



| Tiêu chí                 | `ArrayList`                                                                                                      | `LinkedList`                                                                                                     | `Vector` / `Stack`                                                                                                                                |
| :------------------------ | :--------------------------------------------------------------------------------------------------------------- | :--------------------------------------------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------ |
| **Cấu trúc dữ liệu**     | Mảng động (Dynamic Array)                                                                                        | Danh sách liên kết đôi (Doubly Linked List)                                                                      | Mảng động (Dynamic Array)                                                                                                                       |
| **Truy cập ngẫu nhiên (`get(i)`)** | <span style="color:green">**Rất nhanh (O(1))**</span>                                                      | <span style="color:red">Chậm (O(n))</span>                                                                       | <span style="color:orange">Nhanh (O(1))</span>, nhưng chậm hơn `ArrayList` do đồng bộ hóa.                                                          |
| **Thêm/Xóa ở cuối**      | <span style="color:green">Nhanh (O(1) trung bình)</span>                                                          | <span style="color:green">**Rất nhanh (O(1))**</span>                                                      | <span style="color:orange">Nhanh (O(1) trung bình)</span>                                                                                          |
| **Thêm/Xóa ở đầu/giữa**  | <span style="color:red">Chậm (O(n))</span> do phải dịch chuyển phần tử.                                            | <span style="color:green">**Nhanh (O(1))**</span> (nếu đã có iterator), chỉ cần thay đổi con trỏ.                    | <span style="color:red">Chậm (O(n))</span>, tương tự `ArrayList`.                                                                                  |
| **Đồng bộ hóa (Thread-safe)** | **Không** (Non-synchronized). Nhanh hơn trong môi trường đơn luồng.                                              | **Không** (Non-synchronized).                                                                                    | **Có** (Synchronized). Mọi phương thức đều được đồng bộ hóa, gây ra chi phí hiệu năng.                                                            |
| **Sử dụng bộ nhớ**       | Hiệu quả hơn. Chỉ có overhead của mảng.                                                                          | Tốn nhiều bộ nhớ hơn. Mỗi phần tử (Node) cần thêm 2 tham chiếu (`next`, `prev`).                                    | Tương tự `ArrayList`, nhưng có thể lãng phí hơn do chiến lược tăng gấp đôi kích thước.                                                           |
| **Trường hợp sử dụng tối ưu** | ✅ **Lựa chọn mặc định cho hầu hết các trường hợp.**<br>✅ Thường xuyên đọc và truy cập ngẫu nhiên.<br>✅ Ít thao tác chèn/xóa ở giữa. | ✅ Thường xuyên thêm/xóa ở **đầu hoặc cuối** danh sách.<br>✅ Dùng để triển khai **Stack** hoặc **Queue** (thông qua `Deque` interface). | ❌ **Không khuyến khích (Legacy).**<br>• Thay thế `Stack` bằng `java.util.ArrayDeque`.<br>• Nếu cần thread-safe, dùng `Collections.synchronizedList()` hoặc `CopyOnWriteArrayList`. |


### Chú thích
