-- Lấy ra tổng số hàng hóa theo status với những hàng hóa có quantity > 50 và sort giảm dần

select p.product_status, SUM(p.quantity) as "Tong"
from product p
where p.quantity > 50
group by p.product_status
order by sum(p.quantity) desc;

select *
from users;
select *
from shop;
select *
from product
         left join shop on product.shop_id = shop.shop_id;

select p.product_id, (select avg(p.quantity) from product p)
from product p;

-- select pro id, shop id với các shop còn hoạt động
select p.product_id, p.shop_id
from product p
where p.shop_id in (select s.shop_id from shop s where s.shop_status like 'ACTIVE');


--c2
-- Bật thống kê truy cập bảng
ALTER SYSTEM SET track_io_timing = on;
SELECT pg_reload_conf();

with shop_active as (select s.shop_id
                     from shop s
                     where s.shop_status like 'ACTIVE')
select p.product_id, p.shop_id
from product p
where p.shop_id in (select shop_id from shop_active);
--
select p.product_id, p.shop_id
from product p
where exists (select s.shop_id, s.updated_at
              from shop s
              where s.shop_status like 'ACTIVE'
                and s.updated_at = p.updated_at);

-- select địa chỉ kiểu 12... của tất cả user còn ACTIVE
create view all_customer_active_login as
select u.user_id, u.username, u.address
from users u
where u.status = 'ACTIVE';

select u.address
from all_customer_active_login u
where u.address like '12%';

select p.product_id,
       p.quantity,
       ROW_NUMBER() OVER (ORDER BY p.quantity DESC) AS Top
from product p;

select p.product_id, p.quantity, RANK() OVER (ORDER BY p.quantity DESC) AS TOP
from product p;

select p.product_id, p.quantity, DENSE_RANK() OVER (ORDER BY p.quantity DESC) AS TOP
from product p;




