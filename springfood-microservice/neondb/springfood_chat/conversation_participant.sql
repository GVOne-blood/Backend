create table conversation_participant
(
    participant_id               varchar(50)  not null
        primary key,
    user_id                      varchar(255) not null,
    display_name                 varchar(100),
    avatar_url                   varchar(500),
    role                         varchar(20)  not null,
    status                       varchar(20)  not null,
    nickname                     varchar(50),
    last_read_message_id         varchar(255),
    last_read_at                 timestamp,
    unread_count                 integer,
    is_muted                     integer,
    mute_until                   timestamp,
    is_pinned                    integer,
    pinned_at                    timestamp,
    joined_at                    timestamp    not null,
    left_at                      timestamp,
    added_by                     varchar(100),
    conversation_conversation_id varchar(50)  not null
        constraint fk_conversation_participant__conversation_id
            references conversation,
    created_date                 timestamp,
    last_modified_by             varchar(50),
    last_modified_date           timestamp,
    created_by                   varchar(50)
);

comment on table conversation_participant is 'ConversationParticipant - Users in conversations';

comment on column conversation_participant.role is 'Role: OWNER, ADMIN, MEMBER';

comment on column conversation_participant.status is 'Status: ACTIVE, LEFT, REMOVED, MUTED';

alter table conversation_participant
    owner to neondb_owner;

create index idx_participant_unread
    on conversation_participant (user_id, unread_count);

create index idx_participant_user
    on conversation_participant (user_id);

