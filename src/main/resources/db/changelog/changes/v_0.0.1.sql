--changeset julianespinel:1
CREATE TABLE companies
(
    id         SERIAL PRIMARY KEY,
    nit        VARCHAR(256) UNIQUE,
    name       VARCHAR(256) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);
--rollback drop table companies;

--changeset julianespinel:2
CREATE TABLE branches
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(256) NOT NULL,
    company_id INTEGER REFERENCES companies (id),
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,

    UNIQUE (name, company_id)
);
--rollback drop table branches;

--changeset julianespinel:3
CREATE TABLE queues
(
    id           SERIAL PRIMARY KEY,
    name         VARCHAR(256) NOT NULL,
    initial_turn VARCHAR(128) NOT NULL,
    branch_id    INTEGER REFERENCES branches (id),
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL,

    UNIQUE (name, branch_id)
);
--rollback drop table queues;

--changeset julianespinel:4
CREATE TABLE turns
(
    id            SERIAL PRIMARY KEY,
    turn_number   VARCHAR(128) NOT NULL,
    phone_number  VARCHAR(128) NOT NULL,
    current_state VARCHAR(128) NOT NULL,
    queue_id      INTEGER REFERENCES queues (id),
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,

    UNIQUE (turn_number, phone_number, queue_id)
);
--rollback drop table turns;

--changeset julianespinel:5
CREATE TABLE turn_states
(
    id         SERIAL PRIMARY KEY,
    turn_id    INTEGER REFERENCES turns (id),
    state      VARCHAR(128) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,

    UNIQUE (turn_id, state)
);
--rollback drop table turn_states;
