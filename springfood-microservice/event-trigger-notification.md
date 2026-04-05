<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" style="height:64px;margin-right:32px"/>

# ây bạn ơi bạn có hiểu yêu cầu không vậy ? cái ảnh tôi gửi bạn chỉ tham khảo bố cục trình bày của nó thôi, bạn cần tự đi research nghiệp vụ khi nào thì sẽ gửi thông báo của các sàn thương mại điện tử với các tác nhân từ customer đến shop owner hay administrator kia kìa

README – Thiết kế event trigger cho hệ thống Notification (E‑Commerce Marketplace)
==================================================================================

Hệ thống dưới đây mô tả các event trigger gửi thông báo trong một sàn thương mại điện tử kiểu marketplace với 3 tác nhân chính: Customer (người mua), Shop Owner (người bán) và Administrator (ban vận hành). Các mốc sự kiện được tham chiếu từ thực tế triển khai ở các nền tảng marketplace/multi‑vendor như Mercur (buyer/seller/admin notifications) và các best‑practice về thông báo đơn hàng – vận chuyển – trả hàng.[^1][^2][^3][^4][^5]

***

## 1. Nguyên tắc chung

- Mọi thông báo dưới đây là **transactional notification** (gắn với 1 hành động, 1 đơn, 1 yêu cầu cụ thể), không bao gồm notification marketing/broadcast.
- Mỗi event có thể bắn ra nhiều notification tới các vai trò khác nhau (customer, shop, admin); logic routing nên cấu hình được theo môi trường (prod, staging).
- Hệ thống hỗ trợ tối thiểu 3 kênh: in‑app notification, email, push; mỗi event cần cho phép bật/tắt từng kênh theo loại người dùng.
- Các event về trạng thái đơn hàng/vận chuyển phải **idempotent** theo order_status/shipping_status để tránh gửi trùng khi hệ thống retry.
- Người dùng (customer/shop) có thể tắt một số nhóm thông báo không quan trọng (ví dụ marketing, nhắc nhở mềm), nhưng không được tắt các thông báo pháp lý/bảo mật ( đổi mật khẩu, khoá tài khoản, thanh toán…).

***

## 2. Event cho Tài khoản \& Xác thực

| STT | Module | Trigger | Actions | Receiver |
| :-- | :-- | :-- | :-- | :-- |
| 1 | Account \& Security | Customer đăng ký tài khoản mới thành công | Gửi email + in‑app chào mừng, yêu cầu xác minh email/số điện thoại | Customer |
| 2 | Account \& Security | Customer xác minh email/số điện thoại thành công | Gửi thông báo xác nhận kích hoạt tài khoản | Customer |
| 3 | Account \& Security | Yêu cầu đặt lại mật khẩu (forgot password) | Gửi email/SMS chứa link/OTP đặt lại mật khẩu | Customer / Shop Owner |
| 4 | Account \& Security | Đặt lại mật khẩu thành công | Gửi thông báo bảo mật “Mật khẩu của bạn đã được thay đổi” | Customer / Shop Owner |
| 5 | Account \& Security | Đăng nhập từ thiết bị/địa chỉ IP lạ | Gửi cảnh báo bảo mật, kèm nút “Đó không phải tôi” | Customer / Shop Owner |
| 6 | Account \& Security | Shop Owner gửi yêu cầu đăng ký mở shop | Gửi xác nhận đã nhận hồ sơ, thông báo cho Admin có yêu cầu mới | Shop Owner, Administrator |
| 7 | Account \& Security | Admin phê duyệt hồ sơ shop | Thông báo “Shop của bạn đã được phê duyệt”, mở quyền truy cập dashboard | Shop Owner |
| 8 | Account \& Security | Admin từ chối hồ sơ shop | Thông báo lý do từ chối, hướng dẫn cập nhật hồ sơ | Shop Owner |
| 9 | Account \& Security | Tài khoản bị tạm khoá (fraud, vi phạm chính sách) | Gửi cảnh báo khoá, lý do ngắn gọn và hướng dẫn khiếu nại | Customer / Shop Owner |
| 10 | Account \& Security | Tài khoản được mở khoá | Gửi thông báo khôi phục quyền truy cập | Customer / Shop Owner |


