create table user_wishlist
(
    wishlist_id uuid not null
        primary key,
    created_at  timestamp(6) with time zone,
    note        varchar(500),
    product_id  uuid not null,
    user_id     uuid not null,
    variant_id  uuid,
    constraint uk_user_product
        unique (user_id, product_id)
);

alter table user_wishlist
    owner to neondb_owner;

