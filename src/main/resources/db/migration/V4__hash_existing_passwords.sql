-- Pgcrypto extension (PostgreSQL Docker image includes it)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Hash all existing passwords using BCrypt (cost factor 12)
UPDATE "user"
SET password = crypt(password, gen_salt('bf', 12));
