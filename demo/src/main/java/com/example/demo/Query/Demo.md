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
  hiện 1 lần duy nhất, kết quả của nó được cache lại để outer query sử dụng
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

- **Đóng gói logic nghiệp vụ** : Gom nhóm 1 loạt các thao tác như chuyển tiền, tạo đơn hàng mới,... vào 1 đơn vị duy
  nhất - nghiệp vụ được tập trung lại, dễ bảo trì
- **Giảm lưu lượng mạng** : Thay vì phải gửi cả chục query qua lại giữa ứng dụng và CSDL, ta chỉ cần 1 lệnh gọi
  Procedure.
  Thêm vào đó, lần đầu gọi Procedure, SQl sẽ tạo ra 1 execution plan, các lần gọi tiếp theo có thể thực hiện theo plan
  này giúp tăng hiệu năng
- **Tăng cường bảo mật và tính tái sử dụng** : Thay vì phải cấp quyền để CRUD trên nhiều bảng trong 1 nghiệp vụ, ta chỉ
  cần cấp quyền thực thi 1 Procedure, giúp ngăn chặn SQL injection và truy cập trái phép dữ liệu
- Quản lý transaction : Procedure là 1 công cụ đóng gói hữu ích cho Transaction

Cú pháp

```
CREATE [ OR REPLACE ] PROCEDURE procedure_name ( [ [ argmode ] [ argname ] argtype [ { DEFAULT | = } default_expr ] [, ...] ] )
LANGUAGE plpgsql
AS $$
DECLARE
    -- (Tùy chọn) Khối khai báo biến
    variable_name data_type;
BEGIN
    -- Thân của procedure: chứa logic và các câu lệnh SQL
    -- ...

    [ EXCEPTION
        WHEN condition [ OR condition ... ] THEN
            -- (Tùy chọn) Khối xử lý lỗi
            -- ...
    ]
END;
$$;
```

Ví dụ

```
CREATE OR REPLACE PROCEDURE process_order(p_order_id INT)
LANGUAGE plpgsql
AS $$
BEGIN
    -- Giảm số lượng tồn kho
    UPDATE products
    SET stock = stock - od.quantity
    FROM order_details od
    WHERE products.id = od.product_id AND od.order_id = p_order_id;

    -- Cập nhật trạng thái đơn hàng
    UPDATE orders
    SET status = 'PROCESSED'
    WHERE id = p_order_id;

    -- Nếu mọi thứ thành công, commit giao dịch
    COMMIT;

EXCEPTION
    -- Bắt bất kỳ lỗi nào xảy ra (ví dụ: số lượng tồn kho âm)
    WHEN OTHERS THEN
        RAISE NOTICE 'An error occurred processing order %. Rolling back.', p_order_id;
        -- Hủy bỏ tất cả các thay đổi
        ROLLBACK;
END;
$$;

-- Cách gọi:
CALL process_order(12345);
```

Sự khác biệt giữa query của procedure và query trên application:

- Query trong procedure được biên dịch và tối ưu hóa trước khi lưu trữ nên nó có nhanh hơn 1 chút
- Procedure kiểm soát nghiệp vụ tại một nơi duy nhất, ví dụ, thay vì phải vào các service để đổi code nghiệp vụ, ta chỉ
  cần thay đổi procedure
- Trong môi trường doanh nghiệp, 1 db có thể được truy cập bởi nhiều ứng dụng và module khác nhau, việc mỗi module triển
  khai cùng 1 logic truy cập dữ liệu có thể dẫn đến sự trùng lặp và không nhất quán. Thay vào đó procedure sẽ tạo ra 1
  lớp abstract, các ứng dụng kh cần biết rõ cấu trúc của bảng bên dưới, chỉ cần biết tên procedure và các tham số truyền
  vào. Khi cấu trúc bảng thay đổi or logic thay đổi, ta sửa procedure mà các ứng dụng kh cần sửa gì
- Nhược điểm của procedure là khó kiểm soát phiên bản, ngày nay, việc ra đời
  flyway hay liquibase giúp điều này dễ dàng hơn
- Phụ thuộc vào DB vendor(nhà cung cấp DB) nên khó di chuyển giữa các hệ quản trị CSDL

### Transaction

> Một transaction là một chuỗi các thao tác SQL được thực thi như một đơn vị công việc logic duy nhất và liền mạch. Toàn
> bộ chuỗi thao tác này phải tuân thủ quy tắc: Hoặc là tất cả cùng thành công hoặc là không có gì thay dổi cả.

