--
-- PostgreSQL database dump
--

-- Dumped from database version 17.5
-- Dumped by pg_dump version 17.5

-- Started on 2025-08-13 15:30:00

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
-- TOC entry 5001 (class 1262 OID 27852)
-- Name: springfood; Type: DATABASE; Schema: -; Owner: postgres
--
DROP DATABASE IF EXISTS springfood;
CREATE DATABASE springfood WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc;


ALTER DATABASE springfood OWNER TO postgres;

\connect springfood

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
-- TOC entry 4 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: pg_database_owner
--

CREATE SCHEMA public;


ALTER SCHEMA public OWNER TO pg_database_owner;

--
-- TOC entry 5002 (class 0 OID 0)
-- Dependencies: 4
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: pg_database_owner
--

COMMENT ON SCHEMA public IS 'standard public schema';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 234 (class 1259 OID 28932)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.users (
    dob date,
    is_deleted boolean NOT NULL,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    phone character varying(15),
    address character varying(255),
    avartar character varying(255),
    email character varying(255) NOT NULL,
    first_name character varying(255),
    last_name character varying(255),
    password character varying(255) NOT NULL,
    status character varying(255),
    user_id character varying(255) NOT NULL,
    username character varying(255) NOT NULL,
    CONSTRAINT users_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'BANNED'::character varying])::text[])))
);
ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 28872)
-- Name: role; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.role (
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    description character varying(255) NOT NULL,
    role_name character varying(255) NOT NULL
);
ALTER TABLE public.role OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 28925)
-- Name: user_has_role; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.user_has_role (
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    role_name character varying(255) NOT NULL,
    user_id character varying(255) NOT NULL
);
ALTER TABLE public.user_has_role OWNER TO postgres;


--
-- TOC entry 230 (class 1259 OID 28900)
-- Name: shop; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.shop (
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    introduction character varying(255),
    logo character varying(255),
    owner_id character varying(255) NOT NULL,
    shop_id character varying(255) NOT NULL,
    shop_name character varying(255) NOT NULL,
    shop_status character varying(255),
    CONSTRAINT shop_shop_status_check CHECK (((shop_status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'PENDING'::character varying, 'BANNED'::character varying])::text[])))
);
ALTER TABLE public.shop OWNER TO postgres;


--
-- TOC entry 223 (class 1259 OID 28850)
-- Name: product; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.product (
    price numeric(15,2) NOT NULL,
    quantity integer NOT NULL,
    created_at timestamp(6) without time zone,
    exp date,
    msg date,
    updated_at timestamp(6) without time zone,
    description character varying(255),
    images character varying(255),
    name character varying(255) NOT NULL,
    product_id character varying(255) NOT NULL,
    product_status character varying(255),
    shop_id character varying(255) NOT NULL,
    CONSTRAINT product_product_status_check CHECK (((product_status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'OUT_OF_STOCK'::character varying, 'DISCONTINUED'::character varying])::text[])))
);
ALTER TABLE public.product OWNER TO postgres;


--
-- TOC entry 220 (class 1259 OID 28828)
-- Name: payment; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.payment (
    is_active boolean,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    description character varying(255),
    payment_name character varying(255) NOT NULL
);
ALTER TABLE public.payment OWNER TO postgres;


--
-- TOC entry 229 (class 1259 OID 28893)
-- Name: shipping; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.shipping (
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    address character varying(255),
    city character varying(255),
    shipping_id character varying(255) NOT NULL,
    street character varying(255),
    user_id character varying(255),
    ward character varying(255)
);
ALTER TABLE public.shipping OWNER TO postgres;


--
-- TOC entry 217 (class 1259 OID 28804)
-- Name: booking; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.booking (
    total_price numeric(15,2) NOT NULL,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    booking_id character varying(255) NOT NULL,
    booking_status character varying(255),
    customer_notes text,
    payment_method_name character varying(255),
    shipping_address character varying(255),
    shipping_id character varying(255),
    user_id character varying(255) NOT NULL,
    CONSTRAINT booking_booking_status_check CHECK (((booking_status)::text = ANY ((ARRAY['PENDING'::character varying, 'CONFIRMED'::character varying, 'CANCELLED'::character varying, 'COMPLETED'::character varying, 'NO_SHOW'::character varying])::text[])))
);
ALTER TABLE public.booking OWNER TO postgres;


