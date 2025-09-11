# Spring Security

## 1. Thế nào là authentication, authorization

## 2. JWT, các thành phần của JWT

## 3. Tích hợp JWT vào dự án

## 4. Phân quyền sử dụng annotation và qua cấu hình httpSecurity

[Chú thích](#chú-thích)

## Security

### JWT

> JSON Web Token là một tiêu chuẩn mở định nghĩa một cách nhỏ gọn và khép kín để truyền tải thông tin an toàn giữa
> server và client dưới dạng 1 đối tượng JSON

Cấu trúc : **Header.Payload.Signature**

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

1. **Header** : Thường bao gồm 2 loại :

- alg là thuật toán ký mà token sử dụng
- typ là loại token mặc định là JWT of course

```
{
"alg" : "HS256",
"typ" : "JWT"
}
```

Header này được mã hóa Base64url để tạo ra phần đầu của token. Việc chỉ định alo trong header là rất quan trọng, vì nó
cho bên nhận biết cách để xử lý signature

2. **Payload
   ** : Chứa các tuyên bố (Claims). Claims là các câu lệnh về thực thể (user) và một số data bổ sung. Có 3 loại Claims:
    - **Registered Claims** : Đây là loại Claims được định nghĩa sẵn, không bắt buộc đùng nhưng cung cấp các thông tin
      hữu ích và có thể tương tác. Ví dụ :
        - `iss` - Issuer : Ai phát hành token
        - `sub` - Subject : Chủ thể token là ai
        - `aud` - Audience : Người nhận token
        - `exp` - Expired : Thời hạn token
        - `iat` - Issue At : Thời điểm phát hành
    - **Public Claims** : Loại token được định nghĩa Payload tự do
    - **Private Claims** : Loại token đã được tùy chỉnh để chia sẻ thông tin giữa các bên đã đồng ý sử dụng chúng
        ```
      {
        "sub": "user123",
        "roles": ["ADMIN", "USER"],
        "exp": 1678886400
        }

      ```

Giống như Header, Payload cũng được mã hóa Base64Url, nhưng payload **chỉ được mã hóa, không được encrypted**, bất cứ ai
có token đều giải mã và đọc được data của payload

3. **Signature** : Để có chữ ký cần header mã hóa, payload mã hóa, 1 secret key và thuật toán đã chỉ định trong header
   Tất nhiên signature này để xác minh tính toàn vẹn của payload và header trên đường truyền dữ liệu không bị thay đổi,
   và để xác minh người gửi vì secret key chỉ có bên server biết nên khi server xác định chữ ký trong token này của mình
   nó sẽ cho qua

Luồng họat động của JWT, lấy ví dụ API `/login` :

1. Client gửi username password đến server, nó xác thực bằng cơ chế đã trình bày ở trên
2. Nếu credentials hợp lệ, server tạo 1 JWT chứa payload là thông tin user, secret key,...
3. Server gửi JWT về cho Client
4. Client nhận và lưu vào bộ nhớ bên đó
5. Với các request tiếp theo đến server cần authen, nó sẽ gửi kèm token vào header `Authorization` của request
6. Khi nhận được request, server sẽ lấy token từ header, check token valid, token is expired, check role trong payload
   và phân quyền

Nhược điểm lớn nhất của token là khi nó được server cấp cho client, server sẽ không có cách nào thu hồi token, token sẽ
chỉ vô hiệu khi nó hết hạn. Điều này khiến các biện pháp như giới hạn thời gian accessToken xuống vài phút và thêm
refresh token
hay tạo 1 BLACK_LIST.

Một nhược điểm tiếp theo là việc để lộ secret key cho những thằng trộm thì mất tất.

### Authentication (Xác thực)

> Authenticate là quá trình xác minh danh tính người dùng, đảm bảo trả lời câu hỏi Người dùng là ai ? Có đúng như những
> gì họ đã khai báo không.

Bằng chứng (credentials) phổ biến nhất để một chủ thể (principal) xác thực với hệ thống là username và password. Hiện
nay vì tích hữu dụng nên các hệ thống hỗ trợ xác thực với JWT,OAuth2 token, mail, sms, sinh trắc học,...

Nếu chủ thể cung cấp được bằng chứng hợp lệ, hệ thống xác thực thành công và cấp cho họ một đối tượng `Authentication`
chứa thông tin về danh tính và các quyền hạn của họ.

```
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
String currentUser = authentication.getName(); // Lấy tên người dùng hiện tại
        
```

### Authorization (Ủy quyền / Phân quyền)

> Authorization là quá trình xác định xem một chủ thể đã được xác thực có quyền truy cập vào một tài nguyên cụ thể hay
> chưa.
> Quá trình này diễn ra sau khi authen thành công.

Dựa trên thông tin xác thực, hệ thống sẽ quyết định tài nguyên nào chủ thể được phép truy cập, tài nguyên nào block

Authentication và Authorization là 2 quá trình luôn đi liền với nhau. Một yêu cầu không được xác thực (anonymous
request) cũng sẽ được Spring security coi là một dạng danh tính đặc biệt thông qua `AnonymousAuthenticationToken` với
quyền hạn tối thiểu, quá trình phân quyền vẫn diễn ra với danh tính này.

### Authentication & Authorization per request

Khi một request được gửi đến server, nó sẽ đi qua filter chain (chuỗi các bộ lọc) của Spring Security:
Lấy ví dụ với 1 request login

1. **Security Filters** (bộ lọc) : Mọi thứ trong Spring Security đều bắt đầu từ bộ lọc Servlet.
   `UsernamePasswordAuthenticationFilter` sẽ chịu trách nhiệm chính xử lý yêu cầu login.
   Nó sẽ chặn các request đến endpoint `/login` (default), trích xuất username, password từ `RequestBody
   `, sau đó nó tạo ra đối tượng `UsernamePasswordAuthenticationToken` - là một implement của `Authentication`. Tại thời
   điểm này, token ở trạng thái chưa được xác thực (authenticated = false)
2. **Authentication Manager** (trình quản lý xác thực) : `UsernamePasswordAuthenticationFilter` sẽ chuyển token chưa xác
   thực này cho `
   AuthenticationManager` - nó là một interface có duy nhất một method `authenticate(Authentication authentication)`.
   Nhiệm vụ của nó là điều phối quá trình xác thực.
   Trong hầu hết trường hợp, `AuthenticationManager` sẽ sử dụng `ProviderManager` - một implement phổ biến của nó.
   `ProviderManager` không thực hiện các logic xác thực, mà nó sẽ ủy quyền công việc này cho một hoặc nhiều
   `AuthenticationProvider` của nó.
3. **AuthenticationProvider** (nhà cung cấp các xác thực) : `ProviderManager` sẽ duyệt qua danh sách các
   `AuthenticationProvider` mà nó quản lý để tìm một provider có thể xử lý loại token cụ thể. Mỗi provider có method
   `support()` để xác định xem nó có thể xử lý loại token nào.
   Ví dụ, `DaoAuthenticationProvider` là một implement phổ biến của `AuthenticationProvider`, nó thực hiện logic xác
   thực username password. Để làm được điều này nó cần 2 thành phần phụ trợ:
    - `UserDetailsService` : Đây là 1 interface của Spring security chứa các thông tin về username, passwword mã hóa,
      danh sách các quyền `GrantedAuthority` mà
      dev phải implements, nó có duy nhất 1 method
      `loadUserByUsername(String username)` để tìm kiếm user theo username trong DB. Nếu không tìm thấy method này ném
      ra `UsernameNotFoundException`. Nếu tìm thấy, nó trả về một đối tượng `UserDetails` chứa thông tin user.
    - `PasswordEncoder` : Sau khi `UserDetailsService` trả về `UserDetails`, `DaoAuthenticationProvider` đã có được
      password mã hóa từ DB. Nó dùng mật khẩu này so sánh với mật khẩu thô trong Authentication token. Để so sánh, nó sử
      dụng
      `PasswordEncoder` - một interface có các method để mã hóa và so sánh mật khẩu.
      `DaoAuthenticationProvider` gọi method `matches(rawPassword, encodedPassword)` của `PasswordEncoder` để so sánh.
      Nếu mật khẩu khớp, `DaoAuthenticationProvider` tạo ra một đối tượng `UsernamePasswordAuthenticationToken`
      mới với authenticated = true, chứa thông tin user và các quyền hạn.
4. **Security Context** (ngữ cảnh bảo mật) : Xác thực thành công thì thằng `ProviderManager` nhận lại token và trả về
   cho `UsernamePasswordAuthenticationFilter`, nó lưu token này vào `SecurityContextHolder` - thằng này lại dùng một
   `ThreadLocal` để lưu trữ SecurityContext, đảm bảo thông tin đã xác thực của người dùng có sẵn trong suốt vòng đời của
   một request và chỉ trong phạm vi thread đó. Từ đây dev lấy thông tin user từ
   `SecurityContextHolder.getContext().getAuthentication()` để sử dụng

Với các cơ chế xác thực khác, quá trình có thể khác một chút, nhưng về cơ bản vẫn tuân theo các bước trên. Ta hoàn toàn
có thể tùy chỉnh các thành phần này để phù hợp với yêu cầu bảo mật cụ thể của ứng dụng.

Hết Authen rồi, giờ đếnn Author, sau bước 4, khi token(`Authentication`) đã được lưu trong `SecurityContextHolder`, quá
trình phân quyền
bắt đầu

5. **AuthorizationFilter** : Một đối tượng được đặt gần cuối chuỗi bộ lọc. Nó chịu trách nhiệm thực thi các quy tắc ủy
   quyền cho các Http request. Bộ lọc này lấy `Authentication` từ Context ra để kiểm tra các quyền hạn của user
   hiện tại và quyết định xem họ có được phép truy cập vào tài nguyên yêu cầu hay không.
    - Nếu user có quyền truy cập, request được phép tiếp tục.
    - Nếu user không có quyền, bộ lọc sẽ chặn request và trả về lỗi 403 (Forbidden).
6. **AuthorizationManager** (Trình quản lý ủy quyền) : `AuthorizationFilter` ủy thác công việc kiểm tra quyền cho thằng
   này - một functional interface có method `check()`, nó nhận vào `Authentication` và `Object` đại diện cho tài nguyên
   được truy cập. `AuthorizationManager` sẽ kiểm tra xem `Authentication` có đủ quyền để truy cập tài nguyên hay không.
   Nếu có, nó trả về `AuthorizationDecision` với `granted = true`, ngược lại là false.
   Có nhiều implements của `AuthorizationManager`:
    - `RequestMatcherDelegatingAuthorizationManager` : Đây là trình quản lý cốt lõi khi cấu hình bảo mật cho HTTP
      request. Nó chứa 1 danh sách các cặp `RequestMatcher` với `AuthorizationManager`. Khi 1 request đến, nó tìm
      `RequestMatcher` đầu tiên khớp với request và ủy quyền quyết định cho `AuthorizationManager` tương ứng.
    - `AuthorityAuthorizationManager` : Là một implement đơn giản. Nó kiểm tra xem `Authentication` có chứa 1 hoặc
      nhiều `GrantedAuthority` cụ thể hay không. Ví dụ khi ta viết `requestMatcher(/admin/**).hasRole("ADMIN")` trong
      cấu hình bảo mật, Spring sẽ sử dụng `AuthorityAuthorizationManager` để kiểm tra xem `Authentication` có quyền
      `ROLE_ADMIN`
      hay không.
7. **Method Security** : Ngoài việc phân quyền dựa trên URL, Spring cho phép bảo mật ở cấp độ method trong service hoặc
   component vằng các annotation `@PerAuthorize` và `@PostAuthorize`:
    - `@PerAuthorize` : Anno này được xử lý khi method được thực thi, nó sử dụng Spring Expression Language (SpEL) để
      đinh nghĩa các quy tắc phức tạp. Nó mạnh mẽ trong việc kiểm tra quyền sở hữu dữ liệu
    - `@PostAuthorize` : Anno này được xử lý sau khi method được thực thi và có giá trị trả về. Nó cũng dùng SpEL và
      truy cập vào giá trị trả về của method thông qua biến `returnObject`. Nó mạnh mẽ khi logic kiểm tra quyền phụ
      thuộc vào chính đối tượng được truy xuất

### Chú thích
