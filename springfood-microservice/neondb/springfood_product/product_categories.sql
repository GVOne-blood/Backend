create table product_categories
(
    category_name varchar(255)
        constraint fk9626de2xg5378wqvmljk7rj8j
            references categories,
    product_id    uuid
        constraint fklda9rad6s180ha3dl1ncsp8n7
            references products
);

alter table product_categories
    owner to neondb_owner;

