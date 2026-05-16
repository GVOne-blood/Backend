create table products
(
    product_id      uuid not null
        primary key,
    created_at      timestamp,
    updated_at      timestamp,
    avg_rate        numeric(38, 2),
    description     varchar(255),
    exp             date,
    images          jsonb,
    msg             date,
    name            varchar(255),
    price           numeric(15, 2),
    product_status  varchar(255),
    quantity        integer,
    shop_id         uuid,
    sku             varchar(255),
    wholesale_price numeric(38, 2),
    total_feedbacks bigint,
    average_rating  double precision
);

alter table products
    owner to neondb_owner;

