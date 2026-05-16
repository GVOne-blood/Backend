create table shop_wallets
(
    wallet_id      uuid not null
        primary key,
    shop_id        uuid,
    balance        numeric(15, 2),
    pending_amount numeric(15, 2),
    locked_amount  numeric(15, 2),
    created_at     timestamp with time zone,
    updated_at     timestamp with time zone
);

alter table shop_wallets
    owner to neondb_owner;

