# Spring

## 1. Giới thiệu spring Framework: Spring MVC, Spring boot

## [2. Khái niệm DI, IoC](#)

## 3. Spring bean, life cycle bean, bean scope. Các annotation sử dụng để khai báo bean trong Spring.

## 4. Khái niệm cơ bản về request, httpMethod, JSON, API

## 5. Demo : cài đặt môi trường và tạo 1 project spring boot sử dụng Inteliji (Chú ý một số dependency quan trọng)

## 6. Sơ lược về maven và các sử dụng

## 7. Hướng dẫn sử dụng postman : param, body request

## *. Spring AOP, Proxy

[Chú thích](#chú_thích)

## Spring Core

> Spring core là nền tảng của toàn bộ Spring framewwork, nhiệm vụ cốt lõi của nó là quản lý vòng đời và sự phụ thuộc của
> các đối tượng trong ứng dụng

Để thực hiện quản lý được các đối tượng, Spring sử dụng cơ chế DI, và IoC - trái tim của Spring

Spring ra đời để giải quyết vấn đề về sự phụ thuộc quá mức (tight coupling) giữa các API của application, khó kiểm thử,
code phức tạp,...

Để biết DI và IoC đóng góp lớn như thế nào vào Spring, ta xét các vấn đề sau:
Xét ví dụ sau, đây là 1 class Service khi chưa áp dụng DI:

```
// Lớp xử lý nghiệp vụ đơn hàng
public class OrderService {

    // OrderService phụ thuộc trực tiếp vào một implementation cụ thể
    private MySqlOrderRepository repository = new MySqlOrderRepository();

    public void processOrder(Order order) {
        // ... logic nghiệp vụ ...
        repository.save(order);
    }
}

// Lớp truy cập dữ liệu cụ thể cho MySQL
public class MySqlOrderRepository {
    public void save(Order order) {
        // ... code để lưu đơn hàng vào database MySQL ...
        System.out.println("Saving order to MySQL DB.");
    }
}
```

Nhìn thì đơn giản nhưng nó đang ẩn chứa những lỗi nghiêm trọng:

- Tight Coupling: Class repository bị gán chặt vào Service
- Với 1 thay đổi khi chuyển MySQL sang PostgreSQL thì ta phải thay đổi code trong Service, điều này sẽ rất khó khăn khi
  có hàng trăm Service cùng dùng Repo đó
- Khó kiểm thử, bắt buộc phải kết nối đến MySQLRepo nếu muốn test Service
- Vi phạm nguyên tắc D trong SOLID: Các module cấp cao (Service) không nên phụ thuộc vào các module cấp thấp (Repo)

### IoC (Inversion of Control) - Đảo ngược điều khiển

> IoC là 1 nguyên lý thiết kế phần mềm, không phải là 1 pattern cụ thể. Nguyên lý này mô tả sự đảo ngược luồng kiểm
> soát (control flow) so với lập trình thủ tục truyền thống

Xét ví dụ trên,

- Ở luồng truyền thống : Các đối tượng user viết ra sẽ chủ động tạo và quản lý các đối tượng phụ thuộc của chúng. Vì vậy
  Service sẽ tự quyết định khi nào tạo mới MySQLRepo. Luồng thực thi được kiểm soát hoàn toàn bên trong code nghiệp vụ
- Ở luồng đảo ngược : Các đối tượng user viết ra trở thành các thành phần bị động, chúng không tạo ra các đối tượng phụ
  thuộc (dependencies) của chúng nữa mà chỉ định nghĩa những dependency cần thiết. Một framework bên ngoài (Container)
  sẽ chịu trách nhiệm khởi tạo và tiêm (inject) các dependency đã được định nghĩa. Quyền kiểm soát vòng đời và sự liên
  kết giữa các đối tượng giờ chuyển sang cho Container

Cụ thể, Container nắm quyền kiểm soát các khía cạnh sau :

- Khởi tạo đối tượng : Container quyết định khi nào và làm thế nào để gọi constructor của 1 class
- Quản lý vòng đời : Container quản lý toàn bộ vòng đời của đối tượng, từ lúc khởi tạo, gọi các callback sau khởi tạo,
  đến lúc hủy
- Liên kết phụ thuộc : Container chịu trách nhiệm phân giải và liên kết các đối tượng với nhau để hình thành 1 đồ thị
  đối tượng hoàn chỉnh

Trong Spring, thực thể thực hiện hóa IoC Container chính là **BeanFactory** (cấp thấp) và **ApplicationContext** (cấp
cao)

```
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args); // <--- Thằng này tạo ra ApplicationContext, nó sẽ scan các class có annotation @Component, @Service, @Repository, @Controller, @RestController, @Configuration
	}

}
```

### Bean

> Bản chất bean đơn giản là 1 object, điều khiến nó đặc biệt là do nó được khởi tạo, quản lý và lắp ráp bởi IoC
> Container

Việc bean hóa các object để

- Tất cả các object được quản lý bởi 1 nơi duy nhất là IoC Container.
- Giúp tiết tiệm tài nguyên khi mặc định bean sẽ có scope là singleton, 1 instance chia sẻ cho các bean khác => tiết
  kiệm bộ nhớ
- Container chịu trách nhiệm tiêm các phụ thuộc vào các bean, các thành phần không cần biết về cách tạo ra nhau, chỉ
  biết về interface của chúng (loose coupling)
- Spring có thể bọc các bean bằng các bean khác cao cấp hơn mà không cần thay đổi code của bean

#### Các annotation khai báo bean:

- `@Component` : Annotation chung nhất và cơ bản nhất, bất kỳ object nào muốn được Spring quản lý đều được đánh dấu bằng
  anno này. Các anno dưới sau đây chỉ là các chuyên biệt hóa của Commponent
- `@Service` : Đánh dấu các class ở tầng dịch vụ
- `@Repository` : Đánh dấu các class ở tầng truy cập dữ liệu. Spring cung cấp thêm cho nó khả năng dịch các Exception
  liên
  quan đến persistence (SQLException,...) thành các Exception nhất quán của Spring (DataAccessException), giúp tầng
  Service không bị phụ thuộc vào loại persistance cụ thể
- `@Controller` : Thuộc Spring MVC, đánh dấu các class ở tầng điều khiển, cho biết các class này chịu trách nhiệm xử lý
  các Http request
- `@RestController` : Kết hợp giữa `@Controller` và `@ResponseBody`, dùng để xây dựng các REST API, kết quả của các hàm
  trả về sẽ tự động convert sang JSON/XML và ghi vào HTTP response body.

Khai báo bean dựa trên Java, chủ yếu để bean hóa các class từ thư viện bên ngoài mà không cần sửa code

- `@Configuration` : Đánh dấu 1 class là 1 nguồn định nghiax của bean. Nó tương đương với 1 file cấu hình bằng XML
- `@Bean` : Được sử dụng bên trong 1 class, được đặt trên 1 phương thức, Spring sẽ thực thi phương thức này và bean
  hóa thằng đối tượng mà method này trả về. Tên của phương thức sẽ là tên mặc định của bean

```
@Configuration
public class AppConfig {

    // Khai báo một bean tên là "dataSource"
    @Bean
    public DataSource dataSource() {
        // Giả sử bạn đang dùng một thư viện DataSource bên ngoài
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
        dataSource.setUsername("user");
        dataSource.setPassword("password");
        return dataSource;
    }

    // Khai báo một bean khác phụ thuộc vào bean "dataSource"
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) { // Spring tự động tiêm bean dataSource vào đây
        return new JdbcTemplate(dataSource);
    }
}
```

#### Bean lyfecycle

1. Tải bean : Spring Container tìm thấy đinh nghĩa bean và gọi constructor để tạo ra 1 instance của object
2. Inject : Spring tiêm các thuộc tính được yêu cầu vào bean
3. Khởi tạo : Giai đoạn này setBeanName, setFactory, set thực thi các thứ
4. Bean ready : Bean chờ để được dùng
5. Destruction : Hủy bean khi ApplicationContext đóng

```
@Component
public class MyBean {

    public MyBean() {
        System.out.println("1. Constructor called");
    }

    @PostConstruct
    public void initialize() {
        System.out.println("2. @PostConstruct: Bean is initializing...");
        // Ví dụ: mở kết nối, đọc file cấu hình...
    }

    public void doWork() {
        System.out.println("3. Bean is doing work...");
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("4. @PreDestroy: Bean is cleaning up...");
        // Ví dụ: đóng kết nối, lưu trạng thái...
    }
}
```

#### Bean Scope

> Bean Scope định nghĩa vòng đời và khả năng hiển thị của 1 instance Bean.

Bean scope trả lời cho câu hỏi rằng khi ta yêu cầu 1 bean, thứ ta nhận lại là 1 instance mới hay 1 instance đã được khởi
tạo từ trước

Các scope chính

- singleton (default) : Chỉ 1 instance duy nhất được tạo ra cho mỗi IoC Container, mọi yêu cầu cho bean này sẽ trả về
  cùng 1 tham chiếu đến 1 vùng nhớ. Các bean singleton phải là stateless để đảm bảo an toàn trong môi trường đa luồng.
- prototype : 1 instance mới được tạo ra mỡi khi gọi đến bean. Spring Container không chịu trách nhiệm hủy các instance
  của bean này
- request : 1 instance được tạo ra cho mỗi 1 Http request, khi request kết thúc, bean sẽ bị hủy
- session : 1 instance mới được tạo ra cho mỗi 1 Http session, khi đóng session thì hủy bean
- application : 1 instance duy nhất được tạo ra cho `ServletContext`, tương tự singleton nhưng có ngữ nghĩa web
- websocket : 1 instance tạo ra cho WebSocket session

Khai báo scope

```
@Component
@Scope("prototype")
public class MyPrototypeBean {
    // ...
}

@RestController
@Scope("request") // Thường không cần thiết vì Controller mặc định đã xử lý theo request
public class MyRequestScopedController {
    // ...
}
```

### DI (Dependency Injection) - Tiêm sự phụ thuộc

> DI là 1 Design Pattern cụ thể để thực hiện hóa IoC. Nó là cơ chế mà qua đó IoC Container
> cung cấp các dependency cho một đối tượng. Thay vì tự pull dependency về, container sự tự push dependency vào đối
> tượng

Xét ví dụ sau:
Khi không dùng DI, khi ta khởi tạo `ReportGenerator r1`, bộ nhớ khởi tạo 1 ô nhớ cho `ReportGenerator` , sau đó khởi tạo
1 ô nhớ nữa cho `ExcelReportExporter` , tham chiếu `exporter` trỏ đến ô nhớ đó. Tương tự với `ReportGenerator r2`, ta có
2 vùng nhớ của đối tượng `ExcelReportExporter` được tham chiếu đến bởi biến `exporter` của 2 thằng instance của
`ReportGenerator`.

```
public class ReportGenerator {
    private ExcelReportExporter exporter = new ExcelReportExporter();
}
...
ReportGenerator r1 = new ReportGenerator();
ReportGenerator r2 = new ReportGenerator();
...
```

Luồng

```
[Client Code] --calls--> new ReportGenerator()
                      |
                      +-----> [ReportGenerator's Constructor] --calls--> new ExcelReportExporter()
                                                                   |
                                                                   +-----> [ExcelReportExporter's Constructor]
```

Khi dùng DI

```
@Component
public class ReportGenerator {
    private final ReportExporter exporter;
    public ReportGenerator(ReportExporter exporter) { this.exporter = exporter; }
}

@Component
public class ExcelReportExporter implements ReportExporter { ... }
```

Quyền khởi tạo nằm trong tay IoC Container, nó đọc metadata của Component và quyết định tạo cái nào trước.
`ReportGenerator` không có quyền này. Container se tạo
`ExcelReportExporter` trước vì trong nó không có dependency nào, sau đó lưu trữ tham chiếu đến nó.
Sau đó, khi tạo `ReportGenerator`, nó inject biến tham chiếu đã lưu trữ đó vào constructor của `ReportGenerator`.

```

[Application Startup] --> [IoC Container] --reads metadata--> Discovers ReportGenerator, ExcelReportExporter
                                  |
                                  +-----> Creates "excelReportExporter" bean (singleton instance)
                                  |
                                  +-----> Creates "reportGenerator" bean
                                                |
                                                +-----> Injects "excelReportExporter" bean into ReportGenerator's constructor
 ```

Hệ quả :

- Code của `ReportGenerator` chỉ phụ thuộc vào interface `ReportExporter`, nó không hề biết về `ExcelReportExporter` lúc
  biên dịch.
- Lifecycle của `ExcelReportExporter` được quản lý độc lập bởi Container, độc lập với `ReportGenerator`
- Mặc định, chỉ có 1 instance của `ExcelReportExporter` được tạo ra và chia sẻ cho tất cả các bean khác cần đến nó (
  singleton)

Có 3 loại DI

- **Constructor Injection (tiêm qua hàm khởi tạo)** :
    1. Khi Spring Container cần tạo 1 bean, nó sẽ tìm đến constructor của
       class đó. Nếu class đó có 1 constructor duy nhất, Spring Container mặc định sẽ dùng nó. Nếu có nhiều hơn 1
       constructor, Spring sẽ tìm đến constructor được đánh dấu `@Autowired`. Không có constructor nào thì nó báo lỗi.
    2. Sau đó, Spring phân tích các tham số của constructor đã chọn, nó tìm trong thanh ghi của mình những bean đã tạo
       tương
       thích. Nếu không tìm được bean phù hợp, `NoSuchBeanDefinitionException` được ném ra, nếu tìm ra nhiều bean phù
       hợp mà không có cơ chế phân giải như `@Primary`, `@Qualifier` thì ném ra `NoUniqueBeanDefinitionException`.
    3. Spring lấy các bean dependency đã phân giải, nó gọi constructor với các bean này làm đối số và đối tượng đã được
       tiêm bean cần thiết được tạo ra

```
@Service
public class OrderService {
    // Khai báo final để đảm bảo bất biến
    private final OrderRepository orderRepository;
    private final CustomerService customerService;

    // Constructor là điểm tiêm dependency
    public OrderService(OrderRepository orderRepository, CustomerService customerService) {
        this.orderRepository = orderRepository;
        this.customerService = customerService;
    }
}
```

Kiểu tiêm này dảm bảo tính bất biến, ngăn chặn việc tham chiếu bị thay đổi sau khi đối tượng đã được khởi tạo. Ngoài ra
nó giúp phát hiện sớm **Circular Dependencies** để báo lỗi,...

- **Setter Injection (tiêm qua setter)** :
    1. Spring Container gọi đến 1 constructor không tham số của class để tạo 1 instance rỗng của bean
    2. Sau khi đối tượng đc tạo, Spring tìm tất cả thằng nào có `@Autowired` và gọi chúng (thường là setter) và truyển
       các dependency đã phân giải

```
@Service
public class NotificationService {
    private EmailService emailService; // Bắt buộc
    private SmsService smsService;     // Tùy chọn

    // Constructor không tham số được gọi đầu tiên
    public NotificationService() {}

    @Autowired
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    // Đánh dấu là không bắt buộc, nếu không tìm thấy bean SmsService, Spring sẽ bỏ qua
    @Autowired(required = false)
    public void setSmsService(SmsService smsService) {
        this.smsService = smsService;
    }
}
```

Cách inject này hỗ trợ dependency tùy chọn và khả năng cấu hình lại bean

- **Field Injection (tiêm vào trường)**
    1. Spring gọi đến consstructor không tham số để tạo 1 instance của bean
    2. Sau khi tạo đối tượng, spring dùng Reflection API để truy cập thẳng vào các field của đối tượng, tìm field gán
       `@Autowired` và gán giá trị của các bean dependency tương ứng vào chúng. Cơ chế này bỏ qua setter nếu trường là
       private.

```
@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    
    // ...
}
```

Tuy nó gọn gàng nhất trong 3 kiểu nhưng nó phụ thuộc chặt chẽ vào container, gây khó khăn cho kiểm thử, nó che giấu
dependency và không thể bất biến

## Maven

Trước khi có Maven trong Java, Dev khi khởi tạo 1 project sẽ phải đi tìm các file JAR của các thư viện (
sql-connector,...) để tải và import vào project, hơn nữa, việc này đôi khi gây ra xung đột version giữa các phiên bản
rất khó chịu

> Maven là một công cụ quản lý và thấu hiểu dự án. Meven hoạt động với triết lý Quy ước hơn Cấu hình (Convention over
> Configuation)

Maven giải quyết các vấn đề nhưu quản lý phụ thuộc hay tiêu chuẩn hóa quy trình build project như đã nêu ở trên

Maven định nghĩa file `pom.xml` - là trái tim của project và là một mô hình đối tượng trong bộ nhớ mà Maven xây dựng khi
thực thi. Bản chất file pom là một file khai báo, không phải là 1 file kịch bản

Các thẻ trong pom:

- `<parent>` : Khai báo kế thừa
- `<groupId>`, `<artifactId>`, `<version>` :Xác định chính xác lib
- `<packaing>` :Xác định kết quả cảu việc parse package
- `<properties>` : Định nghĩa các biến toàn cục
- `<dependencies>` : Thẻ cha chứa các thư viện
- `<dependency>` : Thẻ con khai báo 1 thư viện cụ thể
    - `<scope>` : Xác định phạm vi sử dụng của dependency
    - `<optional>` : Nếu là `true` , các dự án phụ thuộc vào dự án này sẽ không kế thừa lại dependency này
- `<build>` : Chứa các cấu hình liên quan đến việc build dự án
- `<plugin>` : Khai báo và cấu hình 1 plugin Maven

Ví dụ

```
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>31.1-jre</version>
</dependency>
```

Sau khi Save, maven chạy lệnh `mvn compile` :

1. Maven xây dựng 1 Effective POM bằng cách gộp thông tin các loại file :
    - file pom mặc định có sẵn trong Maven, chứa các cấu hình cơ bản nhất, gồm
      cả địa chỉ của Maven Central Repository
    - Maven đi ngược lên `<parent>`, gộp các cấu hình từ pom cha
    - File pom chính
    - File cấu hình toán cục,...
2. Maven kiểm tra Local Repository trước tiên (.m2)
    - Convert GAV (group, artifact, version) thành đường dẫn thư mục và tìm trong m2. Nếu có maven lấy dependency ở đây,
      không thì chuyển sang bước 3
3. Tìm kiếm trong Remote Repository
    - Maven lặp qua ds Remote Repo đã được định nghĩa trong Effective pom
    - Với mỗi repo, nó sẽ tạo 1 url truy vấn từ cấu trúc của GAV
    - Nếu tìm thấy file, tải về rồi lưu trong Local Repo, lần build sau, nó sẽ lấy lib từ Local ra
    - Nếu không tìm thấy, báo lỗi
      4.Xử lý phụ thuộc bắc cầu
    - Maven lại đọc file pom của các file đã tải về và lặp lại cho đến khi toàn bộ artifact cần thiết đã có trong m2
5. Xây dựng classpath
    - Xây dựng các đường dẫn đầy đủ đến từng file trong m2, danh sách này sau đó được đưa cho JVM hoặc trình biên dịch
      để chúng tìm kiếm và
      truy cập khi chạy project

Một số lệnh maven cơ bản

- `mvn clean` : Xóa các sản phẩm được tạo ra từ lần build trườc đó, nói chung là xóa `target`
- `mvn validate` : kiểm tra các thông tin cần về việc build có hợp lệ hay không, nói chung là kiểm tra file pom
- `mvn compile` : Biên dịch mã nguồn ứng dụng (không bao gồm mã test), nó chạy 1 loạt các phase, nó copy toàn bộ
  `src/main/resource` vào `target/classes`
- `mvn package` : Lấy code đã biên dịch và đóng gói nó vào dạng file như JAR, WAR, nó lấy nội dung từ `target/classes`
  và các tài nguyên rồi tạo file JAR trong `target`
- `mvn test` : biên dịch và chạy các unit test, nó sao chép tài nguyên vào `target/test-classes`, biên dịch rồi test,
  bất kỳ test nào sai đều báo lỗi
- `mvn install` : Cài đặt artifact vào Local Repo, nó chạy hết các lệnh cho đến package rồi tải file JAR
  và pom vào m2
- `mvn deploy` : Đẩy project lên Remote Repo, nó chạy hết các lệnh đến install rồi tải nó lên Remote thôi

## Spring MVC

> Spring MVC là một module trong Spring framewor,được thiết kế để thực hiện hóa mô hình Model-View-Controller của ứng
> dụng web. Nó sử dụng một mẫu thiết kế trung tâm là Front-Controller, để thực hiện hóa qua `DispatcherServlet`

`DispatcherServlet` ủy quyền cho các component chuyên biệt có thể config và tự thay thế được

Luồng xử lý request của Spring MVC là tiêu biểu cho việc sử dụng IoC:

1. `DispatcherServlet` : Nhận mọi Http request và điều phối chúng
2. `HandleMapping` : Tiếp nhận từ `DispatcherServlet`, tìm Controller method tương thích để xử lý request dựa vào url,
   Http method, headers,...
3. `HandlerAdapter` : `DispatcherServlet`tìm và yêu cầu nó thực thi method đã tìm thấy
4. Thực thi Controller method, bản thân controller đã là 1 bean nên sẽ được inject dependency để gọi đến các logic trong
   service,...
5. Controller trả về kết quả, nếu kết quả là 1 View, `DispatcherServlet` sẽ hỏi `ViewResolver` dịch tên logic của view
   thành 1 implements của `View` cụ thể
6. View được, tạo ra response cuối cùng (HTML)

## Spring boot

## Client (Postman)

### Request

> Request là một thông điệp được gửi từ client đến server để yêu cầu thực hiện một hành động cụ thể trên tài nguyên (DB)
> của server.

Ví dụ về 1 request phía client điển hình :

```
POST /api/users HTTP/1.1                <-- Dòng yêu cầu (Request Line)
Host: my-api.com                        <-- Header
Content-Type: application/json          <-- Header
Content-Length: 56                      <-- Header
User-Agent: PostmanRuntime/7.29.2       <-- Header
Accept: */*                             <-- Header

{                                       <-- Dòng trống (CRLF) ngăn cách header và body
  "username": "testuser",               <-- Body (Payload)
  "email": "test@example.com"
}
```

Trong đó :

- **Request line** : Chứa 3 thông tin quan trọng : HTTP Method (GET, POST,...), URL (hoặc endpoint) và phiên bản giao
  thức
  HTTP
- **Headers** : Một tập hợp các cặp key-value cung cấp thông tin bổ sung (metadata) về request. Ví dụ:
    - `Content-Type` : Loại dữ liệu trong body
    - `Authorization` : Thông tin xác thực
    - `User-Agent` : Thông tin về client gửi request
    - `Accept` : Loại dữ liệu mà client có thể xử lý
- **Body** : Chứa data mà client gửi lên, phần này là tùy chọn, nó thường đi cùng với method POST, PUT, PATCH. Body có
  thể ở nhiều định
  dạng khác nhau như JSON, XML, form-data,...

Request lúc này sẽ đi đến server, hứng nó phía server là **Servlet Container**
Nhiệm vụ của nó là :

- Lắng nghe các kết nối TCP/IP ở trên 1 port
- Đọc data thô của HTTP request
- Phân tích cú pháp (parse) request thành các thành phần có cấu trúc mà Java có thể hiểu được

Request được phân tích và convert tự động giúp dev không phải nhúng tay

Thực hiện hóa bằng các Servlet Container như Tomcat, Jetty, Undertow,...
`ServletRequest` và `HttpServletRequest` là 2 interface quan trọng trong API Servlet, chúng đại diện cho request
đến server :

- `ServletRequest` : Interface gốc cấp thấp, cung cấp các phương thức để truy cập các thành phần cơ bản của request như
  parameter, header, body,...
- `HttpServletRequest` : Kế thừa và Mở rộng `ServletRequest`, thêm các phương thức đặc thù cho giao thức HTTP như
  getMethod(),
  getRequestURI(), getSession(),...

Sau đó request đến các handler và filter khác trước khi đến Controller

Luồng đến của request:

![ew](src/main/resources/local/filterchain-1a.png)

```
HTTP Request -> Embedded Servlet Container (Tomcat) -> HttpServletRequest -> DispatcherServlet -> HandlerMapping -> Controller -> ...
```

## Spring AOP

Trước khi AOP ra đời, các vấn đề về Cross-Cutting Concerns được đặt ra:

Các service khi tạo ra cần ghi log, kiểm tra quyền(authorize), caching, quản lý transaction,... Thông thường ta sẽ lặp
lại việc xử lý các vấn đề này trong code, nó dẫn đến việc code bị lặp, khó bảo trì và logic nghiệp vụ bị cắt ngang bởi
những thứ về hệ thống

```
public class OrderService {
    public Order getOrder(Long id) {
        // 1. Code bảo mật
        SecurityManager.checkPermission("GET_ORDER");

        // 2. Code bắt đầu giao dịch
        Transaction tx = transactionManager.begin();

        // 3. Code logging
        log.info("Entering getOrder with id: " + id);
        long start = System.currentTimeMillis();

        try {
            // 4. LOGIC NGHIỆP VỤ CỐT LÕI
            Order order = repository.findById(id);
            // ...

            // 5. Code commit giao dịch
            tx.commit();
            return order;
        } catch (Exception e) {
            // 6. Code rollback
            tx.rollback();
            throw e;
        } finally {
            // 7. Code logging và đo lường hiệu năng
            long end = System.currentTimeMillis();
            log.info("Finished getOrder. Execution time: " + (end - start) + "ms");
        }
    }
}
```

AOP sinh ra để giải quyết

> Aspect-Oriented Programming là một mô hình lập tình cho phép tách biệt các CCC ra khỏi logic nghiệp vụ của ứng dụng

Sring AOP là cách Spring thực hiện AOP trong framwork của mình, nó cho ta định nghĩa các advise (lời khuyên) và
cắt chúng vào các điểm thực thi cụ thể trong code 1 cách linh hoạt mà không cần sửa code

Các thuật ngữ chính :

- **Aspect** : là một module đóng gói CCC. Trong Spring, class Aspect là class được đánh dấu bởi `@Aspect`
- **Advise** : là hành động sẽ được thực hiện bởi 1 aspect, có các loại advise khác nhau:
    - `@Before` : Chạy trước khi phương thức được gọi
    - `@AfterReturnning` : Chạy sau khi phương thức trả về kết quả thành công
    - `@AfterThrowing` : Chạy sau khi method ném ra exception
    - `@After` : Chạy sau khi phương thức kết thúc bất kể thành công hay thất bại
    - `@Around` : Mạnh, bao bọc toàn bộ phương thức, cho phép thực hiện các hành động cả trước cả sau, thậm chí có thể
      không thực hiện gọi phương thức gốc. Thằng `@Transaction` dùng thằng này
- **Pointcut** : Là một biểu thức (expression) để xác định chỗ nào sẽ được áp dụng advise
- **Join Point** : Là một điểm cụ thể trong quá trình thực hiện chương trình, advise sẽ được cắm vào
- **Weaving** : Là sự kết hợp các Aspect object với object mục tiêu để tạo ra advised object cuối cùng trong Runtime.

Spring AOP không sửa code, nó dùng Proxy

### Proxy Pattern

> Một Proxy là đối tượng đứng ra đại diện cho một đối tượng khác. Proxy có cùng interface với đối tượng thật, cho phép
> nó thay thế đối tượng thật 1 cách trong suốt

Khi một phương thức bị bao bởi Proxy bị gọi,
proxy có thể :

- Thực hiện 1 số hành động trước khi chuyển lời gọi đến đối tượng thật
- Chuyển tiếp lời gọi đến các phương thức tương ứng
- Thực hiện 1 số hành động sau khi nhận được kết quả từ đối tượng thật
- Trả về kết quả cho thằng gọi

Với 1 ví dụ khi transaction cho 1 phương thức, quy trình sẽ diễn ra như sau

1. Spring Container tạo ra `OrderService` - 1 đối tượng thật
2. Spring AOP thấy `OrderService` có method có `@Transaction`
3. Thay vì inject `OrderService` thật, Spring tạo ra 1 lớp Proxy và inject nos vào `OrderController` khi controller cần
   dùng đến service
4. Controller gọi một phương thức `orderService.order()` thực chất nó đang gọi method `order()` trên Proxy
5. Method `order()` được kích hoạt, advise `@Around` hoạt động, nó tạo 1 transaction trước khi gọi phương thức thật
6. Sau khi method thật thực thi xong, nếu rollback thì nó ném exception cho controller xử lý, không thì tra về kết quả

Có 2 loại Proxy trong Spring :

- **JDK Dynamic Proxy** (default) : Nó yêu cầu đối tượng thật phải implements 1 interface, Proxy sẽ được tạo ra để
  implements interface đó
- **CGLIB Proxy** : Nếu đối tượng thật không implements interface nào, nó sẽ tạo 1 subclass của đối tượng thật tại
  Runtime. Spring boot thường dùng thằng này

### Chú thích