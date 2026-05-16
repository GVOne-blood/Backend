create table message_read_receipt
(
    receipt_id         varchar(50)  not null
        primary key,
    user_id            varchar(255) not null,
    read_at            timestamp    not null,
    device_type        varchar(50),
    message_message_id varchar(50)  not null
        constraint fk_message_read_receipt__message_id
            references message,
    created_date       timestamp,
    last_modified_by   varchar(50),
    last_modified_date timestamp,
    created_by         varchar(50)
);

comment on table message_read_receipt is 'MessageReadReceipt - Read tracking';

alter table message_read_receipt
    owner to neondb_owner;

