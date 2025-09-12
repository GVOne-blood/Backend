# ORM

## 1. ORM là gì ? Sử dụng ORM mang lại lợi ích như thế nào cho ứng dụng. Cơ chế hoạt động của ORM như thế nào? So sánh performance của việc sử dụng ORM vs JDBC

## 2. Spring JPA có phải là 1 triển khai của ORM hay không ?

### - Tìm hiểu các loại quan hệ trong JPA : Many To One, One To Many, Many To Many

### - Các loại Cascade type, Fetch type trong JPA

### - Tìm hiểu cách convert DTO -> Entity và ngược lại sử dụng 2 cách : dùng thư viện và sử dụng java reflection

## 3. Advance

### - Native query

### - Specification, Paging

### - Lấy dữ liệu khi JOIN nhiều bảng sử dụng JPA (mỗi bảng lấy 1 tới 2 trường thông tin)

[Chú thích](#chú-thích)

Trong lập trình, sự bất tương đồng trở kháng Hướng đối tượng - Quan hệ, về cơ bản là những sự khác biệt cơ bản, sâu sắc
trong cách mô hình hóa dữ liệu giữa OOP và DB. Sự khác biệt này bao gồm :

- Sự khác biệt về cấu trúc
- Sự khác biệt về định danh : Trong java, 2 obj được phân biệt với nhau qua định danh trong bộ nhớ. Trong DB, 1 hàng
  được xác nhận duy nhất bằng PK. 2 đối tượng khác nhau trong bộ nhớ hoàn toàn có thể biểu thị 1 hàng trong DB
- Sự khác biệt về mối quan hệ
- Sự khác biệt về kiểu dữ liệu : Một số kiểu dữ liệu của Java không map hoàn toàn 1:1 với DB

## ORM (Object Relational Mapping)

> ORM là một kỹ thuật lập trình, một khái niệm, một giải pháp kiến trúc để chuyển đổi dữ liệu giữa một hệ thống Hướng
> đối tượng và một CSDL

Về bản chất, ORM là một lớp trừu tượng đứng giữa application và DB, mục đích của nó :

- Cho phép tương tác với DB bằng các thao tác trên đối trượng Java thuần túy POJO(Plain Old Java Object) thay vì viết
  các câu lệnh SQL
- Tự động xử lý việc chuyển đổi giữa các đối tượng Java và row trong table, bao gồm cả các mối quan hệ phức tạp và các
  kiểu dữ liệu khác nhau

ORM giải quyết sự bất tương đồng trở kháng, để cho dev tập trung vào việc build logic nghiệp vụ

Các cơ chế trong ORM:

- **Mapping** : ORM cần biết đơi tượng java này thì ứng với bảng nào, thuộc tính này ứng với thuộc tính nào, việc này
  thường
  được thực hiện thông qua :
    - Metadata : Các thông tin cấu hình ánh xạ, có thể được nạp bằng các annotation (`@Entity`, `@Table`, `@Column`)
      hoặc bằng các file cấu hình XML riêng biệt
    - Quy ước : Một số ORM có thể tự động ssuy ra ánh xạ dựa vào tên đôis tượng. Ví dụ Object Product

ORM có nhiều triển khai trong Java, mặc định và phổ biến nhất là **Hibernate** , JPA xác định dựa trên Hibernate, Spring
Data JPA nằm trên JPA do có thằng phân tích cú pháp

```
+---------------------------------------------------+
|      Ứng dụng của bạn (Service, Controller)       |
|      (Tương tác với UserRepository)               |
+---------------------------------------------------+
                   ^
                   |
+---------------------------------------------------+
|      Spring Data JPA (JpaRepository, ...)         |  <-- Lớp trừu tượng của Spring
|      (Tự động tạo DAO, suy luận query)            |
+---------------------------------------------------+
                   ^ (Sử dụng bên dưới)
                   |
+---------------------------------------------------+
|      JPA (EntityManager, @Entity, JPQL)           |  <-- Đặc tả chuẩn của Java
|      (Định nghĩa API và quy tắc)                  |
+---------------------------------------------------+
                   ^ (Được triển khai bởi)
                   |
+---------------------------------------------------+
|      ORM Provider (Hibernate, EclipseLink)        |  <-- Triển khai ORM thực sự
|      (Quản lý Session, sinh SQL, caching...)      |
+---------------------------------------------------+
                   ^ (Sử dụng bên dưới)
                   |
+---------------------------------------------------+
|      JDBC (Java Database Connectivity)            |
+---------------------------------------------------+
                   ^
                   |
+---------------------------------------------------+
|                   Cơ sở dữ liệu                   |
+---------------------------------------------------+
```

## JPA

### JPA relationship

> Ownership : Trong DB, một mối quan hệ giữa 2 bảng luôn được duy trì bởi 1 khóa ngoại nằm ở 1 trong 2 bảng. Bảng chứa
> khóa ngoại là owner side trong mối quan hệ. Trong JPA, entity tương ứng với bảng chứa khóa ngoại là owner entity.
> Ngược lại là Inverse side

> Directionality
>> - UniDirectional (Định hướng 1 chiều) : Mối quan hệ chỉ được biết đến từ 1 phía
>> - Bidirectional (Định hướng 2 chiều) : Mối quan hệ 2 chiều, 2 bên bảng đều biết nhau và hình thành quan hệ

### Chiến lược tải và lan truyền dữ liệu

#### Fetch type (Chiến lược tải dữ liệu)

> Fetch type là 1 attribute trong các mối quan hệ trong JPA, nó quyết định khi nào sẽ tải dữ liệu của các entity liên
> quan từ cơ sở dữ liệu.

Có 2 loại fetch type:

- `FetchType.LAZY` : Khi tải 1 entity A, JPA sẽ chỉ tải dữ liệu của entity A, các trường giá trị quan hệ khác không được
  tải lên ngay lập tức mà được giả mạo bởi `proxyCollection`, khi gọi đến những field đó thì proxy mới thực hiện câu
  select xuống lấy data.
  Ví dụ, `author` có quan hệ 1 - N với `book`

  Khi ta thực hiện
    ```
  Optional<Author> authorOpt = authorRepository.findById(1L);
    ```

  Thằng Hibernate nó sẽ chỉ thực hiện query :
    ```
  SELECT a.id, a.name FROM authors a WHERE a.id = 1;
    ```
  Nó không hề JOIN bảng `book` , vì nó là không cần thiết. Bây giờ trong bộ nhớ Java, đối tượng `Author` vừa lấy được sẽ
  kiểu :
    ```
    author.id = 1
    author.book = ...
    ```
  Tất nhiên nó không thể có data được, nhưng thằng Hibernate tạo ra một đối tượng đặc biệt là **Proxy Collection**
  nó chứa tất cả các field của book, nhưng tất cả đều null trừ id.
  Chỉ khi ta dùng lệnh truy cập vào bên trong field `book` của entity `author`, nó sẽ thực hiện sql kiểu dạng :
    ```
  SELECT b.id, b.title, b.author_id FROM books b WHERE b.author_id = 1;
    ```
  Yé nó không JOIN, kết quả được nhét vào trong Proxy, và nó trở thành 1 object thật kiểu có dữ liệu thật

Owner side là bên chịu trách nhiệm quản lý mối quan hệ, mọi thay đổi
về mối quan hệ đều được thực hiện ở owner side để JPA có thể cập nhật đúng cột khóa ngoại trong DB

Hệ lụy của LAZY là 1 exception khi truy cập vào Proxy Collection khi session Hibernate đã đóng, nó sẽ ném ra
`LazyInitializationException`.

    Default :
    ```
    @OneToMany: LAZY
    @ManyToMany: LAZY
    ```

- `FetchType.EAGER` : Khi tải data của entity A, nó sẽ tải toàn bộ field ngay từ đầu, kể cả field khóa ngoại, nghĩa là
  nó sẽ left join luôn xuống SQL để lấy data
  Fetch type này thường không được khuyến khích vì nó dẫn đến các vấn đề về hiệu năng :
    - N + 1 problem : Ta có một ds entity cha và mỗi entity cha có quan hệ với 1 ds entity con, khi lấy data cuả 1 thằng
      cha, hibernate nó lấy lên bằng join, sau đó trong một thằng con lại có 1 attribute liên kết với thằng cha, và
      hibernate lại thực hiện thêm n query nữa (n là số bản ghi cha) vì vậy từ 1 query duy nhất đã có thêm N query thừa
      khác
    - Cartesian Product (tích đề - các) : Khi ta có nhiều field có mối quan hệ EARGE trên 1 entity, JPA sẽ tạo nhiều
      JOIN qua nhiều bảng, kết quả trả về từ DB là cực lớn với nhiều bảng lồng bảng

  Default :
    ```
    @ManyToOne: EAGER
    @OneToOne: EAGER
    ```

Best practice là `LAZY`

#### Cascade Type (Chiến lược lan truyền dữ liệu)

> Cascade quyết định các hành động thay đổi trạng thái của 1 entity có được lan truyền đến các entity liên quan hay
> không.

Các loại Cascade chính :

- `CascadeType.PERSIST` : Khi lưu 1 entity, các entity con cũng sẽ được tự động lưu. Hữu ích khi create cha và con cùng
  lúc
- `CascadeType.MERGE` : Khi update 1 entity cha đã bị detach, các thay đổi trên entity con cũng sẽ được cập nhật
- `CascadeType.REMOVE` : Khi xóa 1 entity cha, các entity con cũng sẽ tự động xóa theo
- `CascadeType.REFRESH` : Khi làm mới trạng thái của entity từ DB, các entity con cũng được cập nhật
- `CascadeType.DETACH` : Khi tách 1 entity cha ra khỏi `Persistence Context` , các entity con cũng sẽ bị tách theo
- `CascadeType.ALL` : áp dụng tất cả

### Chú thích