# Index

## 1. Phân biệt cluester index và non-cluster index ? So sánh về perfomance khi sử dụng 2 loại index này ? Giải thích nguyên nhân

## 2. Có giới hạn việc đánh bao nhiêu index trong 1 table hay không ? Giải thích

## 3. Việc đánh index dựa trên cơ sở nào ?

## 4. Làm thế nào để biết 1 câu query đã sử dụng index hay chưa ?

[Chú thích](#chú_thích)

## Index

> Index là 1 cấu trúc dữ liệu đặc biệt, được tạo ra trên 1 or nhiều cột của một bảng, nhằm mục đích tăng tốc truy xuất
> dữ liệu

Index như mục lục của một cuốn sách, chỉ nên đánh index khi :

- Table lớn, nhiều records
- Data trên table (trường đánh index) không `update` quá nhiều
- Trường đánh index ít trùng lặp, unique là best practice

Ưu điểm:

- Tăng tốc độ truy vấn, càng nhiều record thì tốc độ truy vấn của Index so với Sqe Scan càng nhanh
- Thực thi các ràng buộc `UNIQUE` or `PRIMARY KEY` hiệu quả
- Tăng tốc thao tác `ORDER BY` nếu Index được sắp xếp đúng thứ tự

Nhược điểm:

- Làm chậm các thao tác `INSERT`, `UPDATE`, `DELETE`: Khi thay đổi dữ liệu đã đánh index, SQL ngoài việc thay đổi data
  sẽ phải cập nhật toàn bộ
  cấu trúc Index. Càng nhiều Index, thao tác ghi càng chậm
- Tốn dung lượng lưu trữ: Index là một cấu trúc dữ liệu riêng biệt và tất nhiên nó cũng tốn không gian lưu trữ

Cách kiểm tra xem 1 table đã dược đánh index chưa là xem Execute Plan của nó, ví dụ:

```
EXPLAIN ANALYZE SELECT id, username FROM users WHERE email = 'testuser123@example.com';
```

Không có giới hạn cụ thể hoặc không thể đạt đến giới hạn việc có thể đáng tối đa bao nhiêu index trên 1 bảng, như
Postgre thì tầm 1000 cái, MySQL 65 cái tính cả cluster index,...

Đó là giới hạn phần cứng, nhưng phải biết việc đánh nhiều index khiến tốn bộ nhớ, tác dụng ngược làm chậm tốc độ do khi
thay đổi bảng sẽ phải thay đổi toàn bộ index

nếu có Index Scan thì oce

![xx](src/main/resources/local/kPFIa.png)

### Cluster Index

> một clustered index là cấu trúc dữ liệu được sắp xếp theo thứ tự tăng dần hoặc giảm dần của các giá trị trên một hoặc
> nhiều cột trong bảng. Dữ liệu trong bảng được lưu trữ thực sự theo thứ tự này dựa trên khóa của clustered index.

Nói chung Cluster index là một dải các index duy nhất trong 1 bảng, nó thực hiện sort các bản ghi trong bảng đó theo giá
trị của index. Gần như khi chọn trường nào để gán index, nó sẽ sort trường đó tồi Index Scan theo nó

Vì là gán index bằng việc sort theo field được gán nên có thể hiểu tại sao 1 table chỉ được có 1 cluster index

Thuật toán cluster index sử dụng để tìm kiếm chính xác record là Binary-search, việc sort theo field tạo ra một dãy
index, mỗi index được gắn với bản ghi tương ứng của nó, với việc tổ chức theo B-tree, độ phức tạp sẽ là O(log(N))

### Non-cluster Index

> Một Non-Clustered Index là một cấu trúc dữ liệu tách biệt với dữ liệu của bảng. Nó chứa các giá trị của các cột được
> đánh index và một "con trỏ" (pointer) cho mỗi giá trị. Con trỏ này trỏ đến vị trí của hàng dữ liệu tương ứng trong
> bảng.
> Dữ liệu trong bảng không được sắp xếp theo thứ tự của Non-Clustered Index.

Tìm kiếm trên non-cluster index không nhanh hơn so với cluster, bởi nó phải sort 1 or 1 tập trường không phải khóa
chính, lúc đó nó sẽ phải dùng cluster index trên bảng đó (thường là PK) và gán con trỏ trỏ tới tương ứng. Khi tìm kiếm,
nó cố gắng binary-search đến giá trị của non-cluster, vì non-cluster gắn với cluster nên bài toán giờ trở về tìm kiếm
với cluster-index, độ phức tạp thực tế là O(2log(N))

### Chú thích

