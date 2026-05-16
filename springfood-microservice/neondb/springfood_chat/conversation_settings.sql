create table conversation_settings
(
    settings_id                varchar(50) not null
        primary key,
    only_admin_can_send        integer,
    only_admin_can_add_members integer,
    auto_delete_days           integer,
    allow_reactions            integer,
    allow_replies              integer,
    allow_attachments          integer,
    max_attachment_size_mb     integer,
    allowed_file_types         varchar(500),
    show_read_receipts         integer,
    show_typing_indicators     integer,
    created_date               timestamp,
    last_modified_by           varchar(50),
    last_modified_date         timestamp,
    created_by                 varchar(50)
);

comment on table conversation_settings is 'ConversationSettings - Per-conversation settings';

alter table conversation_settings
    owner to neondb_owner;

