create table permission
(
    permission_name varchar(255) not null
        primary key,
    description     varchar(255),
    created_at      timestamp with time zone,
    updated_at      timestamp with time zone
);

alter table permission
    owner to neondb_owner;

