# ORM

## 1. ORM là gì ? **Sử dụng
** ORM mang lại lợi ích như thế nào cho ứng dụng. Cơ chế hoạt động của ORM như thế nào? So sánh performance của việc sử dụng ORM vs JDBC

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
                   ^ (**Sử dụng** bên dưới)
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
                   ^ (**Sử dụng** bên dưới)
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

## Advance

### Native query

### JDBC (Java DataBase Connectivity)

> JDBC là 1 API được định nghĩa trong Java, nó cung cấp 1 tập hợp các interface và class để giúp java giao tiếp với CSDL
> 1 cách đồng nhất

JDBC ra đời để đồng nhất cách viết truy vấn vì mỗi Hệ quản trị SQL sẽ có cú pháp khá khác nhau, JDBC đóng vai trò biên
dịch

Thực hiện truy vấn JDBC có 7 bước :

```
String url;
String username;
String password;
Connection connection = null;
PreparedStatement preparedStatement = null;
ResultSet resultSet = null;

try{
Class.forName("driver");
connection = DriverManager.getConnection(url, username, password);
String sql = "SELECT id, name, price FROM products WHERE category_id = ? AND price > ?";
PreparedStatement preparedStatement = connection.preparedStatement(sql);

preparedStatement.setLong(1, 123L);
preparedStatement.setBigDecimal(...);

resultSet = preparedStatement.executeQuery();

List<Product> products = new ...

while (resultSet.next()){
Product product = new ...
product.setId(resultSet.getLong("id");
...
}
}catch (SQLException e){...} 
finally {...}


```

Việc này mang đến cho JDBC nhiều nhược điểm :

- Dài dòng
- Quên đóng connection dưới finally có thể gây lỗi
- Ta phải tự tay map từng field một từ bảng sang Java

Trong Spring framework, việc auto config các connection và statement được thực hiện vói JdbcTemplate

#### JDBC Template

> JDBC Template là một pattern, nó định nghĩa lại 1 số phương thức của JDBC mà không làm thay đổi thuật toán và cơ chế
> vốn có của nó

JDBC template tự động tạo connection pool, tạo `PreparedStatement`, thực hiện query, đóng tất cả tài nguyên tự động

`JdbcTemplate` sẽ bắt SQLException và dịch nó về 1 trong các unchecked
của Spring trong hệ thống `DataAccessException`

`JdbcTemplate` cung cấp interface `RowMapper` với method `mapRow(ResultSet rs, int rowNum)` chịu trách nhiệm map 1 dòng
thành 1 đối tượng trong Java. `JdbcTemplate` sẽ tự lặp và mapRow khi nào hết dòng.

Ngoài `RowMapper` thì còn `ResultSetExtractor` nhưng phức tạp hơn, no nhận toàn bộ `ResultSet` và quản lý nó

Nếu lười không muốn implements thằng `RowMapper`, Spring cho ta 1 class `BeanPropertyRowMapper`, nó tự động map các
field cùng tên giữa table và class

Một số method của `JdbcTemplate` :

- `queryForObject()` : Khi chắc chắn query sẽ trả về 1 dòng. Nếu trả về 0 dòng, nó ném
  `EmptyResultDataAccessException`, nếu trả về nhiều hơn 1 dòng, nó ném `IncorrectResultSizeDataAccessException`
    ```
    jdbcTemplate.queryForObject("SELECT name FROM products WHERE id = ?", String.class, 123L)
  ```

- `query()` : Khi query trả về 0 or nhiều dòng
    ```
  jdbcTemplate.query("SELECT * FROM products WHERE category_id = ?", new BeanPropertyRowMapper<>(Product.class), 1L);
  ```
- `queryForList()` : Tương tự query nhưng trả về `List<Map<String, Object>>` or List đơn giản
    ```
  List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM products");
  ```
- `update()` : Thực hiện bất kỳ câu lệnh thuộc nhóm lệnh DML, trả về 1 giá trị kiểu `int` chỉ số dòng đã bị modify.
    ```
  int rowsAffected = jdbcTemplate.update("UPDATE products SET price = ? WHERE id = ?", newPrice, productId);
  ```
- `execute()` : Phương thức chung, thực hiện bất kỳ lệnh SQL nào

#### NamedParameterJDBCTemplate

