create table payment_methods
(
    payment_name varchar(255) not null
        primary key,
    description  text,
    is_active    boolean,
    created_at   timestamp with time zone,
    updated_at   timestamp with time zone
);

alter table payment_methods
    owner to neondb_owner;

