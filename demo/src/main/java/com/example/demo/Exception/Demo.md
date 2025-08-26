# Exception

## [1. Phân biệt throw và throws](#throw-vs-throws)
## [2. Thế nào là checked và unchecked exception](#checked-exception)
## 3. try catch , try with resource khác nhau như thế nào ?
## 4. Làm thế nào để tạo được 1 custom exception ?

[Chú thích](#chú-thích)

## Exception
> Exception là một cơ chế chính thống để xử lý các tình huống bất thường, phá vỡ luồng xử lý bình thường (Happy Path) của chương trình

Phân cấp Exception: 
Throwable là lớp cha nơi bắt nguồn của mọi Exception và Error:
>>Error: Những lỗi nghiêm trọng, nằm ngoài tầm kiểm soát của ứng dụng và người dùng gần như không thể xử lý chúng

Một số error phổ biến như OutOfMemoryError, StackOverflowError,...
>>Exception: Những tình huống bất thường mà ứng dụng có thể dự đoán và phục hồi được


```
java.lang.Throwable
├── java.lang.Error  (Lỗi hệ thống nghiêm trọng, không nên bắt)
│   ├── StackOverflowError
│   ├── OutOfMemoryError
│   ├── VirtualMachineError
│   └── ...
│
└── java.lang.Exception  (Các tình huống bất thường có thể xử lý)
    │
    ├── (CHECKED EXCEPTIONS - Bắt buộc xử lý hoặc khai báo `throws`)
    │   ├── IOException
    │   ├── SQLException
    │   ├── ClassNotFoundException
    │   ├── InterruptedException
    │   └── ...
    │
    └── java.lang.RuntimeException  (UNCHECKED EXCEPTIONS - Lỗi lập trình, không bắt buộc xử lý)
        ├── NullPointerException
        ├── IllegalArgumentException
        │   └── NumberFormatException
        ├── IndexOutOfBoundsException
        │   ├── ArrayIndexOutOfBoundsException
        │   └── StringIndexOutOfBoundsException
        ├── ClassCastException
        ├── IllegalStateException
        └── ...
```


Phân loại Exception


Về cơ bản trình biên dịch sẽ bắt lỗi trong quá trình biên dịch nếu một phương thức, class ném ra exception mà không xử lý nó. Log sẽ được hiển thị dưới dạng stack trade:

![ex](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/Screenshot%202025-08-26%20135156.png)

Checked

```
java.lang.ClassNotFoundException: com.mysql.cj.jdbc.Driver
        at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:641)
        at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:188) 
        at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:520)
        at java.base/java.lang.Class.forName0(Native Method)
        at java.base/java.lang.Class.forName(Class.java:375)
        at com.example.demo.Exception.Code.main(Code.java:8)
```
Unchecked
```
Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: Index 5 out of bounds for length 4
        at com.example.demo.Exception.Code.main(Code.java:6)
```
---
### Checked Exception

>> Checked Exception: Đơn giản là tất cả các exception kế thừa từ Exception class nhưng không kế thừa từ RuntimeException class

Một số Checked Exception tiêu biểu: 
- **IOException**: Lỗi khi thao tác vào/ra (đọc/ghi file, network). Mạng có thể rớt, file có thể không tồn-tại. Đây là những yếu tố bên ngoài mà code của bạn không kiểm soát được, nên bạn phải chuẩn bị kịch bản xử lý.
- **SQLException**: Lỗi khi tương tác với cơ sở dữ liệu. Database có thể sập, câu query có thể sai.
- **ClassNotFoundException**: Không tìm thấy class tại runtime.

Checked Exception nên được catch hoạt throw nếu không muốn gây ra lỗi biên dịch

---

### Unchecked Exception

>>Unchecked Exception: Đây là tất cả các exception kế thừa từ RuntimeException, là những ngoại lệ về logic phần lớn do người dùng tạo ra

Một số Unchecked Exception tiêu biểu:
- **NullPointerException**: Cố gắng truy cập một phương thức hoặc thuộc tính của một đối tượng null. Đây là lỗi 100% do lập trình viên không kiểm tra null trước khi sử dụng.
- **ArrayIndexOutOfBoundsException**: Truy cập một phần tử ngoài giới hạn của mảng. Lỗi logic của lập trình viên.
- **IllegalArgumentException**: Truyền một tham số không hợp lệ vào phương thức. Lỗi của người gọi phương thức.
- **NumberFormatException**: Cố gắng chuyển một chuỗi không phải số thành số.

Unchecked Exception có thể không cần xử lý hoặc cần phải sửa code để case gây ra unchecked exception không bao giờ xảy ra nữa

### Exception Handling
Trong ngôn ngữ lập trình Java, cơ chế xử lý ngoại lệ (Exception Handling) được triển khai thông qua năm từ khóa cốt lõi: try, catch, finally, throw, và throws. Các từ khóa này phối hợp với nhau để cho phép lập trình viên kiểm soát luồng thực thi của chương trình khi có các sự kiện bất thường xảy ra.

Dựa vào từ khóa, có 2 nhóm chức năng chính:
- Nhóm xử lý ngoại lệ (Handling): **try, catch, finally**
- Nhóm lan truyền ngoại lệ (Propagation): **throw, throws**

#### Try
>Khối try được sử dụng để định nghĩa một phạm vi mã lệnh mà trong đó một hoặc nhiều ngoại lệ có thể được ném ra.

Một **try** không thể tồn tại độc lập, nó phải đi sau bởi khối **catch** hoặc **finally**

```
try {
    // Mã lệnh có khả năng phát sinh ngoại lệ
}
// ... theo sau là catch hoặc finally
```

#### Catch
>Khối catch được sử dụng để định nghĩa một trình xử lý ngoại lệ. Nó sẽ được thực thi khi một ngoại lệ thuộc loại được chỉ định (hoặc lớp con của nó) được ném ra từ trong khối try tương ứng.

Một khối **catch** phải theo sau ngay 1 khối **try** hoặc 1 khối **catch** khác

Một khối **catch** xử lý một ngoại lệ cụ thể

Khi có nhiều khối **catch**, thứ tự sắp xếp là quan trọng. Các khối **catch** cho các lớp ngoại lệ con phải được đặt trước các khối **catch** cho các lớp ngoại lệ cha . Nếu không, trình biên dịch sẽ báo lỗi (unreachable code).

```
try {
    // ...
} catch (ExceptionType1 e1) {
    // Xử lý cho ExceptionType1
} catch (ExceptionType2 e2) {
    // Xử lý cho ExceptionType2
}

// Cú pháp multi-catch (Java 7+)
try {
    // ...
} catch (ExceptionType1 | ExceptionType2 e) {
    // Xử lý chung
}
```

#### Finally
>Khối **finally** định nghĩa một đoạn mã lệnh được đảm bảo thực thi sau khi khối try và bất kỳ khối **catch** nào (nếu có) đã hoàn thành, bất kể có ngoại lệ nào được ném ra hay không.

Khối **finally** là tùy chọn, nhưng khi tồn tại, nó phải được đặt sau tất cả khối **catch**

Code trong **finally** sẽ không được thực hiện nếu luồng thực thi bị chấm dứt đột ngột hoặc máy ảo JVM bị lỗi nghiêm trọng

#### Throw
>throw là một câu lệnh được sử dụng để ném ra một cách tường minh một thể hiện (instance) của một lớp Throwable

Khi câu lệnh throw được thực thi, luồng điều khiển của chương trình sẽ dừng lại ngay lập tức và tìm kiếm khối **catch** phù hợp gần nhất trong ngăn xếp cuộc gọi (call stack) để xử lý ngoại lệ đó. Nếu không tìm thấy trình xử lý nào, luồng sẽ kết thúc và chương trình sẽ dừng hoạt động.

Sử dụng khi:
- Tạo và ném ra các ngoại lệ tùy chỉnh (custom exceptions) để biểu thị các lỗi nghiệp vụ.
-Ném ra các ngoại lệ chuẩn của Java khi các điều kiện tiên quyết của phương thức không được đáp ứng (ví dụ: IllegalArgumentException, IllegalStateException).

```
public void processData(Object data) {
    if (data == null) {
        // Ném ra một thể hiện của NullPointerException
        throw new NullPointerException("Dữ liệu đầu vào không được là null");
    }
    // ...
}
```
#### Throws
>throws là một mệnh đề được sử dụng trong khai báo chữ ký của phương thức (method signature).

Mệnh đề throws liệt kê các loại Checked Exception mà một phương thức có thể ném ra (hoặc lan truyền từ các phương thức mà nó gọi). Nó không tự ném ra ngoại lệ, mà chỉ thông báo cho trình biên dịch và mã nguồn gọi phương thức (caller) rằng họ phải chịu trách nhiệm xử lý các ngoại lệ đã được liệt kê.

Nếu một phương thức có khả năng ném ra một Checked Exception, nó bắt buộc phải:
- Xử lý ngoại lệ đó bằng khối try-catch.
- Hoặc khai báo ngoại lệ đó trong mệnh đề throws của mình để đẩy trách nhiệm xử lý cho phương thức gọi nó.

Việc khai báo Unchecked Exception trong mệnh đề throws là không bắt buộc và thường được xem là không cần thiết.

```
// Phương thức này khai báo rằng nó có thể ném ra IOException.
public void readFile(String path) throws IOException {
    FileInputStream fis = new FileInputStream(path);
    // ...
}
```

#### Throw vs Throws


  |   | Throw |   Throws    |
  | :---: | :--------: | :---------------: |
  | **Mục đích**  |  Ném ra một thể hiện cụ thể của ngoại lệ    |    Khai báo rằng phương thức có thể ném ra một hoặc nhiều ngoại lệ    |
  | Vị trí  |   Trong thân phương thức   |  Signature của phương thức  |
  |  Đối tượng theo sau  |   Một instance của lớp Throwable   | Một hoặc nhiều exception của Throwable |
  | Số lượng  |   Chỉ ném được một ngoại lệ mỗi lần   | Có thể khai báo nhiều ngoại lệ cần nhả |
  | Chức năng | Gây ra sự gián đoạn trong luồng thực thi | Được trình biên dịch sử dụng để kiểm tra các Checked Exception |


### Chú thích
1. Happy Path : Con đường mà code chạy mượt mà
2. Sad Path : Những điều xảy ra ngoài dự kiến ảnh hưởng đến luồng hoạt động