--
-- TOC entry 218 (class 1259 OID 28814)
-- Name: booking_item; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.booking_item (
    price_at_booking numeric(15,2) NOT NULL,
    quantity integer NOT NULL,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    bill_id character varying(255) NOT NULL,
    booking_id character varying(255) NOT NULL,
    product_id character varying(255) NOT NULL
);
ALTER TABLE public.booking_item OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 28821)
-- Name: feedback; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.feedback (
    rating integer,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    content character varying(255),
    feedback_id character varying(255) NOT NULL,
    product_id character varying(255) NOT NULL,
    user_id character varying(255) NOT NULL
);
ALTER TABLE public.feedback OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 28835)
-- Name: permission; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.permission (
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    description character varying(255) NOT NULL,
    permission_name character varying(255) NOT NULL
);
ALTER TABLE public.permission OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 28842)
-- Name: post; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.post (
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    body text,
    post_id character varying(255) NOT NULL,
    post_status character varying(255),
    title character varying(255),
    user_id character varying(255),
    CONSTRAINT post_post_status_check CHECK (((post_status)::text = ANY ((ARRAY['PUBLIC'::character varying, 'PRIVATE'::character varying, 'BANNED'::character varying])::text[])))
);
ALTER TABLE public.post OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 28858)
-- Name: product_sale; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.product_sale (
    product_id character varying(255) NOT NULL,
    sale_id character varying(255) NOT NULL
);
ALTER TABLE public.product_sale OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 28865)
-- Name: product_tag; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.product_tag (
    product_id character varying(255) NOT NULL,
    tag_name character varying(255) NOT NULL
);
ALTER TABLE public.product_tag OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 28879)
-- Name: role_has_permission; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.role_has_permission (
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    permission_name character varying(255) NOT NULL,
    role_name character varying(255) NOT NULL
);
ALTER TABLE public.role_has_permission OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 28886)
-- Name: sale; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.sale (
    discount_amount numeric(15,2),
    discount_percentage numeric(5,2),
    created_at timestamp(6) without time zone,
    end_date timestamp(6) without time zone,
    start_date timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    conditions character varying(255),
    sale_id character varying(255) NOT NULL,
    title character varying(255) NOT NULL
);
ALTER TABLE public.sale OWNER TO postgres;

--
-- TOC entry 231 (class 1259 OID 28910)
-- Name: tag; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.tag (
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    tag_description character varying(255),
    tag_name character varying(255) NOT NULL
);
ALTER TABLE public.tag OWNER TO postgres;

--
-- TOC entry 232 (class 1259 OID 28917)
-- Name: token; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.token (
    token_type smallint,
    token character varying(255) NOT NULL,
    user_id character varying(255),
    CONSTRAINT token_token_type_check CHECK (((token_type >= 0) AND (token_type <= 2)))
);
ALTER TABLE public.token OWNER TO postgres;


---
--- DATA INSERTION
---

