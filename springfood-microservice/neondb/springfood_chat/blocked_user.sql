create table blocked_user
(
    block_id           varchar(50)  not null
        primary key,
    blocker_id         varchar(255) not null,
    blocked_user_id    varchar(255) not null,
    reason             varchar(500),
    created_date       timestamp,
    last_modified_by   varchar(50),
    last_modified_date timestamp,
    created_by         varchar(50)
);

comment on table blocked_user is 'BlockedUser - Block list';

alter table blocked_user
    owner to neondb_owner;

