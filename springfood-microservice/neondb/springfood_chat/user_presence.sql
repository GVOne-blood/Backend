create table user_presence
(
    presence_id            varchar(50)  not null
        primary key,
    user_id                varchar(255) not null
        constraint ux_user_presence__user_id
            unique,
    status                 varchar(20)  not null,
    status_message         varchar(100),
    last_seen_at           timestamp    not null,
    active_conversation_id varchar(255),
    device_type            varchar(50),
    device_id              varchar(100),
    session_id             varchar(100),
    last_activity_at       timestamp,
    created_date           timestamp,
    last_modified_by       varchar(50),
    last_modified_date     timestamp,
    created_by             varchar(50)
);

comment on table user_presence is 'UserPresence - Online status tracking';

comment on column user_presence.status is 'Status: ONLINE, AWAY, BUSY, OFFLINE';

alter table user_presence
    owner to neondb_owner;

