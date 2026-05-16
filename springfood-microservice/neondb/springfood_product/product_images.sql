create table product_images
(
    id                 uuid not null
        primary key,
    product_id         uuid,
    bucket_name        varchar(255),
    object_name        varchar(255),
    original_file_name varchar(255),
    file_size          bigint,
    mime_type          varchar(100),
    image_url          varchar(255),
    status             varchar(255),
    uploaded_at        timestamp,
    uploaded_by        varchar(255),
    created_at         timestamp,
    updated_at         timestamp
);

alter table product_images
    owner to neondb_owner;