***

## 3. Event cho Đơn hàng \& Thanh toán

Các mốc trạng thái bám theo thông lệ: tạo đơn, chờ thanh toán, thanh toán thành công/thất bại, huỷ đơn, hoàn tiền… giống các hướng dẫn order status/notification phổ biến.[^6][^7][^5][^1]


| STT | Module | Trigger | Actions | Receiver |
| :-- | :-- | :-- | :-- | :-- |
| 11 | Order \& Payment | Customer đặt đơn thành công (order_created – trước khi thanh toán) | Gửi xác nhận đã tiếp nhận đơn, hiển thị tổng tiền, phương thức thanh toán, thời gian giữ đơn | Customer |
| 12 | Order \& Payment | Thanh toán online tạo nhưng đang chờ (payment_pending) | Gửi thông báo “Đang chờ thanh toán”, hướng dẫn hoàn tất, kèm link thanh toán lại | Customer |
| 13 | Order \& Payment | Thanh toán thành công (payment_success) | Gửi hoá đơn/receipt, cập nhật trạng thái đơn sang “Đã thanh toán – chờ xử lý”; thông báo cho Shop có đơn mới đã thanh toán | Customer, Shop Owner |
| 14 | Order \& Payment | Thanh toán thất bại (payment_failed) | Gửi thông báo lỗi, lý do ngắn (thẻ bị từ chối, hết hạn…), đề xuất phương thức khác | Customer |
| 15 | Order \& Payment | Thanh toán COD được xác nhận (shop xác nhận sẽ chuẩn bị hàng) | Gửi thông báo xác nhận đặt hàng COD thành công; báo cho Shop chuẩn bị hàng | Customer, Shop Owner |
| 16 | Order \& Payment | Customer huỷ đơn trước khi shop xác nhận | Gửi xác nhận huỷ đơn cho Customer; gửi thông báo huỷ cho Shop (đơn không cần xử lý nữa) | Customer, Shop Owner |
| 17 | Order \& Payment | Shop từ chối/huỷ đơn (hết hàng, sai giá…) | Gửi thông báo huỷ đơn + lý do cho Customer; gửi log cho Admin để kiểm soát chất lượng shop | Customer, Administrator |
| 18 | Order \& Payment | Hệ thống auto‑cancel đơn quá hạn thanh toán | Gửi thông báo huỷ do không thanh toán kịp; ghi nhận cho Shop (không cần xử lý) | Customer, Shop Owner |
| 19 | Order \& Payment | Shop cập nhật chi phí (phí ship, phụ thu) trước khi giao | Gửi thông báo yêu cầu Customer xác nhận lại tổng tiền; nếu Customer đồng ý/không đồng ý, bắn event riêng tới Shop \& Admin | Customer, Shop Owner, Administrator |
| 20 | Order \& Payment | Customer yêu cầu hoàn tiền (refund_requested) | Gửi xác nhận đã nhận yêu cầu hoàn tiền cho Customer; thông báo cho Shop \& Admin để xử lý | Customer, Shop Owner, Administrator |
| 21 | Order \& Payment | Refund được phê duyệt và trả về ví/thẻ (refund_completed) | Gửi thông báo “Đã hoàn xxx” cho Customer; thông báo cho Shop về kết quả hoàn tiền | Customer, Shop Owner |
| 22 | Order \& Payment | Refund bị từ chối | Gửi thông báo lý do từ chối cho Customer; thông báo cho Shop nếu lỗi từ phía shop | Customer, Shop Owner |


***

## 4. Event cho Vận chuyển \& Giao hàng

Danh sách dưới đây bám sát các mốc shipping notification phổ biến như “Order created, shipment picked up, in transit, out for delivery, delivered, failed attempt, return created/received…”.[^7][^5][^8][^1]