Mục đích cuối cùng của transaction để đảm bảo tính vẽn toàn và nhất quán của dữ liệu, đặc biệt trong môi trường có nhiều
người dùng hoặc nhiều tiến trình cùng truy cập và thay đổi dữ liệu đồng thời

Ví dụ kinh điển là thằng chuyển tiền

Các thuộc tính ACID: Để đảm bảo tính vẹn toàn, mọi transaction phải tuân thủ 4 thuộc tính nền tảng :

- **Atomicity** (Tính nguyên tử) : Để đảm bảo nguyên tắc all-or-nothing, toàn bộ giao dịch được coi là một thao tác
  nguyên tử duy
  nhất, nó không thể tồn tại ở trạng thái "hoàn thành 1 nữa" mà sẽ phải rollback hết.
- **Consistency** (Tính nhất quán) : Đảm bảo 1 giao dịch chỉ có thể đưa CSDL từ 1 trạng thái hợp lệ này sang 1 trạng
  thái hợp lệ khác. Mọi dữ liệu được ghi vào CSDL phải tuân thủ các quy tắc đã được định nghĩa như các constraints,
  triggers, ...
- **Isolation** (Tính cô lập) : Đảm bảo các giao dịch đang thực hiện đồng thời không can thiệp lẫn nhau, mỗi transaction
  không thể nhìn thấy kết quả của các giao dịch đồng thời đang thực hiện bên cạnh nó, nó giống việc thực hiện tuần tự
  các giao dịch.
- **Durability** (Tính bền vững) : Đảm bảo rằng khi 1 giao dịch xác nhận thành công, các thay đổi của nó phải được lưu
  vĩnh viễn và tồn tại ngay cả khi hệ thống gặp sự cố.

Cú pháp :
Tạo một transaction với `BEGIN` or `START TRANSACTION` , theo sau là 1 chuỗi các lệnh trong transaction

`COMMIT` để kết thúc transacion và lưu vĩnh viễn, sau khi commit thì các transaction khác sẽ nhìn thấy kết quả của
transaction này.

`ROLLBACK` để hủy bỏ transaction và hoàn tác tất cả những thay đổi đã được thực hiện kể từ `BEGIN` gần nhất. CSDL sẽ trả
về trạng thái chính xác ngay trước khi transaction bắt đầu.

Trong PostgreSQL, khối lệnh trong `BEGIN...END` sẽ từ động được commit hoặc rollback

Cơ chế quản lý transaction :

_ Cơ chế chính mà PostgreSQL sử dụng là MVCC(Multi-Version Concurrent Control) - điều khiển
đồng thời đa phiên bản

Nguyên lý : Khi thực hiện 1 lệnh `UPDATE` trên 1 hàng, postgre không ghi đè lên dữ liệu cũ. Thay vào đó nó tạo ra phiên
bản mới của hàng đó và đánh dấu phiên bản cũ là đã hết hạn. Lệnh `DELETE` cũng chưa xóa hàng đó mà đánh dấu nó là hàng
đã chết

Mỗi transaction khi bắt đầu sẽ được cấp 1 id unique, tăng dần. Mỗi phiên bản mới của hàng dữ liệu sẽ được
gắn với id của giao dịch đã tạo ra nó(`xmin`) và id của giao dịch đã cập nhật nó(`xmax`).

Khi một giao dịch bắt đầu, nó sẽ lấy 1 snapshot của CSDL, snapshot này là 1 danh sách các id đã commit tại thời điểm đó

Mỗi giao dịch sẽ chỉ nhìn thấy các phiên bản hàng thỏa mãn 2 điều kiện :

- `xmin` của hàng đó phải là 1 id đã commit và có trong snapshot của giao dịch
- `xmax` của hàng đó phải chưa được thiết lập, hoặc là 1 id chưa commit, hoặc là 1 id đã rollback

Kết quả của MVCC là nhiều giao dịch có thể đọc và ghi trên 1 bảng đồng thời mà ít bị xung đột

_ Cơ chế Write-Ahead Logging (WAL) - Cơ chế đảm bảo Atomicity và Durability:

Nói chung đây là cơ chế ghi log : Trước khi bất kỳ thay đổi nào được ghi vào các file dữ liệu trên ổ đĩa, một bản ghi mô
tả thay đổi đó được ghi xuống 1 file log tuần tự là WAL và được xác nhận là đã lưu an toàn trên đĩa

Thực hiện hóa Durability : KHi commit 1 giao dịch, postgre chỉ cần đảm bảo rằng tất cả bản ghi trong WAL liên quan đến
giao dịch đó đã được ghi thành công xuống đĩa, thao tác ghi log này rất nhanh, sau đó việc cập nhật các file dữ liệu
chính có thể được thực hiện không đồng bộ sau. Nếu server bị sập, khi khởi động lại,postgre sẽ đọc lại WAL từ checkpoint
cuối cùng và phát lại các thay đổi của commit cuối cùng mà chưa được ghi vào đĩa (do đang ghi thì mất điện đồ), đảm bảo
không mất dữ liệu

