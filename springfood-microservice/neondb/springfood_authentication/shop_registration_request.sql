create table shop_registration_request
(
    request_id    uuid not null
        primary key,
    user_id       uuid,
    shop_name     varchar(255),
    logo_media_id varchar(255),
    introduction  text,
    shop_type     varchar(50),
    business_type varchar(50),
    email         varchar(100),
    phone_number  varchar(50),
    shop_address  varchar(255),
    city          varchar(50),
    province      varchar(50),
    postal_code   varchar(50),
    nation_id     varchar(50),
    active_hours  varchar(1000),
    tax_id        varchar(50),
    status        varchar(50),
    reject_reason text,
    reviewed_by   varchar(50),
    reviewed_at   timestamp with time zone,
    shop_id       uuid,
    created_at    timestamp with time zone,
    updated_at    timestamp with time zone
);

alter table shop_registration_request
    owner to neondb_owner;

create index idx_shop_reg_user
    on shop_registration_request (user_id, status);

create index idx_shop_reg_status
    on shop_registration_request (status, created_at);

