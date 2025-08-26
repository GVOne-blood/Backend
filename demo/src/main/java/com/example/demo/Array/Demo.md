# Array

## [1. Nêu hiểu biết về kiểu mảng trong java](#mảng)
## [2. Dùng kiểu mảng mang lại ưu , nhược điểm gì?](#lợi-ích)

[Chú thích](#chú-thích)

## Mảng
Trong khoa học máy tính, mảng là một cấu trúc dữ liệu cơ bản, lưu trữ một tập hợp các phần tử (giá trị hoặc biến) có cùng kiểu dữ liệu, được xác định bởi một chỉ số (index) hoặc một bộ chỉ số.
>Trong Java, mảng là một đối tượng (object) chứa một số lượng phần tử cố định của một kiểu dữ liệu duy nhất.

Vì là một object nên mảng sẽ được cấp phát bộ nhớ liền kề trên Heap (tốc độ truy cập O(1)) và mang các phương thức cơ bản như *toString(), hashCode(), clone(), ...*.

Mảng có kích thước cố định suốt vòng đời của nó khi được khởi tạo bằng từ khóa new
Một mảng chỉ chứa 1 kiểu dữ liệu được khai báo, mảng tham chiếu (Integer, String, Person,...) chứa các tham chiếu trỏ đến các đối tượng thực sự trong Heap

Khai báo và khởi tạo

![](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/Screenshot%202025-08-26%20161702.png)

Chỉ số của mảng chạy từ 0 đến length - 1, truy cập chỉ số không hợp lệ sẽ ném ra *ArrayIndexOutOfBoundsException*



### length
> đây là một trường public final int, không phải phương thức. Để truy cập ta 
dùng array.length

### Covariance 
> Mảng trong Java có tính Covariance, điều này có nghĩa nếu S là cha của T thì S[] cũng là cha của T[]

```
Object[] objectArray = new String[10]; // Hợp lệ tại thời điểm biên dịch

objectArray[0] = new Integer(1); // Lỗi! Ném ra ArrayStoreException tại runtime
```
Trong trường hợp trên, JVM theo dõi kiểu thực sự của mảng (String[]) và sẽ ném ra ArrayStoreException nếu có một nỗ lực gán một đối tượng không tương thích vào mảng. Đây là một sự đánh đổi trong thiết kế hệ thống kiểu của Java.

Bản thân mảng không thể dùng được Generic type vì tại thời điểm runtime, JVM không nhận biết được kiểu của mnagr do cơ chế xóa kiểu của Generic nên nó không cấp phát bộ nhớ cho mảng

### Mảng đa chiều
Thực tế Java không có mảng đa chiều mà chỉ có cơ chế Mảng trong Mảng. Điều này có thể dẫn đến hình thành mảng răng cưa

```
int[][] jaggedArray = new int[3][];
jaggedArray[0] = new int[5];
jaggedArray[1] = new int[3];
jaggedArray[2] = new int[7];
```


### Lợi ích
- **Hiệu năng & bộ nhớ tốt**
- **Nền tảng của các cấu trúc dữ liệu nâng cao hơn (ArrayList, HashMap,...)**
- **Ưu tiên cho các tương tác cấp thấp với Thread, I/O, thuật toán yêu cầu hiệu năng cao,...**

### Nhược điểm
- **Kích thước cố định**
- **Thao tác chèn và xóa kém hiệu quả (O(N))**
- **Không tương tác được với Generic**
- **Lỗi an toàn kiểu gây ra bới Covariance**








### Chú thích