CREATE TABLE challenges (
    id                BIGSERIAL PRIMARY KEY,
    creator_id        BIGINT       NOT NULL REFERENCES users(id),
    title             VARCHAR(200) NOT NULL,
    description       TEXT,
    duration_days     INT          NOT NULL,
    start_date        DATE         NOT NULL,
    end_date          DATE         NOT NULL,
    max_participants  INT          DEFAULT 50,
    fail_threshold    DECIMAL(3,2) NOT NULL DEFAULT 0.15,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE challenge_participants (
    id              BIGSERIAL PRIMARY KEY,
    challenge_id    BIGINT       NOT NULL REFERENCES challenges(id),
    user_id         BIGINT       NOT NULL REFERENCES users(id),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    missed_count    INT          NOT NULL DEFAULT 0,
    joined_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMP,
    UNIQUE(challenge_id, user_id)
);

CREATE TABLE challenge_daily_logs (
    id              BIGSERIAL PRIMARY KEY,
    participant_id  BIGINT       NOT NULL REFERENCES challenge_participants(id),
    log_date        DATE         NOT NULL,
    achieved        BOOLEAN      NOT NULL DEFAULT FALSE,
    checked_at      TIMESTAMP,
    UNIQUE(participant_id, log_date)
);

CREATE INDEX idx_challenges_start_date ON challenges(start_date);
CREATE INDEX idx_cp_challenge_id ON challenge_participants(challenge_id);
CREATE INDEX idx_cp_user_id ON challenge_participants(user_id);
CREATE INDEX idx_cp_status ON challenge_participants(status);
CREATE INDEX idx_cdl_participant_id ON challenge_daily_logs(participant_id);
