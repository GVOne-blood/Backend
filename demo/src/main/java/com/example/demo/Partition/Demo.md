# Partition

## 1. Khi nào thì đánh partition trong 1 bảng

## 2. Đánh partition mang lại lợi ích gì cho truy vấn

[Chú thích](#chú_thích)

## Partition

> Partition là kỹ thuật chia 1 bảng lớn về mặt logic về mặt logic thành nhiều mảnh nhỏ hơn, dễ quản lý hơn về mặt quản
> lý. Các mảnh nhỏ này được gọi là các partitions

Về mặt giao diện, bảng sau khi phân vùng vẫn không khác gì bảng thường, ta vẫn có thể CRUD bình thường, nhưng về mặt vật
lý trên đĩa, SQL đã phân vùng và các query sẽ được định tuyến đến các phân vùng phù hợp

Phân vùng trở nên vô nghĩa nếu không có khóa phân vùng (partition key)

Khi query đến bảng phân vùng, optimizer sẽ thực hiện partition Pruning (Cắt tỉa phân vùng), nó đủ thông minh để chọn
phân vùng phù hợp với data đang tìm kiếm

Tốc độ khi cũng được cải thiện khi thay vì phải cập nhật index cho cả bảng, ta chỉ cần cập nhật index cho phân vùng bị
thay đổi

Có thể xóa và backup từng vùng riêng lẻ dễ dàng và nhanh chóng

Phân loại (PostgreSQL)

- Phân vùng theo khoảng : Bảng được chia thành các vùng, mỗi vùng chứa các hàng mà giá trị của khóa phân vùng nằm trong
  1 range nhất định, vì vậy key phân vùng thường là giá trị kiểu số or ngày tháng
    ```
    CREATE TABLE (...) PARTITION BY RANGE (column)
  
    CREATE TABLE ... PARTITION OF <parent_table>
    FOR VALUES FROM (...) TO (...)
    ```
    - Phân vùng theo danh sách : Phân vùng theo khoảng : Bảng được chia thành các vùng, mỗi vùng chứa các hàng mà giá
      trị
      của khóa phân vùng nằm trong
      1 danh sách các giá trị cụ thể, key thường là cột có 1 tập hợp giá trị hữu hạn or rời rạc
  ```  
  CREATE TABLE(...) PARTITION BY LIST (column)
  
  CREATE TABLE ... PARTITION OF <parent_table>
  FOR VALUES IN (...)
    ```
- Phân vùng băm : Bảng được chia thành 1 số phân vùng chỉ định trước, data được phân phối đều vào các phân vùng này dựa
  trên hash value của partition key
    ```
  CREATE TABLE (...) 
  PARTITION BY HASH (<column>)
  
  CREATE TABLE ... PARTITION OF <parent_table> FOR VALUES WITH (...)
  ```

Nếu ta cố chèn data không khớp với bất kỳ phân vùng nào vào, SQL sẽ báo lỗi

Gỡ bỏ phân vùng (giữ data)
```ALTER TABLE ... DETACH PARTITION ...```

nó khiến phân vùng này trở thành 1 bảng độc lập

Tất nhiên nên dùng partition khi table quá lớn, nhiều data, thường xuyên xóa data theo khối (như log,...)

### Chú thích