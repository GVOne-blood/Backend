create table conversation
(
    conversation_id        varchar(50) not null
        primary key,
    conversation_type      varchar(30) not null,
    name                   varchar(100),
    description            varchar(500),
    avatar_url             varchar(500),
    reference_type         varchar(50),
    reference_id           varchar(100),
    last_message_preview   varchar(200),
    last_message_at        timestamp,
    last_message_sender_id varchar(100),
    message_count          bigint,
    is_archived            integer,
    is_pinned              integer,
    settings_settings_id   varchar(50)
        constraint ux_conversation__settings_settings_id
            unique
        constraint fk_conversation__settings_id
            references conversation_settings,
    created_date           timestamp,
    last_modified_by       varchar(50),
    last_modified_date     timestamp,
    created_by             varchar(50),
    last_message_id        varchar(255),
    participant1_id        varchar(255),
    participant2_id        varchar(255)
);

comment on table conversation is 'Conversation - Chat room/thread';

comment on column conversation.conversation_type is 'Type: DIRECT, GROUP, ORDER_SUPPORT, SHOP_SUPPORT';

alter table conversation
    owner to neondb_owner;

