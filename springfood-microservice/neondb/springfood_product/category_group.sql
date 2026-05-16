create table category_group
(
    group_id      uuid         not null
        primary key,
    created_at    timestamp(6) with time zone,
    description   text,
    display_order integer,
    group_code    varchar(100) not null
        constraint ukaut54j2obiv822n1f8fepvh4w
            unique,
    group_name    varchar(255) not null,
    icon_url      varchar(500),
    is_active     boolean,
    updated_at    timestamp(6) with time zone
);

alter table category_group
    owner to neondb_owner;