-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
INSERT INTO public.users (user_id, username, first_name, last_name, email, phone, address, password, status, is_deleted, created_at, updated_at) VALUES
('689c30502395733c5395ed94', 'dta_admin', 'Anh', 'Đỗ Tuấn', 'dta150904@gmail.com', '0369375404', '123 Minh Khai, Hai Bà Trưng, Hà Nội', '$2a$10$IcnKn7rsLq48rq3Hi5Rms.9l2xCgvv01DluFrtYzeu.9XCJ23v.Zi', 'ACTIVE', false, NOW(), NOW()),
('689c3cf3d8c15b6326a3eb52', 'nguyenvana', 'A', 'Nguyễn Văn', 'nguyenvana@example.com', '0397130501', '456 Lê Lợi, Quận 1, TP. HCM', '$2a$10$Oq8vZSRh28f31GMgK7A0suVKytFtXIHRxbHSAgaMaMdy/ToTxH2Ae', 'ACTIVE', false, NOW(), NOW()),
('8a4fde3b8e1f5b6a7c3d2e1f', 'tran_shop', 'Bình', 'Trần Văn', 'tranvanbinh.shop@example.com', '0987654321', '789 Hùng Vương, Hải Châu, Đà Nẵng', '$2a$10$abcdef1234567890abcdefu.gHijklmnoPqrstuvWxyz123456', 'ACTIVE', false, NOW(), NOW()),
('9b5cde4c8f2g6c7d8e4f3e2g', 'le_thi_b', 'B', 'Lê Thị', 'lethib@example.com', '0912345678', '101 Nguyễn Trãi, Thanh Xuân, Hà Nội', '$2a$10$ghijk9876543210ghijku.lMnopqRstuvwxyzABCDEFGHIJ', 'ACTIVE', false, NOW(), NOW()),
('1c6ade5d9g3h7d8e9f5g4e3h', 'pham_van_c', 'C', 'Phạm Văn', 'phamvanc@example.com', '0905112233', '212 Trần Phú, Quận 5, TP. HCM', '$2a$10$mnopqr6543210987mnopqu.vWxyzabcdefGHIJKLMNOPQ', 'INACTIVE', false, NOW(), NOW()),
('2d7bef6e1h4i8e9f1g6h5f4i', 'hoang_shop', 'Dũng', 'Hoàng Trí', 'hoangtridung.shop@example.com', '0978123456', '333 Võ Văn Tần, Quận 3, TP. HCM', '$2a$10$rstuvw3210987654rstuvu.xYzabcdefghijklmnopqrS', 'ACTIVE', false, NOW(), NOW()),
('3e8cfg7f2i5j9f1g2h7i6g5j', 'vu_thi_d', 'D', 'Vũ Thị', 'vuthid@example.com', '0966778899', '555 Cầu Giấy, Cầu Giấy, Hà Nội', '$2a$10$yzabcd0987654321yzabcu.dEfghijklmnopqrstuvwXYZ', 'ACTIVE', false, NOW(), NOW()),
('4f9dgh8g3j6k1g2h3i8j7h6k', 'dang_van_e', 'E', 'Đặng Văn', 'dangvane@example.com', '0888999000', '1 Cù Lao, Phú Nhuận, TP.HCM', '$2a$10$bcdefg7654321098bcdefu.hIjklmnopqrstuvwxyzABCD', 'BANNED', false, NOW(), NOW()),
('5g1ehj9h4k7l2h3i4j9k8i7l', 'ngo_thi_f', 'F', 'Ngô Thị', 'ngothif@example.com', '0333444555', '88 Lý Thường Kiệt, Hoàn Kiếm, Hà Nội', '$2a$10$fghijk4321098765fghiju.kLmnopqrstuvwxyzABCDEFG', 'ACTIVE', false, NOW(), NOW()),
('6h2fik1i5l8m3i4j5k1l9j8m', 'doan_van_g', 'G', 'Đoàn Văn', 'doanvang@example.com', '0777888999', '234 Pasteur, Quận 3, TP. HCM', '$2a$10$ijklmn1098765432ijklmu.nOpqrstuvwxyzABCDEFGHI', 'ACTIVE', false, NOW(), NOW());


-- Data for Name: role; Type: TABLE DATA; Schema: public; Owner: postgres
INSERT INTO public.role (role_name, description, created_at, updated_at) VALUES
('ADMIN', 'Quản trị viên hệ thống, có toàn quyền', NOW(), NOW()),
('SHOP_OWNER', 'Chủ cửa hàng, quản lý sản phẩm và đơn hàng của shop', NOW(), NOW()),
('USER', 'Người dùng thông thường, mua hàng', NOW(), NOW());


