create table order_items
(
    order_item_id     uuid not null
        primary key,
    order_id          uuid,
    product_id        uuid,
    product_name      varchar(255),
    product_image_url varchar(255),
    quantity          integer,
    price_at_booking  numeric(38, 2),
    created_at        timestamp with time zone,
    updated_at        timestamp with time zone
);

alter table order_items
    owner to neondb_owner;