| STT | Module | Trigger | Actions | Receiver |
| :-- | :-- | :-- | :-- | :-- |
| 23 | Shipping \& Delivery | Đơn chuyển sang trạng thái “Đang xử lý tại kho” | Thông báo cho Customer đơn đang được chuẩn bị; thông báo cho Shop phải đóng gói trước deadline | Customer, Shop Owner |
| 24 | Shipping \& Delivery | Tạo vận đơn (shipment_created) | Gửi thông báo kèm mã vận đơn \& link tracking cho Customer; thông báo cho Shop rằng đơn đã bàn giao cho đơn vị vận chuyển | Customer, Shop Owner |
| 25 | Shipping \& Delivery | Hàng được đơn vị vận chuyển lấy (shipment_picked_up) | Thông báo đơn đang trên đường vận chuyển | Customer |
| 26 | Shipping \& Delivery | Hàng đang trên đường (in_transit / arrived_at_hub) | Gửi update trạng thái (optional, có thể gom theo ngày) | Customer |
| 27 | Shipping \& Delivery | Hàng sắp giao (out_for_delivery) | Gửi thông báo dự kiến giao trong ngày + khoảng thời gian dự kiến | Customer |
| 28 | Shipping \& Delivery | Giao hàng thành công (delivered) | Thông báo giao thành công, mời đánh giá sản phẩm/shop; cập nhật KPI cho Shop | Customer, Shop Owner |
| 29 | Shipping \& Delivery | Giao hàng không thành công (recipient_not_at_home / failed_attempt) | Gửi thông báo lý do thất bại, hướng dẫn đặt lại lịch giao; thông báo cho Shop để chủ động liên lạc nếu cần | Customer, Shop Owner |
| 30 | Shipping \& Delivery | Đơn bị chuyển hoàn (return_to_sender) | Gửi thông báo đơn đang/đã bị hoàn về, nêu lý do (sai địa chỉ, không nhận hàng…) | Customer, Shop Owner |
| 31 | Shipping \& Delivery | Đơn bị trễ so với ETA | Thông báo xin lỗi, cập nhật ETA mới; có thể kèm voucher nhỏ (tuỳ chính sách) | Customer |
| 32 | Shipping \& Delivery | Kiện hàng bị mất/hỏng | Thông báo điều tra, tiếp theo là luồng refund/đền bù; alert cho Admin để giám sát | Customer, Shop Owner, Administrator |
| 33 | Shipping \& Delivery | Customer tạo yêu cầu trả hàng/đổi hàng (return_created / exchange_req) | Thông báo đã nhận yêu cầu, gửi hướng dẫn đóng gói/trả hàng; thông báo cho Shop \& Admin để duyệt | Customer, Shop Owner, Administrator |
| 34 | Shipping \& Delivery | Kho/shop đã nhận lại hàng trả (return_received) | Thông báo cho Customer về việc đã nhận hàng, kèm thời gian dự kiến hoàn tiền/đổi hàng | Customer |


***

## 5. Event cho Tương tác Khách hàng (Chat, Q\&A, Đánh giá)

| STT | Module | Trigger | Actions | Receiver |
| :-- | :-- | :-- | :-- | :-- |
| 35 | Customer Interaction | Customer gửi tin nhắn mới cho shop (chat in‑app) | Thông báo real‑time cho Shop có tin nhắn mới, highlight hội thoại trong inbox | Shop Owner |
| 36 | Customer Interaction | Shop trả lời tin nhắn của customer | Thông báo cho Customer (in‑app + optional push/email nếu đang offline) | Customer |
| 37 | Customer Interaction | Customer đặt câu hỏi Q\&A ở trang sản phẩm | Thông báo cho Shop có câu hỏi mới; optional thông báo cho Admin nếu có từ khóa nhạy cảm | Shop Owner, Administrator |
| 38 | Customer Interaction | Shop trả lời Q\&A | Thông báo cho Customer đã hỏi; cập nhật hiển thị public ở trang sản phẩm | Customer |
| 39 | Customer Interaction | Đơn giao thành công sau X ngày nhưng chưa có đánh giá | Gửi nhắc nhở nhẹ nhàng mời Customer viết review | Customer |
| 40 | Customer Interaction | Customer gửi đánh giá (rating + review) mới cho đơn/sản phẩm | Thông báo cho Shop có review mới; optional thông báo cho Admin nếu rating quá thấp | Shop Owner, Administrator |
| 41 | Customer Interaction | Review bị customer khác báo cáo vi phạm | Thông báo cho Admin để kiểm duyệt | Administrator |
| 42 | Customer Interaction | Admin ẩn/xoá/hạ cấp review sau kiểm duyệt | Thông báo cho người viết review (nếu cần) và cho Shop | Customer (review owner), Shop Owner |


