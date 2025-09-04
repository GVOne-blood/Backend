CREATE INDEX soluong_dongia ON product (quantity, price);
SET enable_seqscan = off;
--select * from product;
select *
from product p
where p.quantity > 100
  and p.price > 80000