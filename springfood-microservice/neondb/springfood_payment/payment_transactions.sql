create table payment_transactions
(
    id                       uuid not null
        primary key,
    user_id                  varchar(255),
    payment_method_name      varchar(255),
    amount                   numeric(38, 2),
    status                   varchar(255),
    provider_transaction_ref varchar(255),
    reference_type           varchar(255),
    reference_id             uuid,
    created_at               timestamp with time zone,
    updated_at               timestamp with time zone,
    success_at               timestamp
);

alter table payment_transactions
    owner to neondb_owner;

