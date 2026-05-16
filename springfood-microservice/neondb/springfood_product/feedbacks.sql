create table feedbacks
(
    feedback_id           uuid not null
        primary key,
    created_at            timestamp,
    updated_at            timestamp,
    content               varchar(255),
    rating                integer,
    user_id               uuid,
    product_id            uuid
        constraint fkti2ywtwc29ys1i591rmmaveyc
            references products,
    is_active             boolean,
    is_shop_reply         boolean,
    "media-file-id"       varchar(255),
    "product-variants-id" uuid,
    "shop-id"             uuid,
    "feedback-title"      varchar(255),
    "feedback-type"       varchar(255)
        constraint "feedbacks_feedback-type_check"
            check (("feedback-type")::text = ANY
                   ((ARRAY ['PRODUCT_FEEDBACK'::character varying, 'SHOP_FEEDBACK'::character varying, 'SHOP_REPLY'::character varying])::text[]))
);

alter table feedbacks
    owner to neondb_owner;

