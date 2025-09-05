# Spring

## 1. Giới thiệu spring Framework: Spring MVC, Spring boot

## [2. Khái niệm DI, IoC](#)

## 3. Spring bean, life cycle bean, bean scope. Các annotation sử dụng để khai báo bean trong Spring.

## 4. Khái niệm cơ bản về request, httpMethod, JSON, API

## 5. Demo : cài đặt môi trường và tạo 1 project spring boot sử dụng Inteliji (Chú ý một số dependency quan trọng)

## 6. Sơ lược về maven và các sử dụng

## 7. Hướng dẫn sử dụng postman : param, body request

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
  -Vi phạm nguyên tắc D trong SOLID: Các module cấp cao (Service) không nên phụ thuộc vào các module cấp thấp (Repo)

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

Tuy nó gọn gàng nhất trong 3 kiểu nhưng nó phụ thuộc chạt chẽ vào container, gay khó khăn cho kiểm thử, nó che giấu
dependency và không thể bất biến

### Chú thích