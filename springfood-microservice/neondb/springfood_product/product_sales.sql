create table product_sales
(
    product_id uuid
        constraint fk2t8pfarluqlwau6jqljopaafb
            references products,
    sale_id    uuid
        constraint fk3p2d8vqt6i8ostbngftdi867p
            references sales
);

alter table product_sales
    owner to neondb_owner;

