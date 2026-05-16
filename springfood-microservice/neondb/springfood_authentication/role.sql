create table role
(
    role_name   varchar(255) not null
        primary key,
    description varchar(255),
    created_at  timestamp with time zone,
    updated_at  timestamp with time zone
);

alter table role
    owner to neondb_owner;

