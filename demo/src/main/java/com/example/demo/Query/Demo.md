# Query

## [Trong một câu query, thứ tự thực hiện của các thành phần như thế nào?](#thứ_tự_thực_hiện_query)

## Tìm hiểu và lấy ví dụ về :

### [Các loại JOIN](#join)

### [Sub query](#sub_query_select_lồng)

### [CTE (Common Table Expression)](#cte)

### [Ranking : ROW_NUMBER, RANK, DENSE_RANK](#ranking)

### [VIEW : Có thể CRUD trên VIEW được không ?](#view)

### [PROCEDURE : mục đích khi tạo ra 1 PROCEDURE là gì ? Làm thế nào để sử dụng PROCEDURE trong ứng dụng java.](#procedure)

[Chú thích](#chú_thích)

SQL là ngôn ngữ tiêu chuẩn sử dụng để giao tiếp với các hệ quản trị cơ sở dữ liệu quan hệ

## Thành phần query

Query SQL được chia thành 5 nhóm chính :

- DQL (Data Language Query):Dùng để truy vấn và lấy dữ liệu
- DML (Data Manipulation Language):Dùng để thêm, sửa, xóa dữ liệu
- DDL (Data Definition Language): Dùng để định nghĩa và quản lý cấu trúc của các đối tượng trong CSDL
- DCL (Data Control Language): Dùng để quản lý quyền truy cập vào dữ liệu
- TCL (Transaction Control Language): Dùng để quản lý các giao dịch trong SQL

### DQL

- SELECT : Truy xuất dữ liệu từ một hoặc nhiều bảng

### DML

- INSERT INTO :Thêm 1 or nhiều hàng mới vào bảng
- UPDATE : Cập nhật, thay đổi dữ liệu của các hàng đã có trong bảng
- DELETE : Xóa 1 or nhiều hàng khỏi bảng

### DDL

- CREATE : Tạo mới bảng
- ALTER : Sửa đổi cấu trúc của một đối tượng đã có
- DROP : Xóa hoàn toàn 1 đối tượng
- TRUNCATE : Xóa toàn bộ dữ liệu trong một bảng 1 cách nhanh chóng

### DCL

- GRANT :Cấp quyền cho người dùng or nhóm người dùng
- REVOKE : Thu hồi quyền đã cấp

### TCL

- BEGIN, START TRANSACTION : Bắt đầu một giao dịch
- COMMIT : Lưu vĩnh viễn tất cả các thay đổi đã thực hiện trong giao dịch hiện tại
- ROLLBACK : Hủy bỏ tất cả những thay đổi trong giao dịch hiện tại, đưa CSDL trở về trạng thái trước khi giao dịch

## Thứ tự thực hiện Query

```
SELECT ...
FROM ...
JOIN ...
WHERE ...
GROUP BY ...
HAVING ...
ORDER BY ...
LIMIT ...
```

1. `FROM` và `JOIN` : Xác định và nạp dữ liệu từ các bảng nguồn thành 1 bảng ảo
2. `WHERE` : Lọc các hàng trong bảng ảo vừa tạo dựa trên các điều kiện chỉ định
3. `GROUP BY` : Nhóm các hàng đã được lọc lại thành các nhóm dựa trên giá trị cảu 1 or nhiều cột. Mỗi nhóm sẽ được coi
   như là 1 hàng duy nhất
4. `HAVING` : Lọc các nhóm theo điều kiện
5. `SELECT` : Chọn ra các trường sẽ được hiển thị, các hàm `SUM()`, `COUNT()`,... cũng được tính toán ở bước này
6. `DISTINCT` : Loại bỏ các hàng trùng lặp khỏi kết quả
7. `ORDER BY` : Xắp xếp tập kết quả theo 1 or nhiều cột
8. `LIMIT` / `OFFSET` : Lấy ra một số lượng hàng nhất định từ tập trước đó

```
-- Lấy ra tổng số hàng hóa theo status với những hàng hóa có quantity > 50 và sort giảm dần 

select p.product_status, SUM(p.quantity) as "Tong"
from product p
where p.quantity > 50
group by p.product_status
order by sum(p.quantity) desc 

```

### JOIN

- `INNER JOIN` : Trả về các bản ghi có giá trị khớp nhau ở cả 2 bảng nếu chúng trùng nhau điều kiện nối - là phép toán
  lấy giao của 1 tập dữ liệu
- `LEFT JOIN` or `LEFT OUTER JOIN` : Trả về tất cả bản ghi của bảng bên trai và các bản ghi khớp từ bảng bên phải
  Ví dụ : Bảng B (shop) có 1 record có shop_id = 999 nhưng bên A (product) không có record nào có shop_id == 999 nên khi
  left join record đó bên A không được join vào
  ```

    select * from product
    left join shop on product.shop_id = shop.shop_id
    ```
- `RIGHT JOIN` or `RIGHT OUTER JOIN` : Trả về tất cả các bản ghi của cột bên phải và các bản ghi khớp từ cột bên trái (
  ngược với left join)
- `FULL OUTER JOIN` : Trả về tất cả các hàng ở 2 bên, nếu record nào bên A có mà B không có hoặc ngược lại, phần không
  có sẽ được gán = **null**

### Sub Query (Select lồng)

> Truy vấn lồng (nested query or sub query) là 1 select được lồng trong 1 câu lệnh khác, khi đó câu select chính là
> outer query

Sub query có nhiều dạng thể hiện:

- Scalar Subquery (Truy vấn vô hướng) : Trả về chính xác 1 hàng or 1 cột duy nhất

```
select p.product_id, (select avg(p.quantity) from product p)
from product p
```

Result :

```
id , avg(quantity)
prod_01,82
prod_02,82
prod_03,82
prod_04,82
prod_05,82
prod_06,82
prod_07,82
prod_08,82
prod_09,82
prod_10,82
```

- Multi-row query : Trả về một cột nhiều hàng

```
-- select pro id, shop id với các shop còn hoạt động 
select p.product_id, p.shop_id
from product p
where p.shop_id in (select s.shop_id from shop s where s.shop_status like 'ACTIVE')
```

- Multi-column Query : Trả về nhiều cột, nhiều hàng

```

select p.product_id, p.shop_id
from product p
where exists (select s.shop_id, s.updated_at from shop s where s.shop_status like 'ACTIVE' and s.updated_at = p.updated_at);
```

#### EXISTS

> Trả về TRUE nếu subquery trả về ít nhất 1 dòng, FALSE nếu ngược lại

### Correlated & Non-correlated SubQuery

- Non-Correlated : Sub query chạy độc lập, không phụ thuộc vào bất kỳ giá trị nào của query cha - là subquery được thực
  hiện
  1 lần duy nhất, kết quả của nó được cache lại để outer query sử dụng
- Correlated : Sub query phụ thuộc vào một hoặc nhiều giá trị của outer query (thường là trong `WHERE`) - là subquery
  được thực hiện lặp đi lặp lại một lần cho mỗi hàng của outer

Thường thì nên sử dụng Non-correlated subquery để tối ưu hiệu năng truy vấn, với Correlated subquery, các giải pháp như
JOIN và CTE hiệu quả hơn

### CTE

> CTE(Common Table Expression) là một tập kết quả read-only, chỉ tồn tại trong ngữ cảnh của 1 câu lệnh SQL duy nhất, nó
> lưu data vào
> cache giúp cải thiện hiệu năng so với subquery

Cú pháp:

- Non-recursive
    ```
    WITH <cte_name> AS (<SQL>)
    SELECT...
    ```
- Recursive
    ```
  WITH <RECURSIVE> <cte_name> (
  <SQL>
  UNION ALL
  <SQL>
  ) 
  SELECT ... 
  ```

Lưu ý rằng bản thân CTE trả về 1 bảng dữ liệu tạm thời, vì vậy các phép so sánh trực tiếp như `IN`, `=` không hợp lệ, ta
phải select vào CTE đó:

```
-- select pro id, shop id với các shop còn hoạt động
select p.product_id, p.shop_id
from product p
where p.shop_id in (select s.shop_id from shop s where s.shop_status like 'ACTIVE');


--c2
with shop_active as (select s.shop_id
                     from shop s
                     where s.shop_status like 'ACTIVE')
select p.product_id, p.shop_id
from product p
where p.shop_id in (select shop_id from shop_active);
--
```

Đệ quy với CTE:

```

```

### Ranking

> Ranking

#### ROW_NUMBER

> Row_number() là một hàm trong sql để xếp hạng các bản ghi trong 1 tập kết quả, không QUAN TÂM đến giá trị của các bản
> ghi đó là giống hay khác nhau

Cú pháp:

```
ROW_NUMBER() OVER (ORDER BY <column1>, <column2>,...)
```

Từ khóa `OVER` là bắt buộc
Về cơ bản `row_number()` sẽ đánh số tăng dần or giảm dần từ trên xuống dưới sau khi đã order by data, các giá trị giống
nhau cũng sẽ có rank khác nhau

Ví dụ:

```
select p.product_id,
       p.quantity,
       ROW_NUMBER() OVER (ORDER BY p.quantity DESC) AS Top
from product p
```

Res

```
prod_04,200,1
prod_06,120,2
prod_02,100,3
prod_08,90,4
prod_05,80,5
prod_10,70,6
prod_07,60,7
prod_01,50,8
prod_03,30,9
prod_09,20,10
```

#### RANK

> Rank() cũng là 1 hàm ranking, nó gán 1 thứ hạng cho mỗi bản ghi trong 1 tập kết quả dựa vào 1 tiêu chí sắp xếp cụ thể

Cú pháp

```
RANK() OVER (ORDER BY <column1>, <column2>,...)
```

Ví dụ

```
select p.product_id, p.quantity, RANK() OVER (ORDER BY p.quantity DESC) AS TOP
from product p;
```

Cơ chế ranking của `RANK()` giống như cơ chế xếp hạng trong thể thao, 2 người cùng thành tích tốt nhất sẽ cùng 1 hạng,
người tiếp theo sẽ có hạng 3, tương tự ...

#### DENSE_RANK

> Dense_rank() là hàm ranking tương tự 2 hàm trên, nhưng nó không bỏ qua số xếp hạng khi có các giá trị bằng nhau

Cú pháp

```
DENSE_RANK() OVER (ORDER BY ...)
```

Đơn giản là `DENSE_RANK()` giống `RANK()` nhưng thay vì phải bỏ qua số thứ tự khi có 2 hay nhiều người cùng giá trị,
`DENSE_RANK` vẫn sẽ đánh trùng rank và đánh theo thứ tự mà không bỏ
Ví dụ

```
select p.product_id, p.quantity, DENSE_RANK() OVER (ORDER BY p.quantity DESC) AS TOP
from product p;
```

### View

> View bản chất là một câu lệnh SQL SELECT được lưu trữ trong CSDL, được coi như một bảng ảo và không lưu trữ bất kỳ dữ
> liệu nào của riêng nó

Khi ta truy vấn 1 view, hệ quản trị CSDL sẽ thực thi câu lệnh select đã được lưu của View đó tại chính thời điểm truy
vấn và trả về kết quả

Cú pháp:

```
CREATE OR REPLACE VIEW <view_name> AS 
    SELECT...
```

Ví dụ

```
-- select địa chỉ kiểu 12... của tất cả user còn ACTIVE
create view all_customer_active_login as
select u.user_id, u.username, u.address
from users u
where u.status = 'ACTIVE';

select u.address
from all_customer_active_login u
where u.address like '12%'
```

Đặc điểm:

- View như một bảng ảo lưu trữ data (định nghĩa câu lệnh SELECT) tồn tại vĩnh viễn cho đến khi bị xóa
- View là dữ liệu động, không như Temp Table
- Toàn bộ CSDL có thể truy cập View, Temp Table chỉ được truy cập ở phiên của nó. Mọi user đều có quyền xem View
- Hiệu năng phụ thuộc vào truy vấn gốc, chạy lại mỗi lần gọi View

CRUD trên View:
Về bản chất có thể thực hiện Update, Insert, Delete,... trên View, nhưng phải đáp ứng đủ các điều kiện:

1. View không được phép JOIN nhiều bảng, phải SELECT từ 1 bản duy nhất
2. Không được chứa các hàm tổng hợp như COUNT(), SUM(),...
3. Không được chứa các mệnh đề GROUP BY hoặc HAVING
4. Không được chứa DISTINCT
5. Không được chứa các hàm cửa sổ (Window Functions) như ROW_NUMBER(), RANK(), ...
6. Không được chứa các toán tử tập hợp như UNION, INTERSECT,...
7. Danh sách SELECT không được chứa các giá trị tính toán và hằng số
8. Câu lệnh SELECT không được chứa CTE Recursive (WITH RECURSIVE)

Các điều kiện này được đưa ra để nhằm đảm bảo 1 nguyên tắc nền tảng và quan trọng nhất của 1 Hệ quản trị CSDL : **Tính
toàn vẹn và nhất quán của dữ liệu**

Một thao tác trong số các điều kiện trên cũng đủ để gây ra sự mơ hồ về mục tiêu của SQL, nó không biết nên dùng giải
pháp nào để CRUD mặc dù nó hiểu lệnh SQL đó
Ví dụ với JOIN, khi ta có 1 View kết hợp từ 2 bảng order và customer. Ta muốn delete 1 record trong View, SQL sẽ mơ hồ
trong việc làm sao để đạt được mục tiêu, nó có thể xóa đơn hàng trong order, hoặc cũng có thể xóa customer tương ứng
trong customer, hoặc cũng có thể xóa cả 2 ???

Tương tự với các điều kiện còn lại

**INSTEAD OF Trigger** - Giải pháp tạm thời cho những View phức tạp
Trigger cho phép ta thực hiện các logic tùy chỉnh thay vì thực thi CRUD
trực tiếp trên View

### PROCEDURE

> Procedure hay Stored Procedure là một tập hợp các câu lệnh SQL được đặt tên và lưu trữ sẵn trong CSDL, nó có thể nhận
> các params đầu vào, xử lý 1 loạt các hành động chuỗi phức tạp và trả về các tham số đầu ra hoặc mã trạng thái

Nó tương tự như 1 phương thức trong lập trình

Mục đích

- Đóng gói logic nghiệp vụ : Gom nhóm 1 loạt các thao tác như chuyển tiền, tạo đơn hàng mới,... vào 1 đơn vị duy nhất -
  nghiệp vụ được tập trung lại, dễ bảo trì
- Giảm lưu lượng mạng : Thay vì phải gửi cả chục query qua lại giữa ứng dụng và CSDL, ta chỉ cần 1 lệnh gọi Procedure.
  Thêm vào đó, lần đầu gọi Procedure, SQl sẽ tạo ra 1 execution plan, các lần gọi tiếp theo có thể thực hiện theo plan
  này giúp tăng hiệu năng
- Tăng cường bảo mật và tính tái sử dụng : Thay vì phải cấp quyền để CRUD trên nhiều bảng trong 1 nghiệp vụ, ta chỉ cần
  cấp quyền thực thi 1 Procedure, giúp ngăn chặn SQL injection và truy cập trái phép dữ liệu
- Quản lý transaction : Procedure là 1 công cụ đóng gói hữu ích cho Transaction

Cú pháp

Ví dụ

### Chú thích

1. Execution plan: Một lộ trình chi tiết, từng bước một do Query Optimizer tạo ra, lộ trình này mô tả chuỗi các thao tác
   mà CSDL sẽ thực hiện với data. Query Optimizer là hạt nhận trong Execution Plan khi nó tiến hành **Phân tích cú pháp
   **, **Tạo các kế hoạch thực thi khả dụng**, **Tính toán chi phí cho từng kế hoạch và chọn cái tốt nhất**
   Execution có dạng tree, mỗi node trong tree là 1 toán tử, một số toán tử phổ biến như:
    - `Sequential Scan` : Đọc toàn bộ bảng từ đầu đến cuối, hàng này qua hàng khác, kiểm tra xem mỗi hàng có thỏa mãn
      điều kiện `WHERE` hay không
    - `Index Scan` : Sử dụng 1 index để tìm kiếm các hàng cần thiết trên đĩa, sau đó nhảy đến và lấy data
    - `Index-only Scan` : Tương tự như `Index Scan` nhưng tất cả thông tin data đều có sẵn trong index, ta không cần
      truy cập vào bảng chính nữa
    - `Nested Loop Join` : Duyêt qua từng phần tử của bảng ngoài, và với mỗi phần tử đó, tìm các hàng khớp trong bảng
      con
    - `Hash Join` : Đọc bảng nhỏ hơn và xây dựng Hash Tabla trong bộ nhớ, sau đó đọc bảng ngoài và dò tìm trong Hash
      Table để tìm các cặp khớp
    - `Merge Join` : Sắp xếp cả 2 bảng theo cột nối, sau đó duyệt qua chúng đồng thời để tìm ra các cặp khớp
    - `Sort` : sort data, tốn tài nguyên
    - `Aggregate` : Thực hiện các hàm tổng hợp `COUNT()`, `SUM()` trong `GROUP BY` clause