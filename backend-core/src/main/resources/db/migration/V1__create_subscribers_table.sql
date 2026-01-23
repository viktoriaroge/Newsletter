CREATE TABLE subscribers
(
    id           VARCHAR(36) PRIMARY KEY,
    email        VARCHAR(255) NOT NULL UNIQUE,
    status       VARCHAR(50)  NOT NULL,
    created_at   VARCHAR(50)  NOT NULL,
    confirmed_at VARCHAR(50)
);