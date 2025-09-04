# Index

## 1. Phân biệt cluester index và non-cluster index ? So sánh về perfomance khi sử dụng 2 loại index này ? Giải thích nguyên nhân

## 2. Có giới hạn việc đánh bao nhiêu index trong 1 table hay không ? Giải thích

## 3. Việc đánh index dựa trên cơ sở nào ?

## 4. Làm thế nào để biết 1 câu query đã sử dụng index hay chưa ?

[Chú thích](#chú_thích)

## Index

> Index là 1 cấu trúc dữ liệu đặc biệt, được tạo ra trên 1 or nhiều cột của một bảng, nhằm mục đích tăng tốc truy xuất
> dữ liệu

Index như mục lục ủa một cuốn sách

Ưu điểm:

- Tăng tốc độ truy vấn, càng nhiều record thì tốc độ truy vấn của Index so với Sqe Scan càng nhanh
- Thực thi các ràng buộc `UNIQUE` or `PRIMARY KEY` hiệu quả
- Tăng tốc thao tác `ORDER BY` nếu Index được sắp xếp đúng thứ tự

Nhược điểm:

- Làm chậm các thao tác `INSERT`, `UPDATE`, `DELETE`: Khi thay đổi dữ liệu đã đánh index, SQL ngoài việc thay đổi data
  sẽ phải cập nhật toàn bộ
  cấu trúc Index. Càng nhiều Index, thao tác ghi càng chậm
- Tốn dung lượng lưu trữ: Index là một cấu trúc dữ liệu riêng biệt và tất nhiên nó cũng tốn không gian lưu trữ

### Non-cluster Index

### Cluster Index

### Chú thích