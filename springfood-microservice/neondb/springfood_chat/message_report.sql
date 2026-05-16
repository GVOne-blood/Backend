create table message_report
(
    report_id          varchar(50)  not null
        primary key,
    reporter_id        varchar(255) not null,
    message_id         varchar(255) not null,
    reason             varchar(30)  not null,
    details            varchar(1000),
    status             varchar(20)  not null,
    reviewed_by        varchar(100),
    reviewed_at        timestamp,
    review_notes       varchar(1000),
    action_taken       varchar(500),
    created_date       timestamp,
    last_modified_by   varchar(50),
    last_modified_date timestamp,
    created_by         varchar(50)
);

comment on table message_report is 'MessageReport - Report messages';

comment on column message_report.reason is 'Reason: SPAM, HARASSMENT, INAPPROPRIATE_CONTENT, SCAM, OTHER';

comment on column message_report.status is 'Status: PENDING, REVIEWED, RESOLVED, DISMISSED';

alter table message_report
    owner to neondb_owner;

