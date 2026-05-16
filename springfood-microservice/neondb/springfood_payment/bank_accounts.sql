create table bank_accounts
(
    account_id          uuid not null
        primary key,
    shop_id             uuid,
    bank_name           varchar(100),
    account_number      varchar(50),
    account_holder_name varchar(255),
    is_default          boolean,
    is_verified         boolean,
    created_at          timestamp with time zone,
    updated_at          timestamp with time zone
);

alter table bank_accounts
    owner to neondb_owner;

