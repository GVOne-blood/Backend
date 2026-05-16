create table user_has_role
(
    user_id    uuid
        constraint fkblk8wtbcujd0row2o8shrog98
            references "user",
    role_name  varchar(255)
        constraint fkdr3ewminb39x8pd3hktnpadi1
            references role,
    created_at timestamp with time zone,
    updated_at timestamp
);

alter table user_has_role
    owner to neondb_owner;

