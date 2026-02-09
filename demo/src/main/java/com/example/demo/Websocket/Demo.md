# Websocket 
websocket có 2 tầng, 1 tầng raw websocket và tầng giao thức tin nhắn cấp cao hơn như STOMP, MQTT,...
## Handshake (cơ chế bắt tay)
> WebSocket sử dụng cơ chế Handshake (bắt tay) để thiết lập kết nối giữa client và server. Quá trình này bắt đầu khi client
> gửi một yêu cầu HTTP đặc biệt đến server, yêu cầu nâng cấp kết nối từ HTTP sang websocket.

Luồng hoạt động: 
1. CLient gửi 1 yêu cầu HTTP GET request đến endpoint của server định nghĩa (vd: `/ws`). Request này chứ các header đặc biệt như:
   - `Connection : Upgrade`: Báo hiệu mong muốn thay đổi giao thức
   - `Upgrade : websocket` : Chỉ định giao thức mới là websocket
   - `Sec-WebSocket-Key`: Một chuỗi ngẫu nhiên mã hóa  Base64 để đảm bảo server thực sự hỗ trợ websocket chứ không phải HTTP
2. Server sẽ response lại với mã `101 - Switching protocols` nếu Spring boot được cấu hình đúng
3. Kết nối TCP bây giờ sẽ không bị đóng nữa mà được giữ lại để truyền tải các frame dữ liệu theo chuẩn websocket
4. 