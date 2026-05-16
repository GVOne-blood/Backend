create table spring_ai_chat_memory
(
    conversation_id varchar(50),
    content         text,
    type            varchar(10),
    timestamp       timestamp
);

alter table spring_ai_chat_memory
    owner to neondb_owner;

create index spring_ai_chat_memory_conversation_id_timestamp_idx
    on spring_ai_chat_memory (conversation_id, timestamp);

