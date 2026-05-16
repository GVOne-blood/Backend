create table "user"
(
    user_id        uuid not null
        primary key,
    first_name     varchar(50),
    last_name      varchar(50),
    email          varchar(255),
    email_verified boolean,
    phone          varchar(20),
    phone_verified boolean,
    password       varchar(255),
    avatar         varchar(255),
    dob            date,
    gender         varchar(255),
    status         varchar(255),
    last_login_at  timestamp with time zone,
    created_at     timestamp with time zone,
    updated_at     timestamp with time zone,
    username       varchar(255),
    is_deleted     boolean,
    address        varchar(255),
    shop_id        varchar(255)
);

alter table "user"
    owner to neondb_owner;