Postgre định dạng các loại cô lập khác nhau:

- `READ COMMITED` (default) : Mỗi câu lệnh trong transaction sẽ lấy 1 snapshot mới, transaction này có thể thấy các thay
  đổi được commit bởi các giao dịch khác trong khi nó đang chạy
- `REPEATABLE READ` : Toàn bộ transaction chỉ sử dụng 1 snapshot duy nhất, đảm bảo dữ liệu được xử lý sẽ không thay đổi
  trong suốt giao dịch
- `SERIALIZABLE` : Đảm bảo kết qủa của giao địch đồng thời giốn hệt khi chúng chạy tuần tự.

Cú pháp thiết lập isolate

```
SET TRANSACTION ISOLATION LEVEL READ COMMIED
```

`@Transaction` trong java :

Khi đánh dấu 1 phương thức là một transaction, method đó sẽ được bao bọc bởi 1 transaction trong DB, nếu trong quá tình
thực hiện hệ thống không ném ra `RuntimeException` thì sẽ commit giao dịch, còn không sẽ rollbakc

Ngày xưa chưa có annotation thì kiểm soát transaction trong java thường được sử dụng với `Transaction` object, sau này
`@Transaction` ra đời giusp loại bỏ các khối try-catch trong quá trình viết code transaction

Cơ chế hoạt động của `@Transaction` được thực hiện hóa bởi Spring AOP(Aspect-Oriented Programming) và Proxy:

1. Spring component scan, những class có phương thức được đánh dấu bởi `@Transaction` sẽ chưa được inject bean vội mà sẽ
   được bọc trong 1 đối tượng Proxy. Khi 1 bean gọi đến phương thức này, thực chất nó đang gọi đến Proxy object
2. Proxy object chứa logic quản lý giao dịch, nó sẽ:
    - Chặn lời gọi
    - Bắt đầu một giao dịch
    - Gọi phương thức thực tế dựa trên bean gốc
    - Chặn kết quả trả về (có thể là giá trị or exception)
    - Thực hiện COMMIT or ROLLBACK dựa trên kết quả trả về
    - Trả kết quả (or ném exception) cho bên gọi

Proxy chỉ hoạt động trên các method *public*, không hoạt động trên private, protected,... Lời gọi phương thức phải đến
từ ngoài đối tượng, vì gọi từ bên trong thì chả khác gì thực hiện gọi 1 hàm bình thường, vì proxy nó bao cả lớp đó, phải
đi qua proxy mới transaction được

Attribute trong `@Transaction` :

- `propagation` (lan truyền): Xác định cách một transaction hoạt động khi nó được gọi từ 1 transaction(phương thức có
  `@Transaction`) khác:
    - `REQUIRED` (default) : Nếu có một giao dịch đâng hoạt động, nó sẽ tham gia vào giao dịch đó. Nếu chưa có nó sẽ bắt
      đầu 1 giao dịch mới
    - `REQUIRES_NEW` : Luôn bắt đầu giao dịch mới, tạm ngừng giao dịch hiện, nos thích hợp cho các giao dịch mà ta luôn
      muốn nó phải thành công dù bị rollback
    - ...
- `isolation` (mức độ cô lập) : Tham số các thứ Giống với SQL
- `readOnly` : nếu giá trị = `true`, giao dịch này sẽ không thực hiện bất kỳ thao tác ghi nào
- `rollbackFor` & `noollbackFor` : Kiểm soát chính xác exception nào gây ra rollback
    - default Spring chỉ rollback cho các runtime exception và error, nó không rollback cho các checked exception.
    - `rollbackFor` cho phép chỉ định 1 danh sách các ngoại lệ kể cả checked có thể rollback
    ```
  @Transactional(rollbackFor = {SQLException.class, CustomCheckedException.class})
    public void someMethod() throws SQLException, CustomCheckedException { ... }
  ```
    - `noRollbackFor` chỉ định danh sách `RuntimeException` ta không muốn Spring kích hoạt rollback

### Chú thích

1. **Execution plan**: Một lộ trình chi tiết, từng bước một do Query Optimizer tạo ra, lộ trình này mô tả chuỗi các thao
   tác mà CSDL sẽ thực hiện với data. Query Optimizer là hạt nhận trong Execution Plan khi nó tiến hành **Phân tích cú
   pháp
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