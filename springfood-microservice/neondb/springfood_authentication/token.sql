create table token
(
    id          uuid not null
        primary key,
    user_id     uuid
        constraint fkl10xjn274m2rkxo54knt2xqvy
            references "user",
    token_value varchar(255),
    type        varchar(255),
    expires_at  timestamp with time zone,
    is_revoked  boolean,
    created_at  timestamp with time zone,
    token       varchar(255),
    token_type  varchar(255)
);

alter table token
    owner to neondb_owner;