-- Data for Name: user_has_role; Type: TABLE DATA; Schema: public; Owner: postgres
INSERT INTO public.user_has_role (user_id, role_name, created_at, updated_at) VALUES
('689c30502395733c5395ed94', 'ADMIN', NOW(), NOW()),
('689c30502395733c5395ed94', 'USER', NOW(), NOW()),
('689c3cf3d8c15b6326a3eb52', 'USER', NOW(), NOW()),
('8a4fde3b8e1f5b6a7c3d2e1f', 'SHOP_OWNER', NOW(), NOW()),
('8a4fde3b8e1f5b6a7c3d2e1f', 'USER', NOW(), NOW()),
('9b5cde4c8f2g6c7d8e4f3e2g', 'USER', NOW(), NOW()),
('1c6ade5d9g3h7d8e9f5g4e3h', 'USER', NOW(), NOW()),
('2d7bef6e1h4i8e9f1g6h5f4i', 'SHOP_OWNER', NOW(), NOW()),
('2d7bef6e1h4i8e9f1g6h5f4i', 'USER', NOW(), NOW()),
('3e8cfg7f2i5j9f1g2h7i6g5j', 'USER', NOW(), NOW()),
('4f9dgh8g3j6k1g2h3i8j7h6k', 'USER', NOW(), NOW()),
('5g1ehj9h4k7l2h3i4j9k8i7l', 'USER', NOW(), NOW()),
('6h2fik1i5l8m3i4j5k1l9j8m', 'USER', NOW(), NOW());

-- Data for Name: shop; Type: TABLE DATA; Schema: public; Owner: postgres
INSERT INTO public.shop (shop_id, shop_name, introduction, logo, owner_id, shop_status, created_at, updated_at) VALUES
('shop_01', 'Bếp Nhà Bình An', 'Cung cấp thực phẩm sạch, an toàn cho mọi gia đình.', 'logo_binhan.png', '8a4fde3b8e1f5b6a7c3d2e1f', 'ACTIVE', NOW(), NOW()),
('shop_02', 'Vườn Rau Sạch Dũng Hoàng', 'Rau củ quả hữu cơ trồng tại Đà Lạt.', 'logo_dunghoang.png', '2d7bef6e1h4i8e9f1g6h5f4i', 'ACTIVE', NOW(), NOW()),
('shop_03', 'Hải Sản Tươi Sống Phan Thiết', 'Giao hàng tận nơi trong ngày.', 'logo_haisan.png', '689c30502395733c5395ed94', 'PENDING', NOW(), NOW());

-- Data for Name: product; Type: TABLE DATA; Schema: public; Owner: postgres
INSERT INTO public.product (product_id, name, description, price, quantity, shop_id, product_status, created_at, updated_at) VALUES
('prod_01', 'Thịt Ba Rọi Heo', 'Thịt heo sạch, không chất tăng trọng.', 150000.00, 50, 'shop_01', 'AVAILABLE', NOW(), NOW()),
('prod_02', 'Ức Gà Phi Lê', 'Thích hợp cho người tập gym, ăn kiêng.', 85000.00, 100, 'shop_01', 'AVAILABLE', NOW(), NOW()),
('prod_03', 'Cá Diêu Hồng Tươi', 'Cá tươi sống mỗi ngày.', 70000.00, 30, 'shop_01', 'AVAILABLE', NOW(), NOW()),
('prod_04', 'Trứng Gà Ta', 'Vỉ 10 trứng gà ta thả vườn.', 35000.00, 200, 'shop_01', 'OUT_OF_STOCK', NOW(), NOW()),
('prod_05', 'Cải Bó Xôi Hữu Cơ', 'Trồng theo tiêu chuẩn VietGAP.', 25000.00, 80, 'shop_02', 'AVAILABLE', NOW(), NOW()),
('prod_06', 'Xà Lách Romana Đà Lạt', 'Giòn ngọt, thích hợp làm salad.', 20000.00, 120, 'shop_02', 'AVAILABLE', NOW(), NOW()),
('prod_07', 'Cà Chua Bi', 'Cà chua bi hữu cơ, vị ngọt thanh.', 40000.00, 60, 'shop_02', 'AVAILABLE', NOW(), NOW()),
('prod_08', 'Khoai Lang Mật', 'Khoai lang mật Tà Nung, dẻo ngọt.', 30000.00, 90, 'shop_02', 'AVAILABLE', NOW(), NOW()),
('prod_09', 'Tôm Sú Biển', 'Tôm sú thiên nhiên, size lớn.', 450000.00, 20, 'shop_01', 'AVAILABLE', NOW(), NOW()),
('prod_10', 'Bí Đỏ Hồ Lô', 'Bí đỏ hồ lô dẻo, thơm.', 15000.00, 70, 'shop_02', 'AVAILABLE', NOW(), NOW());

