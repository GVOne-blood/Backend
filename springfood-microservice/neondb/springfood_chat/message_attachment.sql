create table message_attachment
(
    attachment_id      varchar(50)  not null
        primary key,
    media_id           varchar(255) not null,
    attachment_type    varchar(20)  not null,
    file_name          varchar(255),
    file_size          bigint,
    mime_type          varchar(100),
    url                varchar(1000),
    thumbnail_url      varchar(1000),
    width              integer,
    height             integer,
    duration           integer,
    display_order      integer,
    message_message_id varchar(50)  not null
        constraint fk_message_attachment__message_id
            references message,
    created_date       timestamp,
    last_modified_by   varchar(50),
    last_modified_date timestamp,
    created_by         varchar(50)
);

comment on table message_attachment is 'MessageAttachment - Media/files attached to messages';

comment on column message_attachment.attachment_type is 'Type: IMAGE, VIDEO, AUDIO, DOCUMENT, OTHER';

alter table message_attachment
    owner to neondb_owner;

