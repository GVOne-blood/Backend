CREATE INDEX soluong_dongia ON product (quantity, price);

create index pro on product (price);
SET enable_seqscan = off;
select *
from product
where price > 70000;

SET enable_seqscan = off;
--select * from product;
select *
from product p
where p.quantity > 100
  and p.price > 80000