-- Data for Name: payment; Type: TABLE DATA; Schema: public; Owner: postgres
INSERT INTO public.payment (payment_name, description, is_active, created_at, updated_at) VALUES
('COD', 'Thanh toán khi nhận hàng (Cash On Delivery)', true, NOW(), NOW()),
('MOMO', 'Thanh toán qua ví điện tử MoMo', true, NOW(), NOW()),
('VNPAY', 'Thanh toán qua cổng VNPAY (Thẻ ATM, QR Code)', true, NOW(), NOW()),
('ZALOPAY', 'Thanh toán qua ví điện tử ZaloPay', false, NOW(), NOW());

-- Data for Name: shipping; Type: TABLE DATA; Schema: public; Owner: postgres
INSERT INTO public.shipping (shipping_id, user_id, address, ward, city, street, created_at, updated_at) VALUES
('ship_01', '689c3cf3d8c15b6326a3eb52', '456 Lê Lợi, Quận 1, TP. HCM', 'Phường Bến Nghé', 'TP. Hồ Chí Minh', 'Lê Lợi', NOW(), NOW()),
('ship_02', '9b5cde4c8f2g6c7d8e4f3e2g', '101 Nguyễn Trãi, Thanh Xuân, Hà Nội', 'Phường Thượng Đình', 'Hà Nội', 'Nguyễn Trãi', NOW(), NOW()),
('ship_03', '3e8cfg7f2i5j9f1g2h7i6g5j', '555 Cầu Giấy, Cầu Giấy, Hà Nội', 'Phường Dịch Vọng', 'Hà Nội', 'Cầu Giấy', NOW(), NOW()),
('ship_04', '5g1ehj9h4k7l2h3i4j9k8i7l', '88 Lý Thường Kiệt, Hoàn Kiếm, Hà Nội', 'Phường Cửa Nam', 'Hà Nội', 'Lý Thường Kiệt', NOW(), NOW());

-- Data for Name: booking; Type: TABLE DATA; Schema: public; Owner: postgres
INSERT INTO public.booking (booking_id, user_id, shipping_id, total_price, payment_method_name, booking_status, customer_notes, created_at, updated_at) VALUES
('book_01', '689c3cf3d8c15b6326a3eb52', 'ship_01', 235000.00, 'COD', 'COMPLETED', 'Giao hàng trong giờ hành chính.', '2025-08-10 09:30:00', '2025-08-11 14:00:00'),
('book_02', '9b5cde4c8f2g6c7d8e4f3e2g', 'ship_02', 45000.00, 'MOMO', 'CONFIRMED', 'Vui lòng gọi trước khi giao.', '2025-08-11 11:00:00', '2025-08-11 11:05:00'),
('book_03', '3e8cfg7f2i5j9f1g2h7i6g5j', 'ship_03', 70000.00, 'VNPAY', 'PENDING', NULL, '2025-08-12 15:00:00', '2025-08-12 15:00:00'),
('book_04', '689c3cf3d8c15b6326a3eb52', NULL, 450000.00, 'COD', 'CANCELLED', 'Khách bom hàng.', '2025-08-09 10:00:00', '2025-08-09 18:00:00');

-- Data for Name: booking_item; Type: TABLE DATA; Schema: public; Owner: postgres
INSERT INTO public.booking_item (bill_id, booking_id, product_id, quantity, price_at_booking, created_at, updated_at) VALUES
('bitem_01', 'book_01', 'prod_01', 1, 150000.00, NOW(), NOW()),
('bitem_02', 'book_01', 'prod_02', 1, 85000.00, NOW(), NOW()),
('bitem_03', 'book_02', 'prod_05', 1, 25000.00, NOW(), NOW()),
('bitem_04', 'book_02', 'prod_06', 1, 20000.00, NOW(), NOW()),
('bitem_05', 'book_03', 'prod_07', 1, 40000.00, NOW(), NOW()),
('bitem_06', 'book_03', 'prod_08', 1, 30000.00, NOW(), NOW()),
('bitem_07', 'book_04', 'prod_09', 1, 450000.00, NOW(), NOW());

