create table outbox_messages
(
    id             bigint not null
        primary key,
    created_at     timestamp,
    error_message  varchar(255),
    message_key    varchar(255),
    last_retry_at  timestamp,
    message_id     varchar(255),
    payload        text,
    retry_count    integer,
    source_service varchar(255),
    status         varchar(255),
    topic          varchar(255),
    updated_at     timestamp
);

alter table outbox_messages
    owner to neondb_owner;

