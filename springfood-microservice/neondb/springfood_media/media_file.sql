create table media_file
(
    id                 varchar(255)  not null
        primary key,
    created_by         varchar(50)   not null,
    created_date       timestamp(6),
    last_modified_by   varchar(50),
    last_modified_date timestamp(6),
    stored_bucket_name varchar(255),
    description        varchar(1000),
    file_hash          varchar(64)   not null,
    file_original_name varchar(255)  not null,
    file_path          varchar(1000),
    file_size          bigint        not null,
    file_stored_name   varchar(255)  not null,
    file_type          smallint      not null
        constraint media_file_file_type_check
            check ((file_type >= 0) AND (file_type <= 4)),
    file_url           varchar(1000) not null,
    is_active          boolean       not null,
    upload_module      smallint
        constraint media_file_upload_module_check
            check ((upload_module >= 0) AND (upload_module <= 9)),
    upload_status      smallint
        constraint media_file_upload_status_check
            check ((upload_status >= 0) AND (upload_status <= 6))
);

alter table media_file
    owner to neondb_owner;

