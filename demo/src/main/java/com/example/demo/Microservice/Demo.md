# Microservice

## 1. Nêu hiểu biết về hệ thống microservice ? Ưu nhược điểm khi phát triển và vận hành 1 hệ thống microservice

## 2. Các thành phần và chức năng của nó trong hệ thống microservice

## 3. Giao tiếp trong microservice (REST, gRPC, Message queue)

## 5. Quản lý transaction trong hệ thống microservice (Saga pattern)

[Chứ thích](#chú-thích)

### Chú thích

## Microservice architecture

Hoàn cảnh để microservice ra đời :

- Các ứng dụng monolithic đang yếu thế: một cục code, khó maintain, khó scale up, logic phụ thuộc chặt chẽ,...
- Các hệ thống lớn cần một kiến trúc chịu tải tốt, logging dễ dàng, khó chết,...

> Định nghĩa

## Các thành phần của 1 hệ thống microservice

### Service Discovery

Trong monolithic, các service giao tiếp với nhau = các lời gọi phương thức trực tiếp trong bộ nhớ. Lần gọi này là tức
thời và cố định tại thời điểm biên dịch

Trong microservice, các service giao tiếp với nhau qua mạng, nếu hardcode địa chỉ của service A để service B gọi thì có
thể xảy ra các vấn đề :

- Chết ngay khi hệ thống mở rộng : Khi lượng người dùng tăng, service A phải tạo ra nhiều instance để handle, mỗi
  instance lại có ip và port khác nhau, làm sao để B biết trước các port đó để gọi ?
- Chết ngay thì 1 instance chết : Khi 1 instance của service A chết, 1 instance mới sẽ lên thay, B không biết và cứ thế
  gọi vào 1 instance đã chết
- Môi trường Container và Production : Ở các môi trường khác nhau thì service có địa chỉ khác nhau, nói chung là mõm

> Định nghĩa

#### Cơ chế Service Discovery

##### Đăng ký dịch vụ (Service Registration)

Là quá trình 1 service khai báo danh tính với hệ thống. Nó xảy ra ngay sau khi 1 instance của service được tạo thành
công và sẵn sàng nhận request. Instance đó sẽ gọi 1 API đến Service registry và cung cấp các thông tin của chính nó,
bao gồm tên service, địa chỉ mạng, metadata. Service registry sẽ lưu lại các thông tin này

##### Khám phá dịch vụ (Service Discovery)

Là quá trình 1 service tìm kiếm địa chỉ của 1 service khác mà nó muốn giao tiếp. Khi 1 service gọi đến service khác,
nó không truy cập trực tiếp vào port của service đó, thay vào đó nó gửi truy vấn cho thằng service discovery để nó
tìm tất cả instance còn sống của service cần tìm. Nó sẽ trả về 1 ds địa chỉ các service ấy. Sau khi nhận ds này,
service cần sẽ thực hiện client-side load balancing - nó tự chọn 1 trong các địa chỉ trong danh sách để gửi request
đến (defaut là round-robin)

##### Kiểm tra sức khỏe (Health checking)

Là quá trình dọn rác, loại bỏ các instance đã chết ra khỏi service discovery. Cơ chế phổ biến nhất để làm việc này là
heartbeat(nhịp tim) - mỗi instance service sẽ có trách nhiệm gửi 1 tín hiệu checking đến service registry sau mỗi
khoảng thời gian nhất định(thường là 30s), sau khoảng 90s mà service registry không nhận được tín hiệu từ instance
nào nó vứt luôn instance đó.

#### Cách thức triển khai các cơ chế của service discovery

##### Client-side Discovery

Đây là mẫu triển khai của Netflix Eureka

Luồng hoạt động:

1. Service A đăng ký với Eureka Server
2. Service A muốn gọi service B, A truy vấn đến Eureka lấy địa chỉ của B
3. Eureka trả về 1 ds, A lưu cache ds này và pick 1 instance để gửi request

Ưu điểm :

- Linh hoạt, client (A) có toàn quyền để kiểm soát load balance và thực hiện logic phức tạp

Nhược điểm :

- Logic discovery phải được tích hợp vào từng client service

##### Server-side Discovery

Đây là mẫu triển khai của 1 số nền tảng điều phối như Kubernetes

Luồng hoạt động :

1. 1 thành phần của nền tảng sẽ tự động đăng ký service A với 1 registry nội bộ
2. Service A muốn gọi service B, A sẽ thực hiện gọi đến 1 DNS ảo, cố định
3. Một bộ định tuyến (proxy) sẽ chặn request này và tự động truy vấn registry và lấy 1 instance của B, sau đó chuyển
   request đến B

Ưu điểm :

- Service client bị giảm phụ thuộc vào service đích, hoạt động tốt trong môi trường đa ngôn ngữ
- Cần có 1 proxy với độ sẵn sàng cao

#### Cách thức handle việc chết service discovery

1. Chạy registry ở chế độ cluster : Luôn chạy nhiều instance service registry để chúng có độ sẵn sàng cao, các service
   registry copy data của nhau
2. Caching phía client : Sau khi lấy được ds địa chỉ từ registry, chúng lưu cache. Khi registry bị sập, chúng vẫn dunfg
   cache để xác định service được trong 1 thời gian
3. Self-Preservation Mode của Eureka : Eureka sau khi nhận được việc nhiều service đồng loạt không gửi heartbeat về
   registry, nó sẽ không xóa instance của chúng nó vội (vì có thể do mạng yếu)

### API Gateway

Trong một hệ thống microservice, cần quản lý nhiều service, việc kết nối trực tiếp đến nhiều service sẽ gây ra nhiều vấn
đề :

- Client phải biết địa chỉ của tất cả service để truy cập
- Phải triển khai authen trên từng service,...
- Lặp code logging, rate limitting trên mỗi service

API Gateway giải quyết các vấn đề này, nó là điểm vào duy nhất của client. Một số chức năng của nó như :

Khi một yêu cầu đến với Gateway:

1. Gateway Handler Mapping sẽ duyệt qua tất cả các route đã được cấu hình và tìm route đầu tiên mà request khớp với tất
   cả Predicate của route đó.
2. Nếu tìm thấy, request được chuyển qua Filtering Web Handler để xý lý. Handler này sẽ tạo ra các GlobalFilter và
   các GatewayFilter cụ thể của route đã khớp.
3. Request đi qua Filter theo thứ tự. Mỗi Filter có thể thực hiện các thao tác như logging, rate limitting,
   authentication,...
4. Request sau khi đi qua filter sẽ đến URL đích
5. Response thì đi ngược lại

#### Định tuyến (routing)

> Tập hợp các quy tắc định tuyến hoàn chỉnh. Mỗi quy tắc bao gồm Predicate và Action. Predicate là điều kiện để
> request khớp với route, Action là các thao tác cần thực hiện khi request khớp với Predicate (if else đồ).

Một Route gồm 4 thành phần chính :

1. ID : Thường là 1 chuỗi `string` unique để định danh cho route, nó quan trọng trong việc quản lý và giám sát, gỡ
   lỗi,..
2. Destination URL : Là địa chỉ cuối cùng mà request sẽ đến sau khi đi qua các filters. Một số loại URL như :
    - HTTP/HTTPS url - Định tuyến tĩnh : Một url đầy ủ bao gồm schema, host, port, path. Ví dụ
      `https://example.com:8080/api/v1`
    - Load Balancer url (lb) - Định tuyến động : Một url bắt đầu với `lb://` và tiếp theo là service ID. Gateway sẽ sử
      dụng service
      discovery để tìm địa chỉ của service đích và cân bằng tải giữa các instance của service đó. Ví dụ
      `lb://service-name`
    - WebSocket (ws) : Dùng để định tuyến các kết nối đến Web Socket
3. Predicates : Là một danh sách các điều kiện logic, 1 request PHẢI thỏa mãn hết các điều kiện này thì mới được coi là
   khớp với route. Mỗi Predicate là một điều kiện để nhận các tham số cầu hình và tạo ra 1 quy tắc so khớp
   Ví dụ : 1 Route có thể có 2 Predicate : `Path=/api/user/**` và `HttpMethod=GET` => chỉ có những request GET có đuôi
   api như thế mới được khớp và đi đến bộ lọc
4. Filters : Là một danh sách các hành động sửa đổi được áp dụng lên request trước khi đi hoặc trả response lại trước
   khi trả về client. Bản chất mỗi filter nhận vào một số tham số để sửa đổi request.
   Ví dụ : Một route có thể có Filter `StripPrefix = 1` để xóa phần đầu của đường dẫn, và
   `AddRequestHeader=X-Source, Gateway` để thêm một header vào request

Các cách định nghĩa route :

1. File cấu hình `application`:

```yaml 
spring:
  cloud:
    gateway:
      routes: # Bắt đầu danh sách các Route
        # --- Định nghĩa Route thứ nhất ---
        - id: product_service_v1_route
          uri: lb://PRODUCT-SERVICE
          predicates:
            - Path=/api/v1/products/**
            - Method=GET,POST
            - Header=X-Api-Version, 1
          filters:
            - StripPrefix=2 # Xóa /api/v1
            - AddRequestHeader=X-Service-Version, v1.0

        # --- Định nghĩa Route thứ hai ---
        - id: order_service_route
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/api/orders/**
          filters:
            - name: CircuitBreaker # Sử dụng filter với tham số phức tạp
              args:
                name: orderServiceCircuitBreaker
                fallbackUri: forward:/order-fallback

```

2. Code Java qua `RouteLocatorBulder`

```java
package com.theblood.apigateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfiguration {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // --- Định nghĩa Route thứ nhất (tương đương ví dụ YAML ở trên) ---
                .route("product_service_v1_route", r -> r.path("/api/v1/products/**")
                        .and()
                        .method("GET", "POST")
                        .and()
                        .header("X-Api-Version", "1")
                        .filters(f -> f.stripPrefix(2)
                                .addRequestHeader("X-Service-Version", "v1.0"))
                        .uri("lb://PRODUCT-SERVICE"))

                // --- Định nghĩa Route thứ hai ---
                .route("order_service_route", r -> r.path("/api/orders/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("orderServiceCircuitBreaker")
                                .setFallbackUri("forward:/order-fallback")))
                        .uri("lb://ORDER-SERVICE"))

                // Bạn có thể thêm nhiều .route() khác ở đây
                .build();
    }
}
```

## Giao tiếp trong microservice

### Sync

> Giao tiếp đồng bộ là một tương tác request-response có tính chất blocking, trong đó một client gửi 1 request đến
> server và phải chờ đến khi server hoàn thành việc xử lý và trả về response

Ưu điểm : Tính toàn vẹn dữ liệu với những nghiệp vụ yêu cầu cao về tính nhất quán, dễ dàng debug, triển khai mạch lạc

Nhược điểm : Vì là blocking nên các service phải chờ nhau để lấy được data từ service kia, nên khi có 1 service bị lỗi
hoặc treo thì toàn bộ hệ thống bị ảnh hưởng, dễ quá tải khi lượng user tăng cao, việc triển khai đồng bộ về bản chất là
monolith

### Async

> Giao tiếp bất đồng bộ là một tương tác không đồng bộ, trong đó một client gửi một yêu cầu đến server và không cần chờ
> phản hồi ngay lập tức. Thay vào đó, client có thể tiếp tục thực hiện các tác vụ khác trong khi chờ phản hồi từ server.







