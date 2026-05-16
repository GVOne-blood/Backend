create table user_device
(
    device_id    uuid not null
        primary key,
    created_at   timestamp(6) with time zone,
    device_name  varchar(255),
    is_active    boolean,
    last_used_at timestamp(6) with time zone,
    platform     varchar(20),
    push_token   varchar(500),
    updated_at   timestamp(6) with time zone,
    user_id      uuid not null
);

alter table user_device
    owner to neondb_owner;

