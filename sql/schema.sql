-- DDL for gym-app (PostgreSQL)
-- user is a reserved word — always double-quote it

CREATE TABLE training_type (
    id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    training_type_name VARCHAR(64) NOT NULL UNIQUE
);

CREATE TABLE "user" (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(64)  NOT NULL,
    last_name  VARCHAR(64)  NOT NULL,
    username   VARCHAR(64)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    is_active  BOOLEAN      NOT NULL
);

CREATE TABLE trainee (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID         NOT NULL UNIQUE REFERENCES "user"(id) ON DELETE CASCADE,
    date_of_birth DATE,
    address       VARCHAR(255)
);

CREATE TABLE trainer (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID NOT NULL UNIQUE REFERENCES "user"(id)        ON DELETE CASCADE,
    specialization_id UUID NOT NULL        REFERENCES training_type(id) ON DELETE RESTRICT
);

CREATE TABLE training (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    trainee_id        UUID         NOT NULL REFERENCES trainee(id)       ON DELETE CASCADE,
    trainer_id        UUID         NOT NULL REFERENCES trainer(id)       ON DELETE RESTRICT,
    training_type_id  UUID         NOT NULL REFERENCES training_type(id) ON DELETE RESTRICT,
    training_name     VARCHAR(128) NOT NULL,
    training_date     DATE         NOT NULL,
    training_duration INTEGER      NOT NULL
);

CREATE TABLE trainee2trainer (
    trainee_id UUID NOT NULL REFERENCES trainee(id) ON DELETE CASCADE,
    trainer_id UUID NOT NULL REFERENCES trainer(id) ON DELETE CASCADE,
    PRIMARY KEY (trainee_id, trainer_id)
);