> NamedParameterJDBCTemplate là một class Wrapper JDBCTemplate, sử dụng tham số được đặt tên truyền vào truy vấn thay
> cho ký tự ?

Bản chất Named cũng thực thi SQL giống template, nó sẽ phân tích chuỗi SQL và tìm các tham số được đặt tên ta đã định
nghĩa, sau đó nó thay thế câu SQL bằng 1 câu hoàn toàn mới với tham số được truyền vào thay bằng ? và ghi nhớ thứ tự
tham số bằng cách nhìn vào thằng `Map` or `SqlParameterSource` rồi cuối cùng nó gọi phương thức của JDBC Template bên
trong.

1 hàm tìm sản phẩm theo giá cơ bản

```

    public Page<Product> findByPrice(BigDecimal from, BigDecimal to, Pageable pageable) {
        String sql = " SELECT * FROM product WHERE price > :from and price < :to " +
                        "ORDER BY product_id " +
                        "LIMIT :limit OFFSET :offset ";
        String sqlCount = " SELECT COUNT(*) FROM product WHERE price > :from and price < :to ";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("from", from)
                .addValue("to", to)
               // .addValue("sort", pageable.getSort().toString())
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        SqlParameterSource paramsCount = new MapSqlParameterSource()
                .addValue("from", from)
                .addValue("to", to);

        List<Product> products 
         = namedParameterJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(Product.class));

        Integer total = namedParameterJdbcTemplate.queryForObject(sqlCount, paramsCount, Integer.class);

        return new PageImpl<>(products, pageable, total == null ? 0 : total);
    }
```

### Paging

### Criteria

> JPA Criteria là 1 API được định nghĩa trong JPA, cho phép ta xây dựng câu truy vấn một cách programming - nghĩa là
> không cần viết query, vì nó sử dụng các đối tượng Java thay vì SQL

Mục đích của Criteria là tạo ra các truy vấn an toàn kiểu (type-safe), truy vấn động linh hoạt, tránh các lỗi về cú pháp
chỉ được phát hiện lúc runtime

Thành phần của Criteria API:

- `CriteriaBuilder` : Đối tượng tạo ra các thành phần của truy vấn thông qua các method :
    - `createQuery(Class<T> resultClass)`: Tạo một đối tượng CriteriaQuery.
    - `equal(expression1, expression2), notEqual(...)`
    - `like(expression, pattern), notLike(...)`
    - `gt(), ge(), lt(), le()` (greater than, greater than or equal, ...)
    - `between(expression, start, end)`
    - `in(expression)`
    - `isNull(expression), isNotNull(...)`
    - `and(predicate1, predicate2, ...)`
    - `or(predicate1, predicate2, ...)`
    - `lower(expression), upper(...), count(...), avg(...)`
- `CriteriaQuery<T>` : Đại diện cho câu truy vấn **SELECT** , generic là kiểu data cho dữ liệu trả về
    - `from(Class<E> entityClass)`: Chỉ định mệnh đề FROM và trả về một đối tượng Root.
    - `select(selection)`: Chỉ định mệnh đề SELECT.
    - `where(predicate)`: Chỉ định mệnh đề WHERE.
    - `orderBy(order)`: Chỉ định mệnh đề ORDER BY.
    - `groupBy(expression)`: Chỉ định mệnh đề GROUP BY.
- `Root<T>` : Đại diện cho 1 entity trong mệnh đề **FROM**, nó là điểm bắt đầu để điều hướng đến các thuộc tính của
  entity
    - `get(String attributeName)`: Lấy một thuộc tính của entity (ví dụ: root.get("name")). Đây là cách không an toàn
      kiểu.
    - `get(SingularAttribute<T, Y> attribute)`: Lấy thuộc tính bằng cách sử dụng JPA Metamodel. Đây là cách an toàn
      kiểu.
    - `join(String attributeName)`: Thực hiện JOIN đến một quan hệ.
- `Predicate` : Đại diện cho các điều kiện trong **WHERE**, nó chỉ trả về *true* or *false*

`CriteriaBuilder` được lấy từ `EntityManager`,
`CriteriaQuery` được lấy từ `CriteriaBuiler`,
`Root` được lấy từ `CriteriaQuery`,
`Predicate` là các hàm điều kiện trong `CriteriaBuilder`

### Specification

