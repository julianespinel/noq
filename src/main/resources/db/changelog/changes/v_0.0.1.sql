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
    companyId  INTEGER REFERENCES companies (id),
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,

    UNIQUE (name, companyId)
);
--rollback drop table branches;
