# String

## [1. Tìm hiểu về các đặc điểm và tính chất của String trong java](#đặc-điểm-và-tính-chất)
## [2. Có bao nhiêu cách để tạo 1 biến String](#khởi-tạo)
## [3. Tìm hiểu về String pool?](#)
## [4. Làm sao để so sánh hai chuỗi trong java](#)

---

[Chú thích](#chú-thích)

---

Lớp **java.lang.String** là một trong những kiểu dữ liệu tham chiếu cơ bản và được đối xử đặc biệt nhất trong hệ sinh thái Java. Nó không chỉ đơn thuần là một lớp tiện ích để xử lý chuỗi ký tự, mà còn là một thành phần kiến trúc cốt lõi với các tính chất được thiết kế cẩn thận nhằm đảm bảo tính an toàn, hiệu suất và tính nhất quán. Bản chất của String được định hình bởi một tập hợp các đặc điểm không thể tách rời, trong đó quan trọng nhất là tính bất biến (immutability).

## Khởi tạo

>String có 2 cách khởi tạo trực tiếp  

![init](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/Screenshot%202025-08-25%20170438.png
)


## Đặc điểm và tính chất
> Tính bất biến: Khi một đối tượng String được khởi tạo, trạng thái nội tại của nó không thể  bị thay đổi trong suốt vòng đời của đối tượng trong Heap

Tất cả các phương thức của String (concat(), substring(),...) về bản chất đều không làm thay đổi đối tượng String đó mà trả về một đối tượng String mới

![str](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/Screenshot%202025-08-25%20161919.png)

Việc thực hiện tính bất biến được Java đảm bảo bởi việc khai báo lớp String là final, ngăn chặn việc kế thừa và ghi đè phương thức. Khai báo trường dữ liệu chứa giá trị **byte[] value** là private final để ngăn chặn truy cập trực tiếp và gán lại tham chiếu. Ngoài ra String không cung cấp bất kỳ phương thức accessor nào cho phép thay đổi trạng thái nội tại của đối tượng

```
public final class String
    implements java.io.Serializable, Comparable<String>, CharSequence,
               Constable, ConstantDesc {

   
    private final byte[] value;

    private int hash; // Default to 0

    private boolean hashIsZero; // Default to false;
    ... other attribute & methods
               }

```
Hệ quả của tính chất này là then chốt trong:
- **Đảm bảo an toàn đa luồng (Thread Safety)**: String có thể được chia sẻ giữa nhiều luồng mà không cần bất kỳ cơ chế đồng bộ hóa nào
- **Bảo mật**: Bảo vệ các dữ liệu nhảy cảm (password, url,...) trước các lỗ hổng bảo mật
- **Hiệu suất & Caching**: Immutable là điều kiện kiên quyết cho sự tồn tại của String Constant Pool(SCP). Ngoài ra vì giá trị không bị thay đổi, hashcode của String value chỉ được tính toán 1 lần duy nhất và lưu trong môi trường nội tại -> hoạt động rất hiệu quả khi được dùng làm key trong các cấu trúc dữ liệu băm như HashMap

>String pool: Một vùng nhớ đặc biệt trong Heap, được sử dụng để lưu trữ các chuỗi hằng (String literals) và các chuỗi nội bộ hóa (interned string)

Cơ chế SCP: Khi trình biên dịch gặp phải một hằng chuỗi, JVM sẽ kiểm tra trong SCP, nếu chuỗi đó đã tồn tại trong SCP, biến tham chiếu sẽ lập tức được trỏ đến đối tượng đó thay vì tạo một đối tượng mới, nếu chuỗi đó chưa tồn tại, Java sẽ tạo ra một đối tượng mới trong SCP và biến tham chiếu sẽ được trỏ đến đó

![SCP](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/Screenshot%202025-08-25%20165551.png)




>Khả năng nối chuỗi: Nối chuỗi bằng toán tử + (Không như Python, Java không cho phép người dùng được nạp chồng toán tử, String là ngoại lệ trong thiết kế ngôn ngữ duy nhất)

### Chú thích
