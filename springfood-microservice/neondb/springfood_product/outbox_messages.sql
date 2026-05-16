create table outbox_messages
(
    id             bigint not null
        primary key,
    message_id     varchar(255),
    topic          varchar(255),
    message_key    varchar(255),
    payload        text,
    status         varchar(255),
    retry_count    integer,
    created_at     timestamp,
    updated_at     timestamp,
    last_retry_at  timestamp,
    error_message  varchar(255),
    source_service varchar(255)
);

alter table outbox_messages
    owner to neondb_owner;

