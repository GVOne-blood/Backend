# Web Storage

## Các loại web storage phổ biến

### Local Storage

> Local Storage là một phần của Web storage API, cho phép các trang web lưu trữ data dưới dạng key-value ngay trên thiết
> bị của user. Mục đích của nó là duy trì trạng thái của web qua các lần truy cập khác nhau mà không cần đến cookie hay
> sự can thiệt từ máy chủ

Về bản chất, có thể coi Local storage là một tập tin dữ liệu nhỏ, gắn liền với origin cụ thể. Mỗi origin sẽ có 1 vùng
local storage riêng biệt và không thể truy cập giữa các vùng đó

Đặc điểm :

- Dung lượng 1 local storage thường từ 5-10MB
- Lifecycle bền vững và không có thời gian hết hạn tự động. Nó sẽ tồn tại cho đến khi:
    - User xóa dữ liệu duyệt web
    - Web sử dụng Javascript để xóa data
- Scope local tuân thủ chính sách người dùng gốc, nghĩa là web nào dùng local web đó
- Mọi thao tác với local đều ở phía client, data không bao giờ tự động gửi lên server
- Các thao tác đọc ghi trong local là đồng bộ, cac script đọc ghi local phải đợi nếu tiến trình đọc ghi trước chưa xong
- Local chỉ lưu String, mọi giá trị khác sẽ được convert thành string

Cú pháp gọi local storage

```
window.localStorage()
```

Nhược điểm :

- Dễ bị tấn công XSS : Vì data trong local có thể được truy cập bằng bất kỳ đoạn script nào
  chạy trên trang, nếu web có lỗ hổng XSS => dễ bị lộ data
- Vì thao tác đọc ghi là sync nên nếu data ghi quá lớn có thể gây lag cho browser

### Session storage

> Session storage là một phần của web storage API, nó cung cấp cơ chế để lưu được data dưới dạng key-value, điểm biến nó
> khác biệt so với local storage là lifecycle - data chỉ tồn tại trong 1 phiên duyệt web

Khái niệm session của web khác so với khái niệm session trên server, ở đây nó ám chỉ thời gian sống của 1 tab hay cửa sổ
trình duyệt. Nghiễm nhiên chỉ cần đóng tab là mất data, trường hợp sao chép tab thì session cũng được sao chép

Nếu 2 tab cùng truy cập vào 1 domain, 2 session storage khác biệt được tạo ra, data của bên A sẽ không thể được truy cập
bởi bên B và ngược lại

Đặc điểm :

- Dung lượng 5-10MB cho mỗi origin
- Lifecycle theo phiên của tab
- Scope của nó đảm bảo dữ liệu không được chia sẻ giữa các tab nhưu local storage, ngay cả khi chúng có cùng 1 origin
- Cơ chế cũng là client và sync
- Dữ liệu lưu trữ dạng string

Cú pháp

```
window.sessionStorage
```

Nhược điểm giống local

### Cookie

> Cookie là một
> cơ chế giao tiếp stateful
> được xây dựng trên nền stateless của HTTP

Lifecycle :

1. Khởi tạo : Server khi cần đính kèm thông tin phụ (xác thực, phiên) cùng với request để gửi lên client, nó sẽ đính kèm
   1 or nhiều `Set-cookie` vào trong HTTP response và gửi cho trình duyệt
   VD HTTP response header :

```
HTTP/1.1 200 OK
Content-Type: application/json
Set-Cookie: session-id=abc123xyz; Path=/; HttpOnly; Max-Age=3600
Set-Cookie: theme=dark; Path=/; Max-Age=31536000
```

2. Lưu trữ : Trình duyệt phân tích các `Set-cookie` nhận được và lưu nó. Thường thì browser quản lý 1 kho cookie, gọi là
   cookie jar, nơi quản lý cho từng

3. Request : user thực hiện request đến cùng 1 domain, trước đó, cookie jar tự tìm và
   đính kèm cookie phù hợp vào header của request
   VD :

```
GET /api/user/profile HTTP/1.1
Host: myapp.com
Cookie: session-id=abc123xyz; theme=dark
```

4. Server : nhận được request, phân tích và lấy ra cookie và dùng nó

Có trạng thái là vì thế

Cấu trúc

- Data : `key-value`, giá trị của value thường được mã hóa
- Expires : Xác định thời điểm cookie hết hạn, nếu không có cookie sẽ thành session cookie
- Max-Age : Xác định thời gian sống của cookie, được ưu tiên hơn nếu cả expires và max-age cùng tồn tại
- Domain : Chỉ định domain mà cookie thuộc về, nếu không được đặt thì mặc định sẽ là domain server tạo ra nó
- Path : Chỉ đường dẫn trên server mà cookie sẽ được gửi đến
- Secure : Cờ bảo mật, khi bật thì trình duyệt sẽ chỉ gửi cookie này qua HTTPS, không gửi qua HTTP
- HttpOnly :Cờ bảo mật, khi bật thì script javascript không thể gọi được cookie phía client
- SameSite<Strict|Lax|None> : Cờ bảo mật, kiểm soát việc gửi cookie trong các request từ 1 trang web khác
    - Strict : Chỉ gửi request từ chính site đó
    - Lax : Default trên nhiều trình duyệt, cho phép user điều hướng đến site của mình, chặn các request ngầm (ajax)
    - None : Cho phép gửi trong mọi site-cross, bắt buộc phải bật secure

### Chú thích

1. **origin** : Được định nghĩa là sự kết hợp của Giao thức, Domain và Port. Ví dụ : http://example.com:8080
   và http://example.com:3000 là 2 origin khác nhau