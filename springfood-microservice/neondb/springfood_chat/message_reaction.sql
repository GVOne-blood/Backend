create table message_reaction
(
    reaction_id        varchar(50)  not null
        primary key,
    user_id            varchar(255) not null,
    emoji              varchar(50)  not null,
    emoji_display      varchar(20),
    message_message_id varchar(50)  not null
        constraint fk_message_reaction__message_id
            references message,
    created_date       timestamp,
    last_modified_by   varchar(50),
    last_modified_date timestamp,
    created_by         varchar(50)
);

comment on table message_reaction is 'MessageReaction - Emoji reactions';

alter table message_reaction
    owner to neondb_owner;