-- Data for Name: feedback; Type: TABLE DATA; Schema: public; Owner: postgres
INSERT INTO public.feedback (feedback_id, user_id, product_id, rating, content, created_at, updated_at) VALUES
('feed_01', '689c3cf3d8c15b6326a3eb52', 'prod_01', 5, 'Thịt rất tươi và ngon, đóng gói cẩn thận. Sẽ ủng hộ shop tiếp!', NOW(), NOW()),
('feed_02', '689c3cf3d8c15b6326a3eb52', 'prod_02', 5, 'Ức gà chắc thịt, không bị bở. Giao hàng nhanh.', NOW(), NOW()),
('feed_03', '9b5cde4c8f2g6c7d8e4f3e2g', 'prod_05', 4, 'Rau tươi, nhưng hơi ít so với giá.', NOW(), NOW());

-- Data for Name: tag; Type: TABLE DATA; Schema: public; Owner: postgres
INSERT INTO public.tag (tag_name, tag_description, created_at, updated_at) VALUES
('Thịt Heo', 'Các sản phẩm từ thịt heo', NOW(), NOW()),
('Thịt Gà', 'Các sản phẩm từ thịt gà', NOW(), NOW()),
('Hải Sản', 'Các loại hải sản tươi sống và đông lạnh', NOW(), NOW()),
('Rau Củ Hữu Cơ', 'Rau củ được trồng theo phương pháp hữu cơ', NOW(), NOW()),
('Trái Cây', 'Các loại trái cây theo mùa', NOW(), NOW());


-- Data for Name: product_tag; Type: TABLE DATA; Schema: public; Owner: postgres
INSERT INTO public.product_tag (product_id, tag_name) VALUES
('prod_01', 'Thịt Heo'),
('prod_02', 'Thịt Gà'),
('prod_03', 'Hải Sản'),
('prod_05', 'Rau Củ Hữu Cơ'),
('prod_06', 'Rau Củ Hữu Cơ'),
('prod_07', 'Rau Củ Hữu Cơ'),
('prod_07', 'Trái Cây'),
('prod_08', 'Rau Củ Hữu Cơ'),
('prod_09', 'Hải Sản');


---
--- CONSTRAINTS
---

--
-- TOC entry 4773 (class 2606 OID 28820)
-- Name: booking_item booking_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.booking_item
    ADD CONSTRAINT booking_item_pkey PRIMARY KEY (bill_id);


--
-- TOC entry 4769 (class 2606 OID 28811)
-- Name: booking booking_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.booking
    ADD CONSTRAINT booking_pkey PRIMARY KEY (booking_id);


--
-- TOC entry 4771 (class 2606 OID 28813)
-- Name: booking booking_shipping_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.booking
    ADD CONSTRAINT booking_shipping_id_key UNIQUE (shipping_id);


--
-- TOC entry 4775 (class 2606 OID 28827)
-- Name: feedback feedback_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feedback
    ADD CONSTRAINT feedback_pkey PRIMARY KEY (feedback_id);


--
-- TOC entry 4777 (class 2606 OID 28834)
-- Name: payment payment_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payment
    ADD CONSTRAINT payment_pkey PRIMARY KEY (payment_name);


--
-- TOC entry 4779 (class 2606 OID 28841)
-- Name: permission permission_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission
    ADD CONSTRAINT permission_pkey PRIMARY KEY (permission_name);


--
-- TOC entry 4781 (class 2606 OID 28849)
-- Name: post post_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post
    ADD CONSTRAINT post_pkey PRIMARY KEY (post_id);


--
-- TOC entry 4783 (class 2606 OID 28857)
-- Name: product product_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT product_pkey PRIMARY KEY (product_id);


