create table orders
(
    order_id                 uuid not null
        primary key,
    user_id                  uuid,
    shop_id                  uuid,
    shipper_id               uuid,
    payment_transaction_id   uuid,
    order_status             varchar(255),
    payment_status           varchar(50),
    shipping_address_street  varchar(255),
    shipping_address_ward    varchar(255),
    shipping_address_city    varchar(255),
    shipping_address_details varchar(255),
    subtotal_amount          numeric(38, 2),
    shipping_fee             numeric(38, 2),
    discount_amount          numeric(38, 2),
    final_price              numeric(38, 2),
    payment_method_name      varchar(255),
    customer_notes           text,
    delivered_at             timestamp with time zone,
    created_at               timestamp with time zone,
    updated_at               timestamp with time zone,
    paid_at                  timestamp
);

alter table orders
    owner to neondb_owner;

