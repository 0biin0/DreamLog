CREATE TABLE prompt_templates (
    id              BIGSERIAL PRIMARY KEY,
    category        VARCHAR(50)  NOT NULL,
    content_ko      TEXT         NOT NULL,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);
