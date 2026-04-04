ALTER TABLE "user"
    ADD COLUMN failed_login_attempts INT       NOT NULL DEFAULT 0,
    ADD COLUMN account_locked_until  TIMESTAMP,
    ADD COLUMN last_failed_login_at  TIMESTAMP,
    ADD COLUMN last_logout           TIMESTAMP;
