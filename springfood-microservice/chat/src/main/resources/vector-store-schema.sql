CREATE TABLE IF NOT EXISTS springfood_chat.vector_store (
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    content text,
    metadata json,
    embedding vector(768)
);