--
-- TOC entry 4785 (class 2606 OID 28864)
-- Name: product_sale product_sale_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_sale
    ADD CONSTRAINT product_sale_pkey PRIMARY KEY (product_id, sale_id);


--
-- TOC entry 4787 (class 2606 OID 28871)
-- Name: product_tag product_tag_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_tag
    ADD CONSTRAINT product_tag_pkey PRIMARY KEY (product_id, tag_name);


--
-- TOC entry 4791 (class 2606 OID 28885)
-- Name: role_has_permission role_has_permission_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_has_permission
    ADD CONSTRAINT role_has_permission_pkey PRIMARY KEY (permission_name, role_name);


--
-- TOC entry 4789 (class 2606 OID 28878)
-- Name: role role_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (role_name);


--
-- TOC entry 4793 (class 2606 OID 28892)
-- Name: sale sale_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sale
    ADD CONSTRAINT sale_pkey PRIMARY KEY (sale_id);


--
-- TOC entry 4795 (class 2606 OID 28899)
-- Name: shipping shipping_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.shipping
    ADD CONSTRAINT shipping_pkey PRIMARY KEY (shipping_id);


--
-- TOC entry 4797 (class 2606 OID 28909)
-- Name: shop shop_owner_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.shop
    ADD CONSTRAINT shop_owner_id_key UNIQUE (owner_id);


--
-- TOC entry 4799 (class 2606 OID 28907)
-- Name: shop shop_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.shop
    ADD CONSTRAINT shop_pkey PRIMARY KEY (shop_id);


--
-- TOC entry 4801 (class 2606 OID 28916)
-- Name: tag tag_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tag
    ADD CONSTRAINT tag_pkey PRIMARY KEY (tag_name);


--
-- TOC entry 4803 (class 2606 OID 28924)
-- Name: token token_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.token
    ADD CONSTRAINT token_pkey PRIMARY KEY (token);


--
-- TOC entry 4805 (class 2606 OID 28931)
-- Name: user_has_role user_has_role_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_has_role
    ADD CONSTRAINT user_has_role_pkey PRIMARY KEY (role_name, user_id);


--
-- TOC entry 4807 (class 2606 OID 28943)
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- TOC entry 4809 (class 2606 OID 28941)
-- Name: users users_phone_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_phone_key UNIQUE (phone);


--
-- TOC entry 4811 (class 2606 OID 28939)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- TOC entry 4813 (class 2606 OID 28945)
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- TOC entry 4814 (class 2606 OID 28946)
-- Name: booking fk12g0lkc7s64g1dq18eed0vudb; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.booking
    ADD CONSTRAINT fk12g0lkc7s64g1dq18eed0vudb FOREIGN KEY (payment_method_name) REFERENCES public.payment(payment_name);


--
-- TOC entry 4831 (class 2606 OID 29036)
-- Name: user_has_role fk2dl1ftxlkldulcp934i3125qo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_has_role
    ADD CONSTRAINT fk2dl1ftxlkldulcp934i3125qo FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 4825 (class 2606 OID 29001)
-- Name: product_tag fk2rf7w3d88x20p7vuc2m9mvv91; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_tag
    ADD CONSTRAINT fk2rf7w3d88x20p7vuc2m9mvv91 FOREIGN KEY (product_id) REFERENCES public.product(product_id);


--
-- TOC entry 4823 (class 2606 OID 28996)
-- Name: product_sale fk4pvqe8s1fkn1acdx7ogqw9ofm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_sale
    ADD CONSTRAINT fk4pvqe8s1fkn1acdx7ogqw9ofm FOREIGN KEY (sale_id) REFERENCES public.sale(sale_id);


--
-- TOC entry 4817 (class 2606 OID 28966)
-- Name: booking_item fk5gxsb7a18lt2lhaswkxp3yd7n; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.booking_item
    ADD CONSTRAINT fk5gxsb7a18lt2lhaswkxp3yd7n FOREIGN KEY (product_id) REFERENCES public.product(product_id);


