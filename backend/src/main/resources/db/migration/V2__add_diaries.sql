CREATE TABLE diaries (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users(id),
    title           VARCHAR(200) NOT NULL,
    content         TEXT         NOT NULL,
    unlock_date     DATE         NOT NULL,
    edit_deadline   TIMESTAMP    NOT NULL,
    written_date    DATE         NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_diaries_user_id ON diaries(user_id);
CREATE INDEX idx_diaries_unlock_date ON diaries(unlock_date);
CREATE INDEX idx_diaries_user_written ON diaries(user_id, written_date);