> Về lý thuyết, Specification là 1 design pattern mô tả 1 tiêu chí hoặc 1 quy tắc nghiệp vụ
> có thể được kết hợp với các quy tắc khác. Nó là một lớp trừu tượng hóa cấp cao được xây dựng trên nền tảng JPA
> Criteria API
>> Trong bối cảnh JPA, Specification là một interface cho phép ta xây dựng các điều kiện truy vấn
> > một cách tường minh và dễ tái sử dụng

Một Specification là 1 object đại diện cho 1 phần của mệnh đề where trong truy vấn. Thay vì viết các câu lệnh SQL hay
JSQL, ta sử dụng JPA Criteria API để định nghĩa các điều kiện này

Khả năng kết hợp của Specification là cao khi ta có thể kết hợp các câu điều kiện nhỏ thành 1 câu dk lớn thích hợp

Interface Specification chỉ có 1 method duy nhất :

```
public interface Specification<T> {
    // Phương thức này nhận vào 3 đối tượng từ Criteria API
    // và trả về một Predicate (điều kiện WHERE)
    Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder);
}
```

### Đánh giá

#### JDBC Template

Bản chất chỉ là 1 util của Spring để loại bỏ 1 số nhược điểm của JDBC thuần

**Ưu điểm** :

- Toàn quyền kiểm soát SQL
- Hiệu năng cao, gần như JDBC thuần
- An toàn với tự động đóng tài nguyên và convert SQLException

**Nhược điểm** :

- Không type-safe : Query chỉ báo lỗi trong quá trình Runtime
- Tham số vị trí ? gây khó khăn khi số lượng tham số lớn
- Phụ thuộc vào CSDL cái này hiếm
- Mapping thủ công nhưng ít ra nó có đối tượng để map field giống chính tả

**Sử dụng** cho :

- Các câu query phức tạp
- Store procedure

#### NamedJDBCTemplate

Bản cải tiến cảu JDBCTemplate với việc đặt tên tham số

**Ưu điểm** :

- Tất nhiên là dễ đọc và bảo trì hơn
- An toàn hơn tý
- Kế thừa toàn bộ ưu điểm của thằng trên

**Nhược điểm** :

- Tất nhiên trừ dễ bảo trì hơn thì kế thừa toàn bộ nhược điểm của thằng trên

**Sử dụng** :

- Thay luôn JDBCTemplate

#### JPA

Bản chất là 1 bản đặc tả cho ORM với Hibernate là implements phổ biến nhất

**Ưu điểm** :

- Năng suất phát triển cao vì chỉ cần gọi hàm
- Độc lập với CSDL chỉ cần đổi url và dialect
- Tư duy hướng đối tượng với ORM
- Tích hợp caching, lazyload,...

**Nhược điểm** :

- Hiệu năng : Vì có 1 lớp trừu tượng hóa ở dưới
- Khó kiểm soát câu SQL sẽ được sinh ra
- Truy vấn phức tạp thì hơi móm

**Sử dụng** : all

#### JPA Criteria API

Bản chất cũng là 1 đặc tả trong JPA để xây dựng câu truy vấn động để đảm bảo type-safe và nhìn dễ

**Ưu điểm** :

- Type-safe tất nhiên khi chưa cần query đã biết lỗi rồi
- Dynamic nên nó linh hoạt
- **Ưu điểm** JPA

**Nhược điểm** :

- Dài dòng

**Sử dụng** :
Gần như chả ai dùng, làm nền cho thằng Spec

#### JPA Specification API

Bản chất là lớp trừu tượng nằm trên thằng Criteria

**Ưu điểm** :

- Mọi ưu điểm Criteria
- Ngắn gọn hơn nhiều
- Có thể kết hợp các điều kiện
- Tích hợp liền mạch với Paging và Sorting

**Nhược điểm** :
Query phức tạp hơi ngán

**Sử dụng** :
Best Practice cho search queries

### Chú thích

1. **Type-safe** : là đảm bảo của Java về lỗi do người dùng gây ra cho câu truy vấn về dữ liệu truyền vào truy vấn.
   Ví dụ query `SELECT * FROM product WHERE :price = 10` , nếu biến price viết sai chính tả thì ứng dụng vẫn chạy bình
   thường, nó chỉ báo lỗi trong quá trình runtime khi query đến câu này, mong muốn của ta là tình biên dịch phát hiện và
   báo lỗi ngay trong quá trình compile