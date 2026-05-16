create table product_view_history
(
    id         uuid                        not null
        primary key,
    product_id uuid                        not null,
    session_id varchar(100),
    source     varchar(50),
    user_id    uuid                        not null,
    viewed_at  timestamp(6) with time zone not null
);

alter table product_view_history
    owner to neondb_owner;

create index idx_user_viewed_at
    on product_view_history (user_id asc, viewed_at desc);