***

## 6. Event cho Khuyến mãi, Sản phẩm \& Kho hàng

| STT | Module | Trigger | Actions | Receiver |
| :-- | :-- | :-- | :-- | :-- |
| 43 | Product \& Inventory | Shop tạo sản phẩm mới, chờ duyệt | Thông báo cho Admin có sản phẩm mới chờ kiểm duyệt | Administrator |
| 44 | Product \& Inventory | Admin phê duyệt sản phẩm | Thông báo sản phẩm đã được duyệt và hiển thị trên sàn | Shop Owner |
| 45 | Product \& Inventory | Admin từ chối sản phẩm | Thông báo lý do từ chối, hướng dẫn chỉnh sửa | Shop Owner |
| 46 | Product \& Inventory | Sản phẩm hết hàng | Thông báo cho Shop để nhập hàng; nếu có customer “theo dõi”, lưu lại để bắn noti khi có hàng | Shop Owner |
| 47 | Product \& Inventory | Sản phẩm có hàng lại (back‑in‑stock) | Gửi thông báo cho các Customer đã đăng ký “Báo tôi khi có hàng” | Customer |
| 48 | Promotion \& Marketing | Gán voucher/coupon cá nhân cho 1 customer | Thông báo chi tiết voucher, điều kiện áp dụng, ngày hết hạn | Customer |
| 49 | Promotion \& Marketing | Sản phẩm trong wishlist/đã xem giảm giá mạnh hoặc tham gia flash sale | Thông báo cơ hội giảm giá (opt‑in, có thể tắt trong phần cài đặt thông báo marketing) | Customer |
| 50 | Promotion \& Marketing | Shop tạo chiến dịch khuyến mãi mới | Thông báo cho Admin (nếu cần duyệt) hoặc chỉ log lại nếu auto‑approve | Administrator |


***

## 7. Event cho Quản trị \& Giám sát Hệ thống

| STT | Module | Trigger | Actions | Receiver |
| :-- | :-- | :-- | :-- | :-- |
| 51 | Admin \& Operation | Có report khiếu nại mới (fraud, hàng giả, vi phạm chính sách…) | Thông báo cho nhóm CS/Compliance; đính kèm link đến chi tiết case | Administrator |
| 52 | Admin \& Operation | Tỷ lệ huỷ đơn / refund của 1 shop vượt ngưỡng | Gửi cảnh báo cho Admin phụ trách ngành hàng và cho chính Shop để tự cải thiện | Shop Owner, Administrator |
| 53 | Admin \& Operation | Có spike bất thường về thanh toán thất bại hoặc nghi ngờ gian lận | Gửi cảnh báo realtime cho Admin, có thể bật thêm SMS/Slack/Webhook nội bộ | Administrator |
| 54 | Admin \& Operation | Hệ thống tích hợp vận chuyển/thanh toán bị down (health‑check fail) | Gửi cảnh báo kỹ thuật cho on‑call; optional banner in‑app cho shop/customer khi tạo đơn | Administrator |


***

## 8. Gợi ý cấu trúc kỹ thuật (tóm tắt)

- Mỗi dòng trong bảng trên tương ứng với **1 Notification Type** (hoặc 1 Event + nhiều Notification Type cho từng vai trò).
- Đề xuất lưu metadata: `code`, `module`, `description`, `default_channels`, `available_receivers`, `template_keys`.
- Hệ thống nghiệp vụ bắn **domain event** (ví dụ `order.paid`, `shipment.delivered`), service Notification subscribe và map sang Notification Type tương ứng (giống cách các hệ thống marketplace event‑driven vận hành).[^3][^9]

