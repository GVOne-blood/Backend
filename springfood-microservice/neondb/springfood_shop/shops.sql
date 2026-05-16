create table shops
(
    shop_id             uuid not null
        primary key,
    shop_name           varchar(255),
    logo                varchar(255),
    introduction        text,
    shop_status         varchar(255),
    total_product       integer,
    total_sold          integer,
    created_at          timestamp with time zone,
    updated_at          timestamp with time zone,
    shop_type           varchar(50),
    total_traffic       integer,
    avg_star            numeric(2, 2),
    total_feedback      integer,
    total_orders        integer,
    email               varchar(50),
    phone_number        varchar(50),
    business_type       varchar(50),
    tax_id              varchar(50),
    is_bln              integer,
    is_active           integer,
    commission          numeric(2, 4),
    shop_address        varchar(255),
    city                varchar(50),
    province            varchar(50),
    nation_id           varchar(50),
    postal_code         varchar(50),
    active_hours        varchar(1000),
    contract_start_date timestamp(6),
    contract_end_date   timestamp(6),
    shop_level          integer
);

alter table shops
    owner to neondb_owner;

