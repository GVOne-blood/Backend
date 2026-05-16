create table sales
(
    sale_id             uuid not null
        primary key,
    created_at          timestamp,
    updated_at          timestamp,
    conditions          varchar(255),
    description         text,
    discount_percentage numeric(5, 2),
    end_date            timestamp,
    start_date          timestamp,
    name                varchar(255)
);

alter table sales
    owner to neondb_owner;

