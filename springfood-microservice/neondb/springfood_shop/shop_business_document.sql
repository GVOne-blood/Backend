create table shop_business_document
(
    doc_id              uuid not null
        primary key,
    request_id          uuid,
    shop_id             uuid,
    company_name        varchar(255),
    business_reg_number varchar(100),
    license_media_id    varchar(255),
    company_address     varchar(500),
    verification_status varchar(50),
    verified_by         varchar(50),
    verified_at         timestamp with time zone,
    rejection_reason    text,
    created_at          timestamp with time zone,
    updated_at          timestamp with time zone
);

alter table shop_business_document
    owner to neondb_owner;

