# Primitive & Object Data Tpye

## [1. Phân biệt kiểu dữ liệu nguyên thủy và kiểu dữ liệu object.](#phân-biệt-kiểu-nguyên-thủy-và-tham-chiếu)

## 2. Có thể chuyển đổi giữa hai kiểu dữ liệu này không ?

## 3. Có thể so sánh hai kiểu dữ liệu này với nhau không?

## 4. Giá trị khi khởi tạo biến với hai loại kiểu dữ liệu này là gì?

---

[Chú thích](#chú-thích)

### I. Lý thuyết

Trong Java, hệ thống kiểu dữ liệu được phân chia thành hai loại chính: kiểu dữ liệu nguyên thủy (Primitive Data Types) và kiểu dữ liệu đối tượng (Object Data Types), hay còn gọi là kiểu tham chiếu (Reference Types). Sự phân biệt này là nền tảng cốt lõi, ảnh hưởng sâu sắc đến cách Java quản lý bộ nhớ, hiệu suất và bản chất của lập trình hướng đối tượng.

#### 1. Kiểu dữ liệu nguyên thủy (Primitive Data Type)

> Là kiểu dữ liệu cơ bản được định nghĩa sẵn trong ngôn ngữ Java, chúng không phải là Object nên cũng không có các phương thức đi kèm. Các biến kiểu Primitive lưu trữ trực tiếp giá trị của chúng trong bộ nhớ stack.

Primitive DT được chia thành 4 kiểu cơ bản:

- Kiểu số nguyên

  | Kiểu  | Kích thước |   Miền giá trị    |
  |:-----:|:----------:|:-----------------:|
  | byte  |   8-bit    |    [-128, 127]    |
  | short |   16-bit   |  [-32768, 32767]  |
  |  int  |   32-bit   | [-2e31, 2e31 - 1] |
  | long  |   64-bit   | [-2e63, 2e63 - 1] |

- Kiểu số thực

  |  Kiểu  | Kích thước |           Chuẩn            |
  |:------:|:----------:|:--------------------------:|
  | float  |   32-bit   | IEEE 754 (single accurate) |
  | double |   64-bit   |          IEEE 754          |
- Kiểu ký tự : Kích thước 16-bit, biểu diễn 1 ký tự unicode
- Kiểu logic : boolean - true;false, kích thước mặc định: 1bit or 1byte

---

#### 2. Kiểu dữ liệu tham chiếu (Reference/Object Data Type)

> Là kiểu dữ liệu được định nghĩa bởi user hoặc các API của Java, được thể hiện dưới dạng các Object, nó không lưu trữ trực tiếp đối tượng mà nó đại diện, thay vào đó nó lưu trữ 1 tham chiếu đến vị trí của đối tượng trong bộ nhớ heap. Tất cả các class, interface, enum, array,... đều thuộc kiểu dữ liệu này.

- Cấu trúc cây phân cấp của Object Data Type:
  - java.lang.Object : Là root class của hệ thống phân cấp, mọi lớp đều là lớp con của Object, nghĩa là mọi class đều kế thừa từ lớp này và cố các phương thức cơ bản của object như *equals(), hashCode(), toString(), getClass(), ...*
![Cấu trúc cây các đối tượng](data:image/png;base64,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)

## Phân biệt kiểu nguyên thủy và tham chiếu 


  |                      |                                                              Primitive Data Type                                                               |                                                        Object Data Type                                                         |
  |:--------------------:|:----------------------------------------------------------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------------------------------------:|
  |    **Nguồn gốc**     |                                              Được định nghĩa sẵn thông qua các từ khóa trong Java                                              |                                             User tự custom hoặc đến từ API của Java                                             |
  |     **Lưu trữ**      |                                                           Giá trị dưới dạng nhị phân                                                           |                                        Lưu trữ 1 tham chiếu trỏ đến vị trí của đối tượng                                        |
  | **Giá trị mặc định** |                                             0 cho kiểu number, false cho boolean, \u0000 cho char                                              |                            null - đồng nghĩa khi gọi các phương thức sẽ nhả ra NullPointerException                             |
  |  **Truyền tham số**  | pass-by-value: tạo một bản sao của giá trị để truyền vào tham số, giá trị của đối số không thay đổi trong suốt quá trình thực hiện phương thức | Truyền bản sao của tham chiếu, phương thức có thể thay đổi trạng thái của đối tượng gốc nhưng không làm thay đổi tham chiếu gốc |
  |      **Bộ nhớ**      |                                                                     Stack                                                                      |                         Heap để lưu trữ đối tượng, Stack để lưu trữ tham chiếu (con trỏ) của đối tượng                          |
  | **Hướng đối tượng**  |                                                                     Không                                                                      |              Kế thừa từ Object, có các phương thức, có thể kế thừa, triển khai interface và thể hiện tính đa hình               |

>Khởi tạo

![Sơ đồ minh họa](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/Screenshot%202025-08-25%20140103.png)

>Giá trị mặc định 

![md](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/Screenshot%202025-08-25%20142139.png)

>Tham chiếu trong hàm

![reference](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/Screenshot%202025-08-25%20144423.png)

---
## Chuyển đổi giữa 2 loại dữ liệu

> Java hỗ trợ chuyển đổi từ primitive sang object (Boxing) và ngược lại (Unboxing) bằng lớp bao bọc (Wrapper Classes)

- Cơ chế Boxing và Unboxing: Bản chất của boxing là việc thay đổi giá trị của biến value trong đối tượng bao bọc nó, ví dụ:
  ```
  public final class Integer extends Number implements Comparable<Integer> {

    
      private final int value;
      ... other attributes 
    
      public Integer(int value) {
          this.value = value;
      }

    
      public int intValue() {
          return this.value;
      }

    
      @Override
      public boolean equals(Object obj) {
          if (obj instanceof Integer) {
              return this.value == ((Integer)obj).intValue();
          }
          return false;
      }

      @Override
      public String toString() {
          return Integer.toString(this.value);
      }
  }
  ```
  một lớp wrapper thực chất chứa một biến private final + kiểu nguyên thủy tương ứng. Vì vậy muốn boxing một biến primitive ta có thể gọi constructor, method valueOf(), mục đích cuối cùng để gán giá trị cho biến value:

  ![box](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/Screenshot%202025-08-25%20151007.png)


### Chú thích

1. **Bộ nhớ Stack**: Là vùng nhớ được quản lý tự động, có tốc độ truy xuất rất cao. Nó được sử dụng để lưu trữ các biến cục bộ (local variables) của phương thức và các tham chiếu đến đối tượng. Khi một phương thức kết thúc, toàn bộ khung bộ nhớ (stack frame) của nó sẽ được giải phóng. Biến kiểu nguyên thủy được lưu trữ hoàn toàn trên Stack. Đối với kiểu Object, biến tham chiếu (con trỏ) được lưu trên Stack.
2. **Bộ nhớ Heap**: Là vùng nhớ động, được sử dụng để cấp phát bộ nhớ cho tất cả các đối tượng được tạo ra thông qua từ khóa new. Việc quản lý bộ nhớ trên Heap phức tạp hơn và được thực hiện bởi Garbage Collector (GC) của Java. Đối tượng thực sự (với tất cả các thuộc tính và dữ liệu của nó) nằm trên Heap.
3. **Autoboxing và Auto-unboxing** bản chất chỉ là cách viết gọn hơn của việc gọi các phương thức valueOf() hay intValue(), khi Java compile chương trình chúng vẫn gọi các phương thức trên như thường 