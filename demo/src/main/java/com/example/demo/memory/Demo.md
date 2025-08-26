# Memory
## [1. Thế nào là cấp phát tĩnh và cấp phát động ?](#cấp-phát-tĩnh-static-memory-allocation)
## [2. Phân biệt bộ nhớ heap và bộ nhớ stack ?](#bộ-nhớ-stack-và-heap)

[Chú thích](#chú-thích)

---
## Cấp phát tĩnh (Static Memory Allocation)
>Cấp phát tĩnh là việc cấp phát bộ nhớ có kích thước và thời điểm được xác định lúc biên dịch (compile time). Bộ nhớ này sẽ được cấp phát tự động và giải phóng tự động khi cần.

Cấp phát tĩnh thường được sử dụng trên Stack hoặc Metaspace:
- Stack: việc cấp phát rất nhanh, chỉ đơn giản là di chuyển con trỏ (stack pointer), được JVM tự động quản lý, hoạt động theo cơ chế LIFO, đổi lại vòng đời ngắn (chỉ tồn tại trong phạm vi của phương thức), kích thước phải được biết trước tại thời điểm biên dịch, bộ nhớ Stack có dung lượng vài MB, nếu đệ quy sâu sẽ gặp lỗi StackOverFlowError.
- Metaspace: Các thuộc tính tĩnh của lớp hay thuộc tính static cũng được lưu trữ

![st](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/Screenshot%202025-08-26%20112112.png)

## Cấp phát động (Dynamic Memory Allocation)
>Cấp phát động là việc cấp phát bộ nhớ tại thời điểm chạy (runtime), thường là cho các đối tượng có kích thước và số lượng không thể biết trước

Tất cả các đối tượng đều được cấp phát trên bộ nhớ Heap thông qua từ khóa **new**

![new](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/Screenshot%202025-08-26%20113159.png)

Cấp phát động có vòng đời phức tạp hơn, chậm hơn trên Stack, cần cơ chế đồng bộ hóa khi dùng trong môi trường đa luồng, nhưng được quản lý bởi Garbage Collector (GC) có trên Heap và có kích thước linh hoạt

## Bộ nhớ Stack và Heap
Xét cách bộ nhớ cấp phát cho đoạn chương trình sau:

```
public class Employee {
    private String name; // name là tham chiếu đến đối tượng String trên Heap

    public Employee(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

public class MemoryExample {
    public static void main(String[] args) {
        int id = 25; // (1)
        Employee emp = createEmployee("John Doe"); // (2)
        // ... some logic
    } // (5)

    public static Employee createEmployee(String employeeName) { // (3)
        Employee newEmp = new Employee(employeeName); // (4)
        return newEmp;
    }
}
```

1. main được gọi:

- STACK: Một "khung" (frame) mới cho main được đẩy vào Stack.
- STACK: Biến nguyên thủy int id = 25; được tạo và lưu trữ giá trị 25 trực tiếp trong frame của main.
- createEmployee("John Doe") được gọi:
- TACK: Một frame mới cho createEmployee được đẩy lên trên cùng của Stack, nằm trên frame của main.
- Bên trong createEmployee:
    - STACK: Tham số String employeeName được tạo trong frame của createEmployee. Nó là một tham chiếu, trỏ đến đối tượng String "John Doe" nằm trên Heap (cụ thể hơn là trong String Pool, một phần của Heap).
    - new Employee(employeeName):
        - HEAP: Từ khóa new yêu cầu JVM cấp phát một vùng nhớ trên Heap để chứa đối tượng Employee.
        - HEAP: Đối tượng Employee này có một trường name, trường này lại là một tham chiếu trỏ đến cùng đối tượng String "John Doe" trên Heap.
        - STACK: Biến tham chiếu Employee newEmp được tạo trong frame của createEmployee. Nó giữ địa chỉ của đối tượng Employee vừa tạo trên Heap.
- createEmployee trả về và kết thúc:
    - STACK: Frame của createEmployee bị xóa khỏi Stack. Biến newEmp và employeeName biến mất.
    - STACK: Giá trị trả về (địa chỉ của đối tượng Employee) được gán cho biến emp trong frame của main. Bây giờ emp trỏ đến đối tượng Employee trên Heap.
    - HEAP: Đối tượng Employee vẫn tồn tại vì giờ đây có emp đang trỏ tới nó.
2. main kết thúc:
- STACK: Frame của main bị xóa khỏi Stack. Biến id và emp biến mất.
- HEAP: Lúc này, đối tượng Employee và đối tượng String "John Doe" không còn tham chiếu nào trỏ đến nữa. Chúng trở thành "rác" và sẽ bị Garbage Collector dọn dẹp trong lần chạy tiếp theo.

---

  |   |  Bộ nhớ Stack |   Bộ nhớ Heap   |
  | :---: | :--------: | :---------------: |
  | **Mục đích**  |   Quản lý luồng thực thi (method execution). Theo dõi các phương thức đang được gọi.   |    Lưu trữ các đối tượng (objects) và mảng (arrays) của Java.    |
  | **Quản lý bộ nhớ** |   Tự động bởi JVM theo cơ chế LIFO   |  Tự động bởi Garbage Collector (GC)  |
  |  **Vòng đời**  |   Ngắn. Tồn tại trong thời gian phương thức đang chạy. Bị hủy ngay khi phương thức kết thúc.   | Dài hơn. Tồn tại cho đến khi không còn bất kỳ tham chiếu nào trỏ đến nó và bị GC thu hồi. |
  | **Kích thước bộ nhớ**  |   Nhỏ và cố định (thường là vài MB cho mỗi luồng)   | Lớn và linh hoạt (có thể tăng giảm ) |
  | **Tốc độ** | Rất nhanh   | Chậm hơn |
  | **Phù hợp với** | các biến nguyên thủy và biến tham chiếu | Toàn bộ đối tượng được khởi tạo bằng **new** |
  |  **Phạm vi truy cập** | chỉ truy cập được bởi luồng sở hữu nó | có thể được truy cập bởi tất cả các luồng nếu chúng có tham chiếu |
  


### Chú thích