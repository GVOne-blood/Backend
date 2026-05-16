create table user_ekyc
(
    kyc_id               uuid        not null
        primary key,
    request_id           uuid,
    shop_id              uuid,
    user_id              uuid        not null,
    id_number            varchar(20) not null,
    full_name            varchar(100),
    date_of_birth        date,
    gender               varchar(10),
    permanent_address    text,
    issued_date          date,
    issued_place         varchar(255),
    front_image_media_id varchar(255),
    back_image_media_id  varchar(255),
    selfie_media_id      varchar(255),
    nfc_verified         boolean default false,
    nfc_verified_at      timestamp with time zone,
    nfc_raw_data         text,
    verification_status  varchar(50),
    verified_by          varchar(50),
    verified_at          timestamp with time zone,
    rejection_reason     text,
    created_at           timestamp with time zone,
    updated_at           timestamp with time zone
);

alter table user_ekyc
    owner to neondb_owner;

create index idx_shop_kyc_user
    on user_ekyc (user_id);

create index idx_shop_kyc_id_number
    on user_ekyc (id_number);

