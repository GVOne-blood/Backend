create table typing_indicator
(
    indicator_id       varchar(50)  not null
        primary key,
    user_id            varchar(255) not null,
    user_name          varchar(100),
    conversation_id    varchar(255) not null,
    started_at         timestamp    not null,
    expires_at         timestamp    not null,
    created_date       timestamp,
    last_modified_by   varchar(50),
    last_modified_date timestamp,
    created_by         varchar(50)
);

comment on table typing_indicator is 'TypingIndicator - Who is typing';

alter table typing_indicator
    owner to neondb_owner;

