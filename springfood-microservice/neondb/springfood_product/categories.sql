create table categories
(
    category_name       varchar(255) not null
        primary key,
    description         text,
    is_active           boolean,
    slug                varchar(255),
    parent_id           varchar(255)
        constraint fksaok720gsu4u2wrgbk10b5n8d
            references categories,
    category_group_code varchar(255),
    is_lock             integer
);

alter table categories
    owner to neondb_owner;