--
-- TOC entry 4821 (class 2606 OID 28981)
-- Name: post fk7ky67sgi7k0ayf22652f7763r; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post
    ADD CONSTRAINT fk7ky67sgi7k0ayf22652f7763r FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 4815 (class 2606 OID 28956)
-- Name: booking fk7udbel7q86k041591kj6lfmvw; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.booking
    ADD CONSTRAINT fk7udbel7q86k041591kj6lfmvw FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 4822 (class 2606 OID 28986)
-- Name: product fk94hgg8hlqfqfnt3dag950vm7n; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT fk94hgg8hlqfqfnt3dag950vm7n FOREIGN KEY (shop_id) REFERENCES public.shop(shop_id);


--
-- TOC entry 4827 (class 2606 OID 29016)
-- Name: role_has_permission fkc9qsyy8y1qdfv1tnenrdm2w5u; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_has_permission
    ADD CONSTRAINT fkc9qsyy8y1qdfv1tnenrdm2w5u FOREIGN KEY (role_name) REFERENCES public.role(role_name);


--
-- TOC entry 4832 (class 2606 OID 29031)
-- Name: user_has_role fkdr3ewminb39x8pd3hktnpadi1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_has_role
    ADD CONSTRAINT fkdr3ewminb39x8pd3hktnpadi1 FOREIGN KEY (role_name) REFERENCES public.role(role_name);


--
-- TOC entry 4830 (class 2606 OID 29026)
-- Name: shop fkea1di7i3b50tpkwrfkincd34g; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.shop
    ADD CONSTRAINT fkea1di7i3b50tpkwrfkincd34g FOREIGN KEY (owner_id) REFERENCES public.users(user_id);


--
-- TOC entry 4816 (class 2606 OID 28951)
-- Name: booking fkgfq9l93jtwras5h6iatn7lt3q; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.booking
    ADD CONSTRAINT fkgfq9l93jtwras5h6iatn7lt3q FOREIGN KEY (shipping_id) REFERENCES public.shipping(shipping_id);


--
-- TOC entry 4819 (class 2606 OID 28971)
-- Name: feedback fklsfunb44jdljfmbx4un8s4waa; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feedback
    ADD CONSTRAINT fklsfunb44jdljfmbx4un8s4waa FOREIGN KEY (product_id) REFERENCES public.product(product_id);


--
-- TOC entry 4826 (class 2606 OID 29006)
-- Name: product_tag fkmkwenho1ceh0xlwoq9e5xdmhe; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_tag
    ADD CONSTRAINT fkmkwenho1ceh0xlwoq9e5xdmhe FOREIGN KEY (tag_name) REFERENCES public.tag(tag_name);


--
-- TOC entry 4824 (class 2606 OID 28991)
-- Name: product_sale fkn4mby2cj0njhd6s7rbqbf5ujh; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_sale
    ADD CONSTRAINT fkn4mby2cj0njhd6s7rbqbf5ujh FOREIGN KEY (product_id) REFERENCES public.product(product_id);


--
-- TOC entry 4828 (class 2606 OID 29011)
-- Name: role_has_permission fkoqxo0nij3sjiqewl79gn8t4yk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_has_permission
    ADD CONSTRAINT fkoqxo0nij3sjiqewl79gn8t4yk FOREIGN KEY (permission_name) REFERENCES public.permission(permission_name);


--
-- TOC entry 4820 (class 2606 OID 28976)
-- Name: feedback fkpwwmhguqianghvi1wohmtsm8l; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feedback
    ADD CONSTRAINT fkpwwmhguqianghvi1wohmtsm8l FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 4829 (class 2606 OID 29021)
-- Name: shipping fkr0b3ts5206046qpdq9n65rg5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.shipping
    ADD CONSTRAINT fkr0b3ts5206046qpdq9n65rg5 FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 4818 (class 2606 OID 28961)
-- Name: booking_item fktbqq9ms8palbasvo7ydxgrrpx; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.booking_item
    ADD CONSTRAINT fktbqq9ms8palbasvo7ydxgrrpx FOREIGN KEY (booking_id) REFERENCES public.booking(booking_id);


-- Completed on 2025-08-13 15:30:00

--
-- PostgreSQL database dump complete
--