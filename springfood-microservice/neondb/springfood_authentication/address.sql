create table address
(
    address_id uuid not null
        primary key,
    user_id    uuid
        constraint fkibojxnhlre8lcn6ag9a35epr1
            references "user",
    street     varchar(255),
    ward       varchar(255),
    city       varchar(255),
    details    varchar(255),
    created_at timestamp with time zone,
    updated_at timestamp with time zone
);

alter table address
    owner to neondb_owner;

