--
-- PostgreSQL database dump
--

\restrict QxSNs6Y7gg0WYdUQZWJ7XdTVtBN32O4LtJu91X0hzc6frcJXvlQatpAR9BXcs0I

-- Dumped from database version 17.6
-- Dumped by pg_dump version 17.6

-- Started on 2025-09-09 16:09:57

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 5084 (class 0 OID 33559)
-- Dependencies: 231
-- Data for Name: address; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.address (address_id, ward, street, city, details, user_id, updated_at, created_at) VALUES ('addr_cust01_01', 'Phường Bến Nghé', '456 Lê Lợi', 'Thành phố Hồ Chí Minh', 'Tòa nhà Bitexco, Lầu 10', 'user_customer_01', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.address (address_id, ward, street, city, details, user_id, updated_at, created_at) VALUES ('addr_cust01_02', 'Phường Thảo Điền', '12 Quốc Hương', 'Thành phố Hồ Chí Minh', 'Chung cư Masteri', 'user_customer_01', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.address (address_id, ward, street, city, details, user_id, updated_at, created_at) VALUES ('addr_cust02_01', 'Phường Thượng Đình', '101 Nguyễn Trãi', 'Hà Nội', 'Gần Royal City', 'user_customer_02', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');


--
-- TOC entry 5092 (class 0 OID 33626)
-- Dependencies: 239
-- Data for Name: bank_account; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.bank_account (account_id, user_id, shop_id, bank_name, account_number, account_holder_name, is_default, is_verified, created_at, updated_at) VALUES ('bank_shop01', NULL, 'shop_binhan_01', 'Vietcombank', '0011001234567', 'TRAN VAN BINH', true, true, '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');


--
-- TOC entry 5085 (class 0 OID 33566)
-- Dependencies: 232
-- Data for Name: booking; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.booking (booking_id, user_id, booking_status, final_price, payment_method_name, address_id, customer_notes, created_at, updated_at) VALUES ('book_01', 'user_customer_01', 'COMPLETED', 140000.00, 'MOMO', 'addr_cust01_01', 'Giao hàng trong giờ hành chính.', '2024-08-10 09:30:00', '2024-08-11 14:00:00');
INSERT INTO public.booking (booking_id, user_id, booking_status, final_price, payment_method_name, address_id, customer_notes, created_at, updated_at) VALUES ('book_02', 'user_customer_02', 'PROCESSING', 55000.00, 'COD', 'addr_cust02_01', 'Vui lòng gọi trước khi giao.', '2024-08-11 11:00:00', '2024-08-11 11:05:00');
INSERT INTO public.booking (booking_id, user_id, booking_status, final_price, payment_method_name, address_id, customer_notes, created_at, updated_at) VALUES ('book_03', 'user_customer_01', 'CANCELLED', 70000.00, 'VNPAY', 'addr_cust01_02', 'Khách đổi ý.', '2024-08-12 15:00:00', '2024-08-12 18:00:00');


--
-- TOC entry 5086 (class 0 OID 33573)
-- Dependencies: 233
-- Data for Name: booking_item; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.booking_item (bill_id, booking_id, product_id, quantity, price_at_booking, updated_at, created_at) VALUES ('bitem_01', 'book_01', 'prod_01', 1, 85000.00, '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.booking_item (bill_id, booking_id, product_id, quantity, price_at_booking, updated_at, created_at) VALUES ('bitem_02', 'book_01', 'prod_02', 1, 55000.00, '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.booking_item (bill_id, booking_id, product_id, quantity, price_at_booking, updated_at, created_at) VALUES ('bitem_03', 'book_02', 'prod_05', 1, 25000.00, '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.booking_item (bill_id, booking_id, product_id, quantity, price_at_booking, updated_at, created_at) VALUES ('bitem_04', 'book_02', 'prod_06', 1, 30000.00, '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.booking_item (bill_id, booking_id, product_id, quantity, price_at_booking, updated_at, created_at) VALUES ('bitem_05', 'book_03', 'prod_03', 1, 70000.00, '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');


--
-- TOC entry 5087 (class 0 OID 33580)
-- Dependencies: 234
-- Data for Name: category; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.category (category_name, description, is_active, created_at, updated_at) VALUES ('Thịt Tươi Sống', 'Các loại thịt heo, bò, gà tươi', true, '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.category (category_name, description, is_active, created_at, updated_at) VALUES ('Hải Sản', 'Các loại cá, tôm, mực tươi và đông lạnh', true, '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.category (category_name, description, is_active, created_at, updated_at) VALUES ('Rau Củ Quả', 'Rau củ quả sạch, hữu cơ', true, '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.category (category_name, description, is_active, created_at, updated_at) VALUES ('Đồ Ăn Vặt', 'Các loại bánh kẹo, snack', true, '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');


--
-- TOC entry 5078 (class 0 OID 33516)
-- Dependencies: 225
-- Data for Name: feedback; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.feedback (feedback_id, user_id, product_id, rating, content, updated_at, created_at) VALUES ('feed_01', 'user_customer_01', 'prod_01', 5, 'Thịt rất tươi và ngon, đóng gói cẩn thận. Sẽ ủng hộ shop tiếp!', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.feedback (feedback_id, user_id, product_id, rating, content, updated_at, created_at) VALUES ('feed_02', 'user_customer_01', 'prod_02', 4, 'Ức gà chắc thịt, nhưng giao hàng hơi chậm một chút.', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');


--
-- TOC entry 5089 (class 0 OID 33595)
-- Dependencies: 236
-- Data for Name: notification; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.notification (notification_id, user_id, notify_type, title, content, link, is_read, created_at) VALUES ('noti_01', 'user_customer_01', 'ORDER_UPDATE', 'Đơn hàng #book_01 đã hoàn thành', 'Cảm ơn bạn đã mua sắm. Vui lòng để lại đánh giá cho sản phẩm.', '/orders/book_01', false, '2024-08-11 14:02:00');
INSERT INTO public.notification (notification_id, user_id, notify_type, title, content, link, is_read, created_at) VALUES ('noti_02', 'user_shop_owner_02', 'ORDER_UPDATE', 'Bạn có đơn hàng mới #book_02', 'Khách hàng Lê Thị Lan vừa đặt một đơn hàng mới.', '/shop/orders/book_02', true, '2024-08-11 11:01:00');


--
-- TOC entry 5083 (class 0 OID 33551)
-- Dependencies: 230
-- Data for Name: payment; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.payment (payment_name, description, is_active, created_at, updated_at) VALUES ('COD', 'Thanh toán khi nhận hàng (Cash On Delivery)', true, NULL, NULL);
INSERT INTO public.payment (payment_name, description, is_active, created_at, updated_at) VALUES ('MOMO', 'Thanh toán qua ví điện tử MoMo', true, NULL, NULL);
INSERT INTO public.payment (payment_name, description, is_active, created_at, updated_at) VALUES ('VNPAY', 'Thanh toán qua cổng VNPAY (Thẻ ATM, QR Code)', true, NULL, NULL);
INSERT INTO public.payment (payment_name, description, is_active, created_at, updated_at) VALUES ('ZALOPAY', 'Thanh toán qua ví điện tử ZaloPay', false, NULL, NULL);


--
-- TOC entry 5072 (class 0 OID 33468)
-- Dependencies: 219
-- Data for Name: permission; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.permission (permission_name, description, created_at, updated_at) VALUES ('user:read', 'Xem thông tin người dùng', NULL, NULL);
INSERT INTO public.permission (permission_name, description, created_at, updated_at) VALUES ('user:manage', 'Quản lý tất cả người dùng (khóa, mở khóa)', NULL, NULL);
INSERT INTO public.permission (permission_name, description, created_at, updated_at) VALUES ('shop:create', 'Tạo cửa hàng mới', NULL, NULL);
INSERT INTO public.permission (permission_name, description, created_at, updated_at) VALUES ('shop:read_own', 'Xem thông tin cửa hàng của mình', NULL, NULL);
INSERT INTO public.permission (permission_name, description, created_at, updated_at) VALUES ('shop:update_own', 'Cập nhật thông tin cửa hàng của mình', NULL, NULL);
INSERT INTO public.permission (permission_name, description, created_at, updated_at) VALUES ('shop:manage_all', 'Quản lý tất cả cửa hàng (phê duyệt, từ chối)', NULL, NULL);
INSERT INTO public.permission (permission_name, description, created_at, updated_at) VALUES ('product:create', 'Tạo sản phẩm mới cho cửa hàng của mình', NULL, NULL);
INSERT INTO public.permission (permission_name, description, created_at, updated_at) VALUES ('product:update_own', 'Cập nhật sản phẩm của cửa hàng mình', NULL, NULL);
INSERT INTO public.permission (permission_name, description, created_at, updated_at) VALUES ('product:delete_own', 'Xóa sản phẩm của cửa hàng mình', NULL, NULL);
INSERT INTO public.permission (permission_name, description, created_at, updated_at) VALUES ('order:create', 'Tạo đơn hàng mới (mua hàng)', NULL, NULL);
INSERT INTO public.permission (permission_name, description, created_at, updated_at) VALUES ('order:read_own', 'Xem đơn hàng của mình', NULL, NULL);
INSERT INTO public.permission (permission_name, description, created_at, updated_at) VALUES ('order:manage_shop', 'Quản lý đơn hàng của cửa hàng mình', NULL, NULL);
INSERT INTO public.permission (permission_name, description, created_at, updated_at) VALUES ('order:read_all', 'Xem tất cả đơn hàng trong hệ thống', NULL, NULL);


--
-- TOC entry 5075 (class 0 OID 33489)
-- Dependencies: 222
-- Data for Name: post; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 5077 (class 0 OID 33507)
-- Dependencies: 224
-- Data for Name: product; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.product (product_id, shop_id, name, description, "MSG", "EXP", product_status, price, wholesale_price, avg_rate, quantity, images, updated_at, created_at) VALUES ('prod_01', 'shop_binhan_01', 'Thịt Ba Rọi Heo VietGAP', 'Thịt heo sạch, không chất tăng trọng. Gói 500g.', '2024-09-01', '2024-09-08', 'AVAILABLE', 85000.00, 75000.00, 0.00, 50, '["images/prod01_1.jpg", "images/prod01_2.jpg"]', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.product (product_id, shop_id, name, description, "MSG", "EXP", product_status, price, wholesale_price, avg_rate, quantity, images, updated_at, created_at) VALUES ('prod_02', 'shop_binhan_01', 'Ức Gà Phi Lê Không Xương', 'Thích hợp cho người tập gym, ăn kiêng. Gói 500g.', '2024-09-01', '2024-09-07', 'AVAILABLE', 55000.00, 48000.00, 0.00, 100, '["images/prod02_1.jpg"]', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.product (product_id, shop_id, name, description, "MSG", "EXP", product_status, price, wholesale_price, avg_rate, quantity, images, updated_at, created_at) VALUES ('prod_03', 'shop_binhan_01', 'Cá Diêu Hồng Tươi', 'Cá tươi sống mỗi ngày, làm sạch sẵn. Con khoảng 800g.', '2024-09-04', '2024-09-05', 'AVAILABLE', 70000.00, 60000.00, 0.00, 30, '["images/prod03_1.jpg"]', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.product (product_id, shop_id, name, description, "MSG", "EXP", product_status, price, wholesale_price, avg_rate, quantity, images, updated_at, created_at) VALUES ('prod_04', 'shop_binhan_01', 'Trứng Gà Ta Thả Vườn', 'Vỉ 10 trứng gà ta thả vườn.', '2024-08-20', '2024-09-20', 'OUT_OF_STOCK', 35000.00, 30000.00, 0.00, 0, '["images/prod04_1.jpg"]', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.product (product_id, shop_id, name, description, "MSG", "EXP", product_status, price, wholesale_price, avg_rate, quantity, images, updated_at, created_at) VALUES ('prod_05', 'shop_dunghoang_02', 'Cải Bó Xôi Hữu Cơ', 'Trồng theo tiêu chuẩn hữu cơ, không thuốc trừ sâu. Bó 300g.', '2024-09-03', '2024-09-10', 'AVAILABLE', 25000.00, 20000.00, 0.00, 80, '["images/prod05_1.jpg"]', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.product (product_id, shop_id, name, description, "MSG", "EXP", product_status, price, wholesale_price, avg_rate, quantity, images, updated_at, created_at) VALUES ('prod_06', 'shop_dunghoang_02', 'Xà Lách Romana Đà Lạt', 'Giòn ngọt, thích hợp làm salad. Cây 500g.', '2024-09-03', '2024-09-09', 'AVAILABLE', 30000.00, 25000.00, 0.00, 120, '["images/prod06_1.jpg"]', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.product (product_id, shop_id, name, description, "MSG", "EXP", product_status, price, wholesale_price, avg_rate, quantity, images, updated_at, created_at) VALUES ('prod_07', 'shop_dunghoang_02', 'Cà Chua Bi Hữu Cơ', 'Cà chua bi hữu cơ, vị ngọt thanh. Hộp 500g.', '2024-09-02', '2024-09-12', 'UNLISTED', 40000.00, 35000.00, 0.00, 60, '["images/prod07_1.jpg"]', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');


--
-- TOC entry 5088 (class 0 OID 33588)
-- Dependencies: 235
-- Data for Name: product_category; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.product_category (product_id, category_name) VALUES ('prod_01', 'Thịt Tươi Sống');
INSERT INTO public.product_category (product_id, category_name) VALUES ('prod_02', 'Thịt Tươi Sống');
INSERT INTO public.product_category (product_id, category_name) VALUES ('prod_03', 'Hải Sản');
INSERT INTO public.product_category (product_id, category_name) VALUES ('prod_04', 'Thịt Tươi Sống');
INSERT INTO public.product_category (product_id, category_name) VALUES ('prod_05', 'Rau Củ Quả');
INSERT INTO public.product_category (product_id, category_name) VALUES ('prod_06', 'Rau Củ Quả');
INSERT INTO public.product_category (product_id, category_name) VALUES ('prod_07', 'Rau Củ Quả');


--
-- TOC entry 5082 (class 0 OID 33544)
-- Dependencies: 229
-- Data for Name: product_sale; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 5080 (class 0 OID 33530)
-- Dependencies: 227
-- Data for Name: product_tag; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.product_tag (product_id, tag_name) VALUES ('prod_01', 'VietGAP');
INSERT INTO public.product_tag (product_id, tag_name) VALUES ('prod_01', 'Tươi sống');
INSERT INTO public.product_tag (product_id, tag_name) VALUES ('prod_02', 'Tươi sống');
INSERT INTO public.product_tag (product_id, tag_name) VALUES ('prod_03', 'Tươi sống');
INSERT INTO public.product_tag (product_id, tag_name) VALUES ('prod_05', 'Hữu cơ');
INSERT INTO public.product_tag (product_id, tag_name) VALUES ('prod_06', 'Hữu cơ');


--
-- TOC entry 5071 (class 0 OID 33461)
-- Dependencies: 218
-- Data for Name: role; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.role (role_name, description, created_at, updated_at) VALUES ('ADMIN', 'Quản trị viên hệ thống, có toàn quyền.', NULL, NULL);
INSERT INTO public.role (role_name, description, created_at, updated_at) VALUES ('SHOP_OWNER', 'Chủ cửa hàng, quản lý sản phẩm và đơn hàng của shop mình.', NULL, NULL);
INSERT INTO public.role (role_name, description, created_at, updated_at) VALUES ('CUSTOMER', 'Khách hàng, có thể mua sắm và quản lý đơn hàng cá nhân.', NULL, NULL);


--
-- TOC entry 5074 (class 0 OID 33482)
-- Dependencies: 221
-- Data for Name: role_has_permission; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.role_has_permission (role_name, permission_name, updated_at, created_at) VALUES ('ADMIN', 'user:read', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.role_has_permission (role_name, permission_name, updated_at, created_at) VALUES ('ADMIN', 'user:manage', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.role_has_permission (role_name, permission_name, updated_at, created_at) VALUES ('ADMIN', 'shop:manage_all', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.role_has_permission (role_name, permission_name, updated_at, created_at) VALUES ('ADMIN', 'order:read_all', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.role_has_permission (role_name, permission_name, updated_at, created_at) VALUES ('SHOP_OWNER', 'shop:read_own', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.role_has_permission (role_name, permission_name, updated_at, created_at) VALUES ('SHOP_OWNER', 'shop:update_own', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.role_has_permission (role_name, permission_name, updated_at, created_at) VALUES ('SHOP_OWNER', 'product:create', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.role_has_permission (role_name, permission_name, updated_at, created_at) VALUES ('SHOP_OWNER', 'product:update_own', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.role_has_permission (role_name, permission_name, updated_at, created_at) VALUES ('SHOP_OWNER', 'product:delete_own', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.role_has_permission (role_name, permission_name, updated_at, created_at) VALUES ('SHOP_OWNER', 'order:manage_shop', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.role_has_permission (role_name, permission_name, updated_at, created_at) VALUES ('CUSTOMER', 'shop:create', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.role_has_permission (role_name, permission_name, updated_at, created_at) VALUES ('CUSTOMER', 'order:create', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.role_has_permission (role_name, permission_name, updated_at, created_at) VALUES ('CUSTOMER', 'order:read_own', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');


--
-- TOC entry 5081 (class 0 OID 33537)
-- Dependencies: 228
-- Data for Name: sale; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 5076 (class 0 OID 33496)
-- Dependencies: 223
-- Data for Name: shop; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.shop (shop_id, owner_id, shop_name, logo, total_product, total_sold, introduction, shop_status, updated_at, created_at) VALUES ('shop_binhan_01', 'user_shop_owner_01', 'Bếp Nhà Bình An', 'logos/binhan.png', 0, 0, 'Cung cấp thực phẩm sạch, an toàn cho mọi gia đình.', 'ACTIVE', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.shop (shop_id, owner_id, shop_name, logo, total_product, total_sold, introduction, shop_status, updated_at, created_at) VALUES ('shop_dunghoang_02', 'user_shop_owner_02', 'Vườn Rau Sạch Dũng Hoàng', 'logos/dunghoang.png', 0, 0, 'Rau củ quả hữu cơ trồng tại Đà Lạt.', 'ACTIVE', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.shop (shop_id, owner_id, shop_name, logo, total_product, total_sold, introduction, shop_status, updated_at, created_at) VALUES ('shop_haisan_03', 'user_customer_01', 'Hải Sản Tươi Sống Phan Thiết', 'logos/haisan.png', 0, 0, 'Giao hàng tận nơi trong ngày.', 'PENDING_APPROVAL', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');


--
-- TOC entry 5090 (class 0 OID 33603)
-- Dependencies: 237
-- Data for Name: shop_wallet; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.shop_wallet (wallet_id, shop_id, balance, pending_amount, locked_amount, created_at, updated_at) VALUES ('wallet_shop01', 'shop_binhan_01', 500000.00, 0.00, 0.00, '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.shop_wallet (wallet_id, shop_id, balance, pending_amount, locked_amount, created_at, updated_at) VALUES ('wallet_shop02', 'shop_dunghoang_02', 120000.00, 0.00, 0.00, '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');


--
-- TOC entry 5079 (class 0 OID 33523)
-- Dependencies: 226
-- Data for Name: tag; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.tag (tag_name, tag_description, updated_at, created_at) VALUES ('Hữu cơ', 'Sản phẩm đạt chuẩn hữu cơ', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.tag (tag_name, tag_description, updated_at, created_at) VALUES ('VietGAP', 'Sản phẩm đạt chuẩn VietGAP', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.tag (tag_name, tag_description, updated_at, created_at) VALUES ('Tươi sống', 'Sản phẩm tươi giao trong ngày', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.tag (tag_name, tag_description, updated_at, created_at) VALUES ('Khuyến mãi', 'Sản phẩm đang có chương trình giảm giá', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');


--
-- TOC entry 5093 (class 0 OID 33636)
-- Dependencies: 240
-- Data for Name: token; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 5070 (class 0 OID 33445)
-- Dependencies: 217
-- Data for Name: user; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public."user" (user_id, "firstName", "lastName", email, email_verified, status, phone, phone_verified, address, dob, username, password, avatar, is_deleted, last_login_at, updated_at, created_at) VALUES ('user_admin_01', 'Admin', 'Hệ Thống', 'admin@foodapp.com', true, 'ACTIVE', '0909090909', true, '123 Admin Street, District 1, HCMC', '1990-01-01', 'admin', '$2a$10$GGKD4ti4CgUgd2KPXL.AtevwBnN/AC0QOrWFc190EaWjjXGHRuCMG', 'avatars/admin.png', false, '2025-09-04 14:23:57.361417', '2025-09-05 15:32:56.581404', '2025-09-04 14:23:57.361417');
INSERT INTO public."user" (user_id, "firstName", "lastName", email, email_verified, status, phone, phone_verified, address, dob, username, password, avatar, is_deleted, last_login_at, updated_at, created_at) VALUES ('user_shop_owner_01', 'Bình', 'Trần Văn', 'binh.tran@shop.com', true, 'ACTIVE', '0987654321', true, '789 Hùng Vương, Hải Châu, Đà Nẵng', '1988-05-20', 'binh_shop', '$2a$10$N4dKDQoK8Z1eUruu1wkvR.fALzx7e9G2vfVFZQ6/x75iP74/c4LlG', 'avatars/binh.png', false, '2025-09-04 14:23:57.361417', '2025-09-05 15:32:56.911449', '2025-09-04 14:23:57.361417');
INSERT INTO public."user" (user_id, "firstName", "lastName", email, email_verified, status, phone, phone_verified, address, dob, username, password, avatar, is_deleted, last_login_at, updated_at, created_at) VALUES ('user_shop_owner_02', 'Dũng', 'Hoàng Trí', 'dung.hoang@shop.com', true, 'ACTIVE', '0978123456', true, '333 Võ Văn Tần, District 3, HCMC', '1992-11-15', 'dung_shop', '$2a$10$MJjEZvzmkwwl9zHDW48ZEekLjZgen35dYhc7ak8r7hmhzVcWBSe8S', 'avatars/dung.png', false, '2025-09-04 14:23:57.361417', '2025-09-05 15:32:57.019237', '2025-09-04 14:23:57.361417');
INSERT INTO public."user" (user_id, "firstName", "lastName", email, email_verified, status, phone, phone_verified, address, dob, username, password, avatar, is_deleted, last_login_at, updated_at, created_at) VALUES ('user_customer_01', 'An', 'Nguyễn Văn', 'an.nguyen@customer.com', true, 'ACTIVE', '0397130501', true, '456 Lê Lợi, District 1, HCMC', '1995-08-10', 'nguyenvana', '$2a$10$X6YYk/6b4iYafK6yoqxZfeofkQjn5xOOML7jHs9QRR41f9g3WPIg2', 'avatars/an.png', false, '2025-09-04 14:23:57.361417', '2025-09-05 15:32:57.107202', '2025-09-04 14:23:57.361417');
INSERT INTO public."user" (user_id, "firstName", "lastName", email, email_verified, status, phone, phone_verified, address, dob, username, password, avatar, is_deleted, last_login_at, updated_at, created_at) VALUES ('user_customer_02', 'Lan', 'Lê Thị', 'lan.le@customer.com', true, 'ACTIVE', '0912345678', false, '101 Nguyễn Trãi, Thanh Xuân, Hà Nội', '1998-02-25', 'lethilan', '$2a$10$JO.gjjJRx1E40gD97hDY9O012uLnhkDD3hXvhtL30o46dteVKkDbi', 'avatars/lan.png', false, '2025-09-04 14:23:57.361417', '2025-09-05 15:32:57.201198', '2025-09-04 14:23:57.361417');
INSERT INTO public."user" (user_id, "firstName", "lastName", email, email_verified, status, phone, phone_verified, address, dob, username, password, avatar, is_deleted, last_login_at, updated_at, created_at) VALUES ('user_customer_03', 'Cường', 'Phạm Văn', 'cuong.pham@customer.com', false, 'INACTIVE', '0905112233', false, '212 Trần Phú, District 5, HCMC', '2000-12-30', 'phamvancuong', '$2a$10$FpDqyefEzvm2pZurcgKLHOuQjZwTTl9G2CIEibiFstRGYbXcFd23y', NULL, false, NULL, '2025-09-05 15:32:57.285037', '2025-09-04 14:23:57.361417');
INSERT INTO public."user" (user_id, "firstName", "lastName", email, email_verified, status, phone, phone_verified, address, dob, username, password, avatar, is_deleted, last_login_at, updated_at, created_at) VALUES ('user_customer_04', 'Linh', 'Vũ Thị', 'linh.vu@customer.com', true, 'BANNED', '0966778899', true, '555 Cầu Giấy, Cầu Giấy, Hà Nội', '1999-07-07', 'vuthilinh', '$2a$10$RWfP6vbJO9QZ6J0FycWkv.G5MmqbTdueaF9al8439w8Yza56rhEoy', 'avatars/linh.png', false, '2025-09-04 14:23:57.361417', '2025-09-05 15:32:57.361721', '2025-09-04 14:23:57.361417');
INSERT INTO public."user" (user_id, "firstName", "lastName", email, email_verified, status, phone, phone_verified, address, dob, username, password, avatar, is_deleted, last_login_at, updated_at, created_at) VALUES ('68baa6a84867d302c571ee24', 'Do', 'Anh', 'blandboy2023@gmail.com', false, 'ACTIVE', '0369375404', false, 'Minh Khai bro', NULL, 'hhhh', '$2a$10$A0.eW9Y8m27xNvRw1SXD6eHubXG6wdFwZ8PmvVGOqacmzpTmXiw1G', NULL, false, NULL, '2025-09-05 16:00:24.427622', '2025-09-05 16:00:24.427622');


--
-- TOC entry 5073 (class 0 OID 33475)
-- Dependencies: 220
-- Data for Name: user_has_role; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.user_has_role (user_id, role_name, updated_at, created_at) VALUES ('user_admin_01', 'ADMIN', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.user_has_role (user_id, role_name, updated_at, created_at) VALUES ('user_shop_owner_01', 'SHOP_OWNER', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.user_has_role (user_id, role_name, updated_at, created_at) VALUES ('user_shop_owner_01', 'CUSTOMER', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.user_has_role (user_id, role_name, updated_at, created_at) VALUES ('user_shop_owner_02', 'SHOP_OWNER', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.user_has_role (user_id, role_name, updated_at, created_at) VALUES ('user_shop_owner_02', 'CUSTOMER', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.user_has_role (user_id, role_name, updated_at, created_at) VALUES ('user_customer_01', 'CUSTOMER', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.user_has_role (user_id, role_name, updated_at, created_at) VALUES ('user_customer_02', 'CUSTOMER', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.user_has_role (user_id, role_name, updated_at, created_at) VALUES ('user_customer_03', 'CUSTOMER', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');
INSERT INTO public.user_has_role (user_id, role_name, updated_at, created_at) VALUES ('user_customer_04', 'CUSTOMER', '2025-09-04 14:23:57.361417', '2025-09-04 14:23:57.361417');


--
-- TOC entry 5091 (class 0 OID 33615)
-- Dependencies: 238
-- Data for Name: wallet_transaction; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.wallet_transaction (transaction_id, wallet_id, transaction_code, transaction_type, amount, balance_before, balance_after, fee, status, reference_type, reference_id, description, created_at, updated_at) VALUES ('trans_book01_p1', 'wallet_shop01', 'TRN001', 'DEPOSIT', 85000.00, 500000.00, 580750.00, 4250.00, 'COMPLETED', 'booking_item', 'bitem_01', 'Thanh toán cho đơn hàng book_01', '2024-08-11 14:01:00', '2024-08-11 14:01:00');
INSERT INTO public.wallet_transaction (transaction_id, wallet_id, transaction_code, transaction_type, amount, balance_before, balance_after, fee, status, reference_type, reference_id, description, created_at, updated_at) VALUES ('trans_book01_p2', 'wallet_shop01', 'TRN002', 'DEPOSIT', 55000.00, 580750.00, 633000.00, 2750.00, 'COMPLETED', 'booking_item', 'bitem_02', 'Thanh toán cho đơn hàng book_01', '2024-08-11 14:01:00', '2024-08-11 14:01:00');


-- Completed on 2025-09-09 16:09:57

--
-- PostgreSQL database dump complete
--

\unrestrict QxSNs6Y7gg0WYdUQZWJ7XdTVtBN32O4LtJu91X0hzc6frcJXvlQatpAR9BXcs0I

