create table role_has_permission
(
    role_name       varchar(255)
        constraint fkc9qsyy8y1qdfv1tnenrdm2w5u
            references role,
    permission_name varchar(255)
        constraint fkoqxo0nij3sjiqewl79gn8t4yk
            references permission,
    created_at      timestamp with time zone,
    updated_at      timestamp
);

alter table role_has_permission
    owner to neondb_owner;

