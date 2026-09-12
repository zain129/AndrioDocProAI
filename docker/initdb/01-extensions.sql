-- Runs once, only when the data volume is first initialized.
-- pgvector powers semantic search over document embeddings.
CREATE EXTENSION IF NOT EXISTS vector;
-- gen_random_uuid() etc. (handy even though IDs are app-populated)
CREATE EXTENSION IF NOT EXISTS pgcrypto;
