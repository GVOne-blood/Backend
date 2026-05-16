create table ai_message
(
    message_id         varchar(50)  not null
        primary key,
    conversation_id    varchar(100) not null,
    user_id            varchar(100) not null,
    message_type       varchar(20)  not null,
    content            text,
    content_preview    varchar(200),
    metadata           text,
    token_count        integer,
    response_time_ms   integer,
    model_name         varchar(50),
    is_deleted         integer default 0,
    deleted_at         timestamp,
    created_by         varchar(50),
    created_date       timestamp,
    last_modified_by   varchar(50),
    last_modified_date timestamp
);

alter table ai_message
    owner to neondb_owner;

create index idx_ai_msg_conv_created
    on ai_message (conversation_id, created_date);

create index idx_ai_msg_type
    on ai_message (message_type);

create index idx_ai_msg_user
    on ai_message (user_id, created_date);

