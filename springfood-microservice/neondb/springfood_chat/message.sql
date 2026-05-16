create table message
(
    message_id                     varchar(50)  not null
        primary key,
    client_message_id              varchar(100),
    sender_id                      varchar(255) not null,
    sender_name                    varchar(100),
    sender_avatar                  varchar(500),
    message_type                   varchar(30)  not null,
    content                        text,
    content_preview                varchar(200),
    reply_to_message_id            varchar(255),
    reply_to_preview               varchar(200),
    forwarded_from_message_id      varchar(255),
    forwarded_from_conversation_id varchar(255),
    reference_type                 varchar(50),
    reference_id                   varchar(100),
    status                         varchar(20)  not null,
    is_edited                      integer,
    edited_at                      timestamp,
    is_deleted                     integer,
    deleted_at                     timestamp,
    deleted_by                     varchar(100),
    reaction_count                 integer,
    conversation_conversation_id   varchar(50)  not null
        constraint fk_message__conversation_id
            references conversation,
    created_date                   timestamp,
    last_modified_by               varchar(50),
    last_modified_date             timestamp,
    created_by                     varchar(50),
    is_read                        boolean,
    metadata                       text,
    sender_type                    varchar(10)
);

comment on table message is 'Message - Individual chat messages';

comment on column message.message_type is 'Type: TEXT, IMAGE, VIDEO, FILE, AUDIO, LOCATION, STICKER, SYSTEM, ORDER_CARD, PRODUCT_CARD';

comment on column message.status is 'Status: SENDING, SENT, DELIVERED, READ, FAILED';

alter table message
    owner to neondb_owner;

create index idx_msg_client_id
    on message (client_message_id);

create index idx_msg_conv_created
    on message (conversation_conversation_id, created_date);

create index idx_msg_sender
    on message (sender_id, created_date);

