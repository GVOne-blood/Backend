create table product_variants
(
    id           uuid         not null
        primary key,
    created_at   timestamp(6),
    updated_at   timestamp(6),
    attributes   jsonb,
    image_url    varchar(255),
    is_available boolean,
    price        numeric(15, 2),
    product_id   varchar(255) not null,
    sku          varchar(255) not null
        constraint ukq935p2d1pbjm39n0063ghnfgn
            unique,
    stock        integer      not null,
    variant_name varchar(255)
);

alter table product_variants
    owner to neondb_owner;

