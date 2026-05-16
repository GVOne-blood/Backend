create table notifications
(
    notification_id    varchar(50) not null
        primary key,
    table_name         varchar(50),
    object_id          varchar(50),
    notification_type  varchar(255),
    event_id           varchar(50),
    receive_id         varchar(50),
    is_active          integer,
    title              varchar(2000),
    body               varchar(2000),
    action_url         varchar(2000),
    is_viewed          integer,
    is_clicked         integer,
    last_modified_date timestamp,
    last_modified_by   varchar(50),
    created_by         varchar(50),
    created_date       timestamp
);

alter table notifications
    owner to neondb_owner;