Tài liệu này có thể dùng làm README cho repo service Notification, đồng thời là input để đội Product/CS soát lại nghiệp vụ và ưu tiên triển khai từng cụm event.
<span style="display:none">[^10][^11][^12][^13][^14][^15][^16][^17][^18][^19][^20][^21][^22][^23][^24][^25][^26][^27][^28][^29][^30]</span>

<div align="center">⁂</div>

[^1]: https://outvio.com/blog/shipping-notifications/

[^2]: https://docs.mercurjs.com/core-concepts/notifications

[^3]: https://www.mercurjs.com/features/notifications-event-feeds

[^4]: https://www.pubnub.com/blog/ecommerce-push-notifications/

[^5]: https://wareiq.com/resources/blogs/shipping-notifications/

[^6]: https://dev.holbi.co.uk/blog/success-case-setting-up-order-status-update-email-notifications

[^7]: https://ecommerce.aftership.com/insight/shipment-notifications/

[^8]: https://www.lateshipment.com/blog/understanding-shipping-notifications-types-benefits-and-best-practices/

[^9]: https://docs.aws.amazon.com/marketplace/latest/userguide/notifications.html

[^10]: https://music.youtube.com/playlist?list=PLbBBKJlaj-EWi1Ud2DGpaiNuZmv3NZvvc

[^11]: https://www.youtube.com/playlist?list=PLhJojf9cm-Py_YOJUgWZbaPq2eR0V_Y6o

[^12]: https://www.youtube.com/playlist?list=PLdXegvZpiwp7FY9ez0vi9FMPVD0Df9ux0

[^13]: https://music.youtube.com/playlist?list=PLinUhzXNlgD_PsaunK3zIFgCZfwamwkrO

[^14]: https://music.youtube.com/playlist?list=PL8mG-RkN2uTw7PhlnAr4pZZz2QubIbujH

[^15]: https://music.youtube.com/playlist?list=PLxjM5m7-ZGWI1T-xai11dnZvkEr7ww7SG\&si=E-OOhamP80baYVFR

[^16]: https://music.youtube.com/playlist?list=PLWgDwxNQz66rifrNVIwXn9u47obfibzan\&si=VzLTNM9d4Crw-M-8

[^17]: https://music.youtube.com/playlist?list=PLMJxZ4WugjO1sW6Vg-VQPBDrYBF__SDoQ

[^18]: https://music.youtube.com/playlist?list=PLC-KvrDf_kp_O0dXgVOM1hLkWgfQYRDdF

[^19]: https://music.youtube.com/playlist?list=PLFHZZH5JnHGKoSoK6bPHRvRXZrGCA7Ngd

[^20]: https://www.youtube.com/playlist?list=PL07Y6cfPTvX0iWdPZ4gth0C5Dtpol1XB2

[^21]: https://music.youtube.com/playlist?list=PLPKQdcSp3PiTZwt3GeEUxcOMPqqninWWY

[^22]: https://music.youtube.com/playlist?list=PLlJdNiwAJuoih62XxYIUqOc4PJlYe2OYo

[^23]: https://music.youtube.com/playlist?list=PLH0QAgrVq2gNvQgTLIapjW1avrGEDJyqx\&si=2prVzQilCt-jLIqM

[^24]: https://music.youtube.com/playlist?list=PL3z8hr1H5sKrb3bx-NBgI1X-S9h7y0zJu

[^25]: https://www.onlineordering.help/article/222-how-to-notify-customers-of-order-status-change-admin-dashboard

[^26]: https://webkul.com/blog/multivendor-marketplace-for-shopify-seller-notification/

[^27]: https://www.reddit.com/r/webdev/comments/oardq9/how_to_notify_backend_administrator_of_incoming/

[^28]: https://woocommerce.com/document/notifications-for-woocommerce/

[^29]: https://expertsender.com/blog/uses-of-web-push-notifications/

[^30]: https://docs.aws.amazon.com/marketplace/latest/buyerguide/buyer-notifications-email.html

