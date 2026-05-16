create table acc_login_log
(
    acc_audit_log_id   varchar(50)                                            not null
        constraint pk_acc_login_log
            primary key,
    user_id            varchar(50),
    auth_type          varchar(20)                                            not null,
    event_type         varchar(255)                                           not null,
    event_details      varchar(4000),
    ip_address         varchar(45)                                            not null,
    device_type        varchar(50),
    login_attempt_time timestamp(6) default NULL::timestamp without time zone not null,
    created_by         varchar(50),
    created_date       timestamp(6),
    last_modified_by   varchar(50),
    last_modified_date timestamp(6)
);

alter table acc_login_log
    owner to neondb_owner;

