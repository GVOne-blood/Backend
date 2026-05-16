create table shop_members
(
    shop_id        uuid,
    user_id        uuid,
    role_name      varchar(255),
    created_at     timestamp with time zone,
    updated_at     timestamp with time zone,
    department     varchar(50),
    join_date      varchar(50),
    status         varchar(50),
    end_date       varchar(50),
    work_schedule  varchar(255),
    salary_type    varchar(50),
    base_salary    numeric(18, 2),
    commission     numeric(18, 2),
    shop_member_id varchar(50) not null
        primary key
);

alter table shop_members
    owner to neondb_owner;

