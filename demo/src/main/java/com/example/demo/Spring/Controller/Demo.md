# RestController & Controller

## 1. Tìm hiểu sự khác nhau giữa RestController vs Controller, trường hợp sử dụng

## 2. Routing

## 3. Tìm hiểu cách mà controller nhận input đầu vào là : params, body, path variable, header....

## 4. Thực hành viết các api với các method + các loại input đầu vào với data static

## 5. Validate input controller

## 6. Hanlde exception

[Chú thích](#chú-thích)

## Controller

> Controller là một thành phần quan trọng, đóng vai trò cửa ngõ tiếp nhận và xử lý các request từ client, gọi các dịch
> vụ nghiệp vụ và trả về phản hồi phù hợp.

Nguyên tắc thiết kế của Controller hướng đến sự rõ ràng, chỉ nên đóng vai trò là 1 tầng điều phối,
không nên chứa logic tính toán, truy vấn DB hay logic phức tạp

### @Controller

> `@Controller` là một annotation trong Spring, được thiết kế chủ yếu cho kịch bản
> Server-side-rendering. Nó đánh dấu một class là 1 lớp điều phối có nhiệm vụ chính là nhận request, xử lý và trả về 1
> tên logic của 1 View

Cơ chế hoạt động cốt lõi là View Resolution. Luồng xử lý mặc định của Controller:

```
@GetMapping("/home")
public String homePage() {
    return "index"; // Đây là TÊN VIEW, không phải nội dung response
}
```

`DispatcherServlet` không nhận diện đây là một response, mà là tên 1 View và nó cần phải tìm kiếm một template, nó sẽ
chuyển result cho `ViewResolver` để tìm kiếm template tương ứng (ví dụ: `index.html` trong thư mục `templates` nếu sử
dụng Thymeleaf) và tạo ra một trang HTML hoàn chỉnh để trả về client.

Vì cơ chế này là default trên `@Controller`, ta phải dùng `@ResponseBody` ở từng phương thức để chỉ rằng kết quả trả về
dưới dạng JSON/XML, không phải tên View. Điều này giúp Spring bỏ qua cơ chế View Resolution và sử dụng
`HttpMessageConverter` để
chuyển đổi đối tượng Java thành JSON/XML.

### @RestController

> `@RestController` là một annotation trong Spring, được thiết kế đặc biệt cho các ứng dụng RESTful API. Nó là một
> composite annotation - kết hợp giữa `@Controller` và `@ResponseBody`. Nó đánh dấu 1 class là một lớp điều phối có
> nhiệm
> vụ chính là nhận request và trả về dữ liệu trực tiếp dưới dạng JSON/XML.

Cơ chế hoạt động cốt loĩ là Data Serialization. Luồng xử lý mặc định của `RestController`:

```
@RestController
@RequestMapping("/api/users")
public class UserRestController {
    @GetMapping
    public List<User> getAllUsers() {
        // Không cần @ResponseBody
        return userService.findAll();
    }
}
```

`DispatcherServlet`nhận diện đây là một response dữ liệu và sử dụng `MappingJackson2HttpMessageConverter` cho JSON để tự
động chuyển đổi đối
tượng Java thành JSON/XML và trả về client.

### Chú thích